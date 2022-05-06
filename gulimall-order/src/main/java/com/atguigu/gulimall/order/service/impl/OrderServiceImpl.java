package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;

import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.SeckillOrderTo;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberResVo;
import com.atguigu.gulimall.order.constant.OrderConstant;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.feign.productFeignService;
import com.atguigu.gulimall.order.intercept.LoginUserInterceptor;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.atguigu.gulimall.order.to.orderCreateTo;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
//import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
    @Autowired
    com.atguigu.gulimall.order.feign.memberFeignService memberFeignService;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    OrderService orderService;

    @Autowired
    com.atguigu.gulimall.order.feign.wareFeignService wareFeignService;
    @Autowired
    com.atguigu.gulimall.order.feign.cartFeignService cartFeignService;

    @Autowired
    productFeignService productFeignService;

    private ThreadLocal<OrderSubmitVo>  submitVoThreadLocal=new ThreadLocal<>();

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 确认订单信息
     * @param memberResVoId
     * @return
     */
    @Override
    public OrderConfirmVo confirmOrder(Long memberResVoId) throws ExecutionException, InterruptedException {
        MemberResVo memberResVo = LoginUserInterceptor.threadLocal.get();
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
//          旧请求
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        //        获取收获地址
        CompletableFuture<Void> getAddressTask = CompletableFuture.runAsync(()->{
//          把旧请求放到新请求中
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> memberAddressVos = memberFeignService.getMemberAddressListById(memberResVoId);
            orderConfirmVo.setAddress(memberAddressVos);
        },threadPoolExecutor);
//          获取所有订单项
        CompletableFuture<Void> getCartItemsTask = CompletableFuture.runAsync(() -> {
            List<OrderItemVo> cartItems = memberFeignService.getCartItems();
            orderConfirmVo.setItems(cartItems);
        },threadPoolExecutor).thenRunAsync(()->{
//            检查是否有库存
            List<OrderItemVo> items = orderConfirmVo.getItems();
            List<Long> SkuIdList = items.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            R r = wareFeignService.hasStock(SkuIdList);
            List<SkuStockVo> data = r.getData(new TypeReference<List<SkuStockVo>>(){});
            Map<Long, Boolean> map = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
            orderConfirmVo.setStocks(map);
        },threadPoolExecutor);
//        TODO
//        防重令牌
        String token = UUID.randomUUID().toString().replace("-", ""); //给页面一个令牌
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX+memberResVo.getId()
                ,token,30, TimeUnit.MINUTES); //缓存中放置令牌,key为前缀加当前用户的id，value为令牌
        orderConfirmVo.setOrderToken(token);
//       获取积分
        Integer integration = memberResVo.getIntegration();
        orderConfirmVo.setIntegration(integration);
        //      等待这两个线程任务结束才进行下一步
        CompletableFuture.allOf(getAddressTask,getCartItemsTask).get();
        return orderConfirmVo;
    }
//        下单，去创建订单，验令牌，验价格，锁库存
    @Override
    @Transactional
//    @GlobalTransactional
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo orderSubmitVo) {
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        responseVo.setCode(0); //默认成功,有异常就改
        submitVoThreadLocal.set(orderSubmitVo);
        MemberResVo memberResVo = LoginUserInterceptor.threadLocal.get();

        String orderToken = orderSubmitVo.getOrderToken();
        String redisToken = (String) redisTemplate.opsForValue().get(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResVo.getId());
        //        对比验证和删令牌应该是原子性的，
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        Long result = (Long) redisTemplate.execute(
                new DefaultRedisScript(script,Long.class),   //param1:脚本, param2 返回值类型
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResVo.getId()), // KEYS[1],参数，用来从redis中get。需要是数组类型
                orderToken);            //ARGV[1] 需要被验证的参数
        if(result==0L){
//           令牌验证失败
            responseVo.setCode(1);
            return responseVo;
        }else{
//            验证成功
            responseVo.setCode(0);
            orderCreateTo orderCreateTo = createOrder();//创建订单项
//            TODO 验证价格
//         TODO 保存订单项[省略】,成功则锁库存,
            if(true){// 保存成功
//                锁定库存
                WareSkuLockVo lockVo = new WareSkuLockVo ();
                lockVo.setOrderSn(orderCreateTo.getOrder().getOrderSn());
                System.out.println();
                List<OrderItemVo> collect = orderCreateTo.getOrderItems ().stream ().map (item -> {
                    OrderItemVo itemVo = new OrderItemVo ();
                    itemVo.setSkuId (item.getSkuId ()); //商品
                    itemVo.setCount (item.getSkuQuantity ()); //要锁几件
                    itemVo.setTitle (item.getSkuName ()); //商品名字
                    return itemVo;
                }).collect (Collectors.toList ());
                lockVo.setLocks(collect);
//                远程锁库存
                R r = wareFeignService.lockOrder(lockVo);
                if(r.getCode()==0){
//                    锁定成功
                    responseVo.setOrder(orderCreateTo.getOrder());
//                    锁定成功要执行关闭订单
                    rabbitTemplate.convertAndSend (
                            "order-event-exchange",
                            "order.create.order",
                            orderCreateTo.getOrder ());

                    return responseVo;
                }else{
//                    锁定失败,状态码非0就是失败
                    responseVo.setCode(3);
                    return responseVo;
                }
            } else {
                responseVo.setCode (2); //2:金额对比失败
                return responseVo;
            }

        }
