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
     * ??????????????????
     * @param memberResVoId
     * @return
     */
    @Override
    public OrderConfirmVo confirmOrder(Long memberResVoId) throws ExecutionException, InterruptedException {
        MemberResVo memberResVo = LoginUserInterceptor.threadLocal.get();
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
//          ?????????
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        //        ??????????????????
        CompletableFuture<Void> getAddressTask = CompletableFuture.runAsync(()->{
//          ??????????????????????????????
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> memberAddressVos = memberFeignService.getMemberAddressListById(memberResVoId);
            orderConfirmVo.setAddress(memberAddressVos);
        },threadPoolExecutor);
//          ?????????????????????
        CompletableFuture<Void> getCartItemsTask = CompletableFuture.runAsync(() -> {
            List<OrderItemVo> cartItems = memberFeignService.getCartItems();
            orderConfirmVo.setItems(cartItems);
        },threadPoolExecutor).thenRunAsync(()->{
//            ?????????????????????
            List<OrderItemVo> items = orderConfirmVo.getItems();
            List<Long> SkuIdList = items.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            R r = wareFeignService.hasStock(SkuIdList);
            List<SkuStockVo> data = r.getData(new TypeReference<List<SkuStockVo>>(){});
            Map<Long, Boolean> map = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
            orderConfirmVo.setStocks(map);
        },threadPoolExecutor);
//        TODO
//        ????????????
        String token = UUID.randomUUID().toString().replace("-", ""); //?????????????????????
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX+memberResVo.getId()
                ,token,30, TimeUnit.MINUTES); //?????????????????????,key???????????????????????????id???value?????????
        orderConfirmVo.setOrderToken(token);
//       ????????????
        Integer integration = memberResVo.getIntegration();
        orderConfirmVo.setIntegration(integration);
        //      ???????????????????????????????????????????????????
        CompletableFuture.allOf(getAddressTask,getCartItemsTask).get();
        return orderConfirmVo;
    }
//        ????????????????????????????????????????????????????????????
    @Override
    @Transactional
//    @GlobalTransactional
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo orderSubmitVo) {
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        responseVo.setCode(0); //????????????,???????????????
        submitVoThreadLocal.set(orderSubmitVo);
        MemberResVo memberResVo = LoginUserInterceptor.threadLocal.get();

        String orderToken = orderSubmitVo.getOrderToken();
        String redisToken = (String) redisTemplate.opsForValue().get(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResVo.getId());
        //        ????????????????????????????????????????????????
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        Long result = (Long) redisTemplate.execute(
                new DefaultRedisScript(script,Long.class),   //param1:??????, param2 ???????????????
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResVo.getId()), // KEYS[1],??????????????????redis???get????????????????????????
                orderToken);            //ARGV[1] ????????????????????????
        if(result==0L){
//           ??????????????????
            responseVo.setCode(1);
            return responseVo;
        }else{
//            ????????????
            responseVo.setCode(0);
            orderCreateTo orderCreateTo = createOrder();//???????????????
//            TODO ????????????
//         TODO ???????????????[?????????,??????????????????,
            if(true){// ????????????
//                ????????????
                WareSkuLockVo lockVo = new WareSkuLockVo ();
                lockVo.setOrderSn(orderCreateTo.getOrder().getOrderSn());
                System.out.println();
                List<OrderItemVo> collect = orderCreateTo.getOrderItems ().stream ().map (item -> {
                    OrderItemVo itemVo = new OrderItemVo ();
                    itemVo.setSkuId (item.getSkuId ()); //??????
                    itemVo.setCount (item.getSkuQuantity ()); //????????????
                    itemVo.setTitle (item.getSkuName ()); //????????????
                    return itemVo;
                }).collect (Collectors.toList ());
                lockVo.setLocks(collect);
//                ???????????????
                R r = wareFeignService.lockOrder(lockVo);
                if(r.getCode()==0){
//                    ????????????
                    responseVo.setOrder(orderCreateTo.getOrder());
//                    ?????????????????????????????????
                    rabbitTemplate.convertAndSend (
                            "order-event-exchange",
                            "order.create.order",
                            orderCreateTo.getOrder ());

                    return responseVo;
                }else{
//                    ????????????,????????????0????????????
                    responseVo.setCode(3);
                    return responseVo;
                }
            } else {
                responseVo.setCode (2); //2:??????????????????
                return responseVo;
            }

        }
