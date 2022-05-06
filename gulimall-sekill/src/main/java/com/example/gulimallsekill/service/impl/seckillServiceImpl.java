package com.example.gulimallsekill.service.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.Exception.NoSeckillSessionException;
import com.atguigu.common.to.mq.SeckillOrderTo;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberResVo;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import com.atguigu.gulimall.seckill.vo.SeckillSkuVo;
import com.atguigu.gulimall.seckill.vo.SkuInfoVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.example.gulimallsekill.feign.productFeignService;
import com.example.gulimallsekill.interctor.MyInterceptor;
import com.example.gulimallsekill.service.seckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


//TODO秒杀模块
@Service
@Slf4j
public class seckillServiceImpl implements seckillService {

    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    com.example.gulimallsekill.feign.couponFeignService couponFeignService;
    @Autowired
    productFeignService productFeignService;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    MyInterceptor myInterceptor;
    @Autowired
    RabbitTemplate rabbitTemplate;
    public static final String SKUKILL_CACHE_PREFIX="seckill:skus";   //商品的基本信息，skuid作为key，方便详情页查询秒杀信息
    public static final String SESSIONS_CACHE_PREFIX="seckill:sessions";  //活动基本信息，key为开始时间和结束时间，value为[skuids]
    public static final String SKU_STOCK_SEMAPHORE="seckill:sku:stock";

    @Override
    public String kill(String killId, String key, Integer num) {
//        登入检测->合法性校验->      成功->获取信号量-> 成功 ->快速创建秒杀单，用户，订单号，商品->    失败，秒杀时间，随机码，对应关系，幂等性->结束
//      1  -》（前端返回秒杀成功，正在为您准备订单）->收货地址确认页->支付确认页->结束
//      2  -》发送mq消息，订单服务监听，准备创建订单

        MemberResVo memberResVo = myInterceptor.threadLocal.get();
        BoundHashOperations ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        String item = (String) ops.get(killId);
        if(item!=null){
            SeckillSkuRedisTo skuRedisTo = JSON.parseObject(item, SeckillSkuRedisTo.class);
            long time = new Date().getTime();
            if(time>=skuRedisTo.getStartTime() && time <= skuRedisTo.getEndTime() ){
                log.info("时间检验成功");
                String skuKey=skuRedisTo.getPromotionId()+"_"+skuRedisTo.getSkuId();
//                验证码是否成功和购买是否超过单笔限制
                if (skuRedisTo.getRandomCode().equals(key) && num<skuRedisTo.getSeckillLimit()){
//                    该用户是否秒杀过，如果用户购买过了再刷新，这时候不能再买了，这就是一个幂等性诊断
//                    幂等性的解决方法：token机制，锁，防重表，各种唯一约束，全局请求唯一id
//                    一个用户秒杀成功过，系统要对这个用户进行记录，如果每个商品有一个已经秒杀的用户idSET集合或许可以，//类似于防重表
//                   如果使用各种唯一性约束：利用redis的setIfAbsent，只有不存在才会加入key就是 uerId+活动场次id+商品id
                    Long userId = memberResVo.getId();   //通过拦截器获取用户id
                    String setRedisKey=userId+"_"+skuKey;
                    Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(setRedisKey, num,
                            skuRedisTo.getEndTime()-time, TimeUnit.MILLISECONDS);//成功就是加成了，那么这个用户就是第一次
                    if(aBoolean){
//                  所有的合法性校验都通过了，这时候该获取用户量了
                       RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + key); //获取信号量
                       try {
                           boolean b = semaphore.tryAcquire(num, 100, TimeUnit.MILLISECONDS);//这是非阻塞的
                           if(b){
                               // TODO进行下单,给RabbitMq发送消息
//                               rabbitTemplate.convertAndSend("kill_order_exchange",
//                                       "kill_order",skuRedisTo);
                               String orderSn = IdWorker.getIdStr();  //如果秒杀成功返回订单号
                               SeckillOrderTo seckillOrderTo = new SeckillOrderTo();
                               seckillOrderTo.setOrderSn(orderSn);
                               seckillOrderTo.setNum(num);
                               seckillOrderTo.setSkuId(skuRedisTo.getSkuId());
                               seckillOrderTo.setMemberId(memberResVo.getId());
                               seckillOrderTo.setPromotionSessionId(skuRedisTo.getPromotionSessionId());
                               seckillOrderTo.setSeckillPrice(skuRedisTo.getSeckillPrice());
                               rabbitTemplate.convertAndSend("order-event-exchange",
                                       "order.seckill.order",seckillOrderTo);
                               return orderSn;
                           }
                       }catch (Exception e){
                           return null;
                       }
                    }else{
                        log.info("该用户已经够没过了不能再次购买");
                        return null;
                    }
                }else{
                    log.info("随机码验证是啊比或者购买数量超过了限制");
                    return null;
                }
            }else{
                return null;
            }

        }else{
            return null;
        }