//        return responseVo;
    }

    @Override
    public void closeOrder(OrderEntity orderEntity) {
        Long id = orderEntity.getId();
        OrderEntity byId = orderService.getById(id); //获取实时的entity
        //只有待付款状态才可以关单
        if(byId.getStatus()==4){
            OrderEntity entity = new OrderEntity();
            BeanUtils.copyProperties(byId,entity);
            entity.setId(byId.getId());
            entity.setStatus(4);
            this.updateById(entity);
        }
        OrderTo orderTo = new OrderTo ();
        BeanUtils.copyProperties (orderEntity, orderTo);
        try {
            //方式一：  网络链接不上可以这马做
            //TODO 保证消息一定发出去，每一个消息都可以做好日志记录 (给数据库保存每一个消息的详细信息)
            //TODO 定期扫描数据库将失败的消息在发送一遍
            rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
        }catch (Exception e) {
            e.printStackTrace();
        }


//        } catch (Exception e) {
//            //TODO 将没法发送的成功的消息进行重试
////                    while（）循环
//
//            rabbitTemplate.convertAndSend ("order-event-exchange", "order.release.other", orderTo);
//        }


    }

    @Override
    public R createSeckillOrder(SeckillOrderTo seckillOrderTo) throws ExecutionException, InterruptedException {
        orderCreateTo orderCreateTo = new orderCreateTo();  //要创造的订单
        String orderSn=seckillOrderTo.getOrderSn();
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(orderSn);
        entity.setStatus(0);
        BigDecimal multiply = seckillOrderTo.getSeckillPrice().multiply(new BigDecimal(seckillOrderTo.getNum()));
        entity.setPromotionAmount(multiply);
        this.save(entity);
//        CompletableFuture<Void> saveOrderItemTask = CompletableFuture.runAsync(() -> {
            OrderItemEntity orderItemEntity = new OrderItemEntity();
            orderItemEntity.setOrderSn(orderSn);
            orderItemEntity.setOrderId(entity.getId());
            orderItemEntity.setRealAmount(multiply);
            R r = productFeignService.getSkuInfo(seckillOrderTo.getSkuId());
            SpuInfoVo dta = r.getData(new TypeReference<SpuInfoVo>() {
            });
            orderItemEntity.setSpuId(dta.getId());
            orderItemEntity.setSpuBrand(dta.getBrandId().toString());
            orderItemEntity.setSpuName(dta.getSpuName());
            orderItemEntity.setCategoryId(dta.getCatalogId());
            R info = productFeignService.info(seckillOrderTo.getSkuId());
            if (info != null) {
                SkuInfoVo data = info.getData(new TypeReference<SkuInfoVo>() {
                });
                orderItemEntity.setSkuName(data.getSkuTitle());
                orderItemEntity.setSkuPic(data.getSkuDefaultImg());
                orderItemEntity.setSkuPrice(data.getPrice());
                orderItemEntity.setSkuQuantity(data.getSaleCount().intValue());
            }
            orderItemService.save(orderItemEntity);
//        },threadPoolExecutor);
//        saveOrderItemTask.get();
        return R.ok();
    }

    //    创建订单
    private orderCreateTo createOrder(){
        orderCreateTo orderCreateTo = new orderCreateTo();  //要创造的订单
        OrderSubmitVo orderSubmitVo = this.submitVoThreadLocal.get();  //订单提交信息
        String orderSn = IdWorker.getTimeId();  //订单流水号
        OrderEntity orderEntity = new OrderEntity();//订单本身的信息
        orderEntity.setOrderSn(orderSn);  //设置订单号
//      获取运费信息
        R fare = wareFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo  fareResp = fare.getData(new TypeReference<FareVo>() {
        });
//        设置运费信息
        orderEntity.setFreightAmount(fareResp.getFare());
        orderEntity.setReceiverCity(fareResp.getAddress().getCity());
        orderEntity.setReceiverDetailAddress(fareResp.getAddress().getDetailAddress());
        orderEntity.setReceiverName(fareResp.getAddress().getName());
        orderEntity.setReceiverPhone(fareResp.getAddress().getPhone());
        orderEntity.setReceiverPostCode (fareResp.getAddress ().getPostCode ());
        orderEntity.setReceiverProvince (fareResp.getAddress ().getProvince ());
        orderEntity.setReceiverRegion (fareResp.getAddress ().getRegion ());
//        获取所有订单项,里面是根据用户id查找redis中的购物项
        List<OrderItemVo> currentUserCarItems = cartFeignService.getCurrentUserCarItems();
        if (currentUserCarItems != null && currentUserCarItems.size () > 0) {
            List<OrderItemEntity> collects = currentUserCarItems.stream ().map (carItem -> {
                //映射成订单信息
                OrderItemEntity itemEntity =  buildOrderItem (carItem);
//                itemEntity.setOrderSn (orderId);
                return itemEntity;
            }).collect (Collectors.toList ());
            orderCreateTo.setOrder(orderEntity);
            orderCreateTo.setOrderItems(collects);
            orderCreateTo.setFare(fareResp.getFare());

        }
        return orderCreateTo;
    }
    /**
     * 构建某一个订单项内容
     *TODO
     * @param cartItem
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity itemEntity = new OrderItemEntity ();
        //1、订单信息：订单号
        //2、商品的SPU信息
        Long skuId = cartItem.getSkuId ();
//        productFeignService.
//        R r = productFeignService.spuInfoBySkuId (skuId);
//        productFeignService
//        R r = productFeignService.getSkuInfo(skuId);
        R r=productFeignService.getSkuInfo(skuId);
        SpuInfoVo dta = r.getData (new TypeReference<SpuInfoVo> () {
        });
        itemEntity.setSpuId (dta.getId ());
        itemEntity.setSpuBrand (dta.getBrandId ().toString ());
        itemEntity.setSpuName (dta.getSpuName ());
        itemEntity.setCategoryId (dta.getCatalogId ());
        //3、商品的SKU
        itemEntity.setSkuId (cartItem.getSkuId ());
        itemEntity.setSkuName (cartItem.getTitle ());
        itemEntity.setSkuPic (cartItem.getImage ());
        itemEntity.setSkuPrice (cartItem.getPrice ());
        String s = StringUtils.collectionToDelimitedString (cartItem.getSkuAttr (), ";");
        itemEntity.setSkuAttrsVals (s);
        itemEntity.setSkuQuantity (cartItem.getCount ());
        //4、优惠信息不做
        //5、积分信息
        itemEntity.setGiftGrowth (cartItem.getPrice ().multiply (new BigDecimal (cartItem.getCount ().toString ())).intValue ());
        itemEntity.setGiftIntegration (cartItem.getPrice ().multiply (new BigDecimal (cartItem.getCount ().toString ())).intValue ());
        //6、订单项的价格信息.
        itemEntity.setPromotionAmount (new BigDecimal ("0"));
        itemEntity.setCouponAmount (new BigDecimal ("0"));
        itemEntity.setIntegrationAmount (new BigDecimal ("0"));
        //当前订单项的实际金额。总额-各种优惠
        BigDecimal orign = itemEntity.getSkuPrice ().multiply (new BigDecimal (itemEntity.getSkuQuantity ().toString ()));
        BigDecimal subtract = orign.subtract (itemEntity.getCouponAmount ())
                .subtract (itemEntity.getPromotionAmount ())
                .subtract (itemEntity.getIntegrationAmount ());
        itemEntity.setRealAmount (subtract); //实际金额
        return itemEntity;
    }

}