//        return responseVo;
    }

    @Override
    public void closeOrder(OrderEntity orderEntity) {
        Long id = orderEntity.getId();
        OrderEntity byId = orderService.getById(id); //???????????????entity
        //????????????????????????????????????
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
            //????????????  ?????????????????????????????????
            //TODO ???????????????????????????????????????????????????????????????????????? (????????????????????????????????????????????????)
            //TODO ??????????????????????????????????????????????????????
            rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
        }catch (Exception e) {
            e.printStackTrace();
        }


//        } catch (Exception e) {
//            //TODO ?????????????????????????????????????????????
////                    while????????????
//
//            rabbitTemplate.convertAndSend ("order-event-exchange", "order.release.other", orderTo);
//        }


    }

    @Override
    public R createSeckillOrder(SeckillOrderTo seckillOrderTo) throws ExecutionException, InterruptedException {
        orderCreateTo orderCreateTo = new orderCreateTo();  //??????????????????
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

    //    ????????????
    private orderCreateTo createOrder(){
        orderCreateTo orderCreateTo = new orderCreateTo();  //??????????????????
        OrderSubmitVo orderSubmitVo = this.submitVoThreadLocal.get();  //??????????????????
        String orderSn = IdWorker.getTimeId();  //???????????????
        OrderEntity orderEntity = new OrderEntity();//?????????????????????
        orderEntity.setOrderSn(orderSn);  //???????????????
//      ??????????????????
        R fare = wareFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo  fareResp = fare.getData(new TypeReference<FareVo>() {
        });
//        ??????????????????
        orderEntity.setFreightAmount(fareResp.getFare());
        orderEntity.setReceiverCity(fareResp.getAddress().getCity());
        orderEntity.setReceiverDetailAddress(fareResp.getAddress().getDetailAddress());
        orderEntity.setReceiverName(fareResp.getAddress().getName());
        orderEntity.setReceiverPhone(fareResp.getAddress().getPhone());
        orderEntity.setReceiverPostCode (fareResp.getAddress ().getPostCode ());
        orderEntity.setReceiverProvince (fareResp.getAddress ().getProvince ());
        orderEntity.setReceiverRegion (fareResp.getAddress ().getRegion ());
//        ?????????????????????,?????????????????????id??????redis???????????????
        List<OrderItemVo> currentUserCarItems = cartFeignService.getCurrentUserCarItems();
        if (currentUserCarItems != null && currentUserCarItems.size () > 0) {
            List<OrderItemEntity> collects = currentUserCarItems.stream ().map (carItem -> {
                //?????????????????????
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
     * ??????????????????????????????
     *TODO
     * @param cartItem
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity itemEntity = new OrderItemEntity ();
        //1???????????????????????????
        //2????????????SPU??????
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
        //3????????????SKU
        itemEntity.setSkuId (cartItem.getSkuId ());
        itemEntity.setSkuName (cartItem.getTitle ());
        itemEntity.setSkuPic (cartItem.getImage ());
        itemEntity.setSkuPrice (cartItem.getPrice ());
        String s = StringUtils.collectionToDelimitedString (cartItem.getSkuAttr (), ";");
        itemEntity.setSkuAttrsVals (s);
        itemEntity.setSkuQuantity (cartItem.getCount ());
        //4?????????????????????
        //5???????????????
        itemEntity.setGiftGrowth (cartItem.getPrice ().multiply (new BigDecimal (cartItem.getCount ().toString ())).intValue ());
        itemEntity.setGiftIntegration (cartItem.getPrice ().multiply (new BigDecimal (cartItem.getCount ().toString ())).intValue ());
        //6???????????????????????????.
        itemEntity.setPromotionAmount (new BigDecimal ("0"));
        itemEntity.setCouponAmount (new BigDecimal ("0"));
        itemEntity.setIntegrationAmount (new BigDecimal ("0"));
        //???????????????????????????????????????-????????????
        BigDecimal orign = itemEntity.getSkuPrice ().multiply (new BigDecimal (itemEntity.getSkuQuantity ().toString ()));
        BigDecimal subtract = orign.subtract (itemEntity.getCouponAmount ())
                .subtract (itemEntity.getPromotionAmount ())
                .subtract (itemEntity.getIntegrationAmount ());
        itemEntity.setRealAmount (subtract); //????????????
        return itemEntity;
    }

}