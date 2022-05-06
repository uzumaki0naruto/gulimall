package com.atguigu.guilicart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.common.utils.StringUtils;
import com.atguigu.common.utils.R;
import com.atguigu.guilicart.feign.productFeignService;
import com.atguigu.guilicart.interceptor.cartInterceptor;
import com.atguigu.guilicart.service.cartService;
import com.atguigu.guilicart.vo.SkuInfoVo;
import com.atguigu.guilicart.vo.UserInfoTo;
import com.atguigu.guilicart.vo.cart;
import com.atguigu.guilicart.vo.cartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
//extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity>

@Service
public class cartServiceImpl  implements cartService {



    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    productFeignService productFeignService;


    @Autowired
    ThreadPoolExecutor executor;

    private static String CART_PERFIX="gulimall:CART:";

    @Override
    public cartItem addtoCart(Long id, Integer number) throws ExecutionException, InterruptedException {
        BoundHashOperations<String,Object,Object> cartHashBound = getCartFmredis();
        String cs = (String) cartHashBound.get(id.toString());
//        redis中没有
        if(StringUtils.isEmpty(cs)){
            cartItem cartItem = new cartItem();
            R info =  productFeignService.info(id);

//        1.获取到商品详情信息
//        1.1 获取商品基本信息
//        1.2获取商品组合信息
            SkuInfoVo skuInfoVo = info.getData("skuInfo",new TypeReference<SkuInfoVo>(){});
//      购物车已有
            CompletableFuture<Void> getCartItemTask = CompletableFuture.runAsync(() -> {
                if (skuInfoVo != null) {
                    cartItem.setSkuId(id);
                    cartItem.setCheck(false);
                    cartItem.setDefaultImage(skuInfoVo.getSkuDefaultImg());
                    cartItem.setTitle(skuInfoVo.getSkuName());
                    cartItem.setTotalPrice(skuInfoVo.getPrice().multiply(new BigDecimal(skuInfoVo.getSaleCount())));
                    cartItem.setPrice(skuInfoVo.getPrice());
                    cartItem.setCount(number);
                }
            }, executor);
            CompletableFuture<Void> getSkuAttrTask = CompletableFuture.runAsync(() -> {
                List<String> skuSaleAttrValue = productFeignService.getSkuSaleAttrValue(skuInfoVo.getSkuId());
                cartItem.setSkuAttr(skuSaleAttrValue);
            }, executor);
            //2.获取缓存中的购物车
//        3.在购物车中加入商品信息,也就是放到缓存中,key为商品skuid,value为item
            CompletableFuture.allOf(getCartItemTask,getSkuAttrTask).get();
            String s = JSON.toJSONString(cartItem);
            cartHashBound.put(cartItem.getSkuId(),s);
            return cartItem;
        }else{
//            redis中存在
            cartItem item = JSON.parseObject(cs, cartItem.class);
            item.setCount(item.getCount()+number);
            cartHashBound.put(id.toString(),JSON.toJSONString(item));
            return item;
        }
    }

    @Override
    public cartItem getCartItem(Long skuid) {
        BoundHashOperations<String, Object, Object> cartFmredis =
                getCartFmredis();
        String s = (String) cartFmredis.get(skuid.toString());
        cartItem cartItem = JSON.parseObject("cartItem", cartItem.class);
        return cartItem;
    }

    /**
     *     没登录显示临时购物车
     *     登录了，则需要合并已登录购物车和临时购物车
     * @return
     */
    @Override
    public cart getCart() {
        UserInfoTo userInfoTo = cartInterceptor.threadLocal.get();
        Long userId = userInfoTo.getUserId();
        cart cart = new cart();
        if(userId!=null){
//              登入
//            1.获得临时用户的购物车
            List<cartItem> tempItems = getCartItems(CART_PERFIX+userInfoTo.getUserKey());
//            删除临时用户购物车
            clearCart(CART_PERFIX+userInfoTo.getUserKey());
//            2.获得已登入用户的购物车
            List<cartItem> cartItems = getCartItems(CART_PERFIX+userInfoTo.getUserId().toString());
            cartItems.addAll(tempItems);
            cart.setItems(cartItems);
        }else{
//            cart tempCart=new cart();
//            没登入，获取临时购物车的临时购物项
            List<cartItem> itemList = getCartItems(CART_PERFIX+userInfoTo.getUserKey());
            cart.setItems(itemList);
            return cart;
        }
        return null;
    }

    @Override
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }


    @Override
    public List<cartItem> getUserCarItems() {
        //登录成功访问过来是没用登录的、出现Feign远程调用丢失请求头问题
        UserInfoTo userInfoTo = cartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() == null) {
            return null;
        } else {
            String tempCartkey = CART_PERFIX+ userInfoTo.getUserId();
            List<cartItem> cartItems = getCartItems(tempCartkey);
            //获取所有被选中的购物项
            List<cartItem> collect = cartItems.stream()
                    .filter(item -> item.isCheck())
                    .map((item) -> {
                        //远程调用查出数据库的价格
                        R price = productFeignService.getPrice(item.getSkuId());
                        //TODO 更新为最新的价格
                        String data = (String) price.get("data");
                        item.setPrice(new BigDecimal(data));
                        return item;
                    }).collect(Collectors.toList());
            return collect;
        }

    }


    // 获得购物车的购物项
    private List<cartItem> getCartItems(String cartKey) {
        BoundHashOperations<String,Object,Object> tempUserCartBound = redisTemplate.boundHashOps(cartKey);
        List<Object> values = tempUserCartBound.values();
        List<cartItem> cartItemList = values.stream().map(cartItem -> {
            cartItem item = (com.atguigu.guilicart.vo.cartItem) JSON.parseObject((String) cartItem, cartItem.getClass());
            return item;
        }).collect(Collectors.toList());
     return  cartItemList;
    }

    private BoundHashOperations<String,Object,Object> getCartFmredis() {
        //        2.从缓存中获取购物车
//        2.1缓存中可能是临时用户也可能是已经登入用户
//        2.2判断是否登入
        UserInfoTo userInfoTo = cartInterceptor.threadLocal.get();
        String cartKey="";
        if(userInfoTo.getUserId()!=null){
            cartKey=CART_PERFIX+userInfoTo.getUserId();
        }else if(userInfoTo.getUserId()!=null){
            cartKey=CART_PERFIX+userInfoTo.getUserKey();
        }
        BoundHashOperations<String,Object,Object> cartBound= redisTemplate.boundHashOps(cartKey);
        return cartBound;
    }
}