        return null;
    }
    @Override
    public void uploadSeckillSkuLast3Days() {
        R r = couponFeignService.getSeckillLast3Day();
        if(r.getCode()==0){
            List<com.atguigu.gulimall.seckill.vo.SeckillSessionWithSku> data =
                    r.getData(new TypeReference<List<com.atguigu.gulimall.seckill.vo.SeckillSessionWithSku>>() {});
            if(data!=null){
                //                           缓存活动信息
                saveSeckillSession(data);
                //                           缓存商品信息
                saveSkuInfo(data);
            }else{
                throw  new NoSeckillSessionException();
            }
        }
    }
    /**
     * 获取到当前已经上架的商品，要获取到缓存中的商品数据，。。还要对比时间的对吧
     * （获取未来三天的活动，也就是活动开始时间在 现在和3天之后之内=》redis中的活动now<starttime.现在获取已经上架的商品的条件是NOW>startTime,<ENDTIME
     * @return
     */

    @Override
    @SentinelResource(value = "hello",blockHandler = "getCurrentSeckillSkusBlockHandler")
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        List<String> resList = new ArrayList<>();
//        1:看当前时间的场次
        long time = new Date().getTime();
        //获取到所有key值含SESSIONS_CACHE_PREFIX的key，开发环境最好用scan，因为keys会阻塞
        Set<String> keys = redisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
        BoundHashOperations ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX); //绑定商品hash
        log.info("keys:"+keys);
        for (String key : keys) {
            String[] split = key.split("_");
            Long startTime = Long.valueOf(split[split.length - 2]);
            Long endTime = Long.valueOf(split[split.length - 1]);
            System.out.println(startTime+"_"+new Date().getTime()+"_"+endTime);
                        if(time>=startTime && time<=endTime) {  //在活动区间之间，说明这个活动已经开始了
                        System.out.println("说明这个key就是当前已经上架的活动");
                        List<String> skuIdList = redisTemplate.opsForList().range(key, 0, -1);  //获取到skuidlist
                        List<String> list = ops.multiGet(skuIdList);  //根据skuidlist获取到hash中封装的所有信息
                        System.out.println ("redis的key*" + list);
                        if (list != null && list.size () >= 0) {
                            List<SeckillSkuRedisTo> collect = list.stream().map(item -> { //获取到秒杀商品的所有信息
//                                JSON.parseObject(item,SeckillSkuRedisTo.getClass());
                                SeckillSkuRedisTo skuRedisTo = JSON.parseObject(item, SeckillSkuRedisTo.class);
                                return skuRedisTo;
                            }).collect(Collectors.toList());
//                            因为秒杀已经开始了，所以随机码不用为空
                            log.info("collect:"+collect);
                            return collect;
                        }
                        break;  //break的含义，因为获取的是该活动的所有商品信息，如果获得到的为空，则直接跳出这层循环
                    }
        }
        return null;
    }

    public List<SeckillSkuRedisTo> getCurrentSeckillSkusBlockHandler(BlockException e){
        log.error("触发了降级,{}",e.getMessage());
        return null;
    }
    public void saveSeckillSession(List<com.atguigu.gulimall.seckill.vo.SeckillSessionWithSku> data){
        System.out.println("--data:"+data);
        data.stream().forEach(
                seckillSession->{
                    Long startTime = seckillSession.getStartTime().getTime();
                    Long endTime = seckillSession.getEndTime().getTime();
                    List<SeckillSkuVo> relationEntities = seckillSession.getSeckillSkuRelationEntityList();
                    List<String> skuIdList =
                            relationEntities.stream().map(item->{return item.getPromotionSessionId().toString()+"_"+item.getSkuId().toString();}).collect(Collectors.toList());
                    String key=SESSIONS_CACHE_PREFIX+"_"+startTime+"_"+endTime;
//                    redisTemplate.opsForHash().put(SESSIONS_CACHE_PREFIX,key+endTime,skuIdList);
//                    String s = JSON.toJSONString(skuIdList);
                    if(!redisTemplate.hasKey(key)) {
//                        redisTemplate.opsForList().leftPush(SESSIONS_CACHE_PREFIX,startTime+""+endTime,skuIdList);
                        redisTemplate.opsForList().rightPush(key,skuIdList);
                    }else{ }
                }
        );
    }
//    保存具体的sku信息
    public void saveSkuInfo(List<com.atguigu.gulimall.seckill.vo.SeckillSessionWithSku> data){
        data.stream().forEach(
                seckillSessionWithSku -> {
                    BoundHashOperations ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                    List<SeckillSkuVo> relationEntities = seckillSessionWithSku.getSeckillSkuRelationEntityList();
                    relationEntities.stream().forEach(
                            skuInfoVo->{
                                Long skuId = skuInfoVo.getSkuId();
                                String token = UUID.randomUUID().toString().replace("-", "");
                                SeckillSkuRedisTo seckillSkuRedisTo = new SeckillSkuRedisTo();
                                if(!ops.hasKey(skuInfoVo.getPromotionSessionId().toString()+"_"+skuId.toString())){
//                             1.   sku基本信息
                                    System.out.println("skuid"+skuId);
                                    R info = productFeignService.info(skuId);
                                    if(info.getCode()==0){
                                        SkuInfoVo infoVo = info.getData(new TypeReference<SkuInfoVo>(){});
                                        seckillSkuRedisTo.setSkuInfoVo(infoVo);
                                    }
//                              2.  sku秒杀信息
                                    BeanUtils.copyProperties(skuInfoVo,seckillSkuRedisTo);
//                              3.  开始时间和结束时间用于比较
                                    seckillSkuRedisTo.setStartTime(seckillSessionWithSku.getStartTime().getTime());
                                    seckillSkuRedisTo.setEndTime(seckillSessionWithSku.getEndTime().getTime());
                                    /**
                                     * 4、设置随机码？seckill?skuId&key=dadlajldj、
                                     *      想要来秒杀你不知道随机码、商品id知道、发请求也没用、随机码只要秒杀开启的时候才暴漏出来
                                     *
                                     *   设置信号量： 每个请求进来要减redis的计数、这个信号量的值就是商品库存的信息、比如库存100、那计算器就是100
                                     *   问题：如果带拿商品id过来匹配的一个数量（计数器）、直接去redis中查到了、那就不合适、所以准备一个随机码、
                                     *   只要到了秒杀时间了、然后你知道这个随机码、带上这个随机码、我是按照随机码减信号量的
                                     *
                                     * 如果不带随机码、每次按照skuid来减信号量、会出问题、秒杀还没开始、恶意请求就把信号量减去了、这个随机码又是一种保护机制、
                                     * 买商品这个人知道我的随机码才可以买这个商品、减掉库存
                                     *
                                     * 每一个商品都要设置他的分布式信号量
                                     *
                                     */
                                    seckillSkuRedisTo.setRandomCode(token);
                                    String s = JSON.toJSONString(seckillSkuRedisTo);
                                    String hashKey=skuInfoVo.getPromotionSessionId().toString()+"_"+skuId.toString();
//                                    String jsonString = JSON.toJSONString(hashKey);
                                    ops.put(hashKey,s); //商品id作为key，value为商品的详情信息+秒杀信息+随机码，
                                }else{
                                    log.info("缓存中已有该商品信息"); }
//                              5.信号量
                                if(! redisTemplate.hasKey(SKU_STOCK_SEMAPHORE +skuInfoVo.getPromotionSessionId().toString()+"_"+skuId.toString()) ){ //缓存中没信号量
                                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE+skuInfoVo.getPromotionSessionId().toString()+"_"+skuId.toString());
                                    semaphore.trySetPermits(skuInfoVo.getSeckillCount());//库存作为信号量
                                }else{
                                    log.info("商品中已有信号量"); }
                            }
                    ); }
        );
    }
    public void getRelationEntites(){

    }

}
