package com.atguigu.guilicart.controller;


import com.atguigu.common.utils.R;
import com.atguigu.guilicart.feign.productFeignService;
import com.atguigu.guilicart.interceptor.cartInterceptor;
import com.atguigu.guilicart.service.cartService;
import com.atguigu.guilicart.vo.UserInfoTo;
import com.atguigu.guilicart.vo.cart;
import com.atguigu.guilicart.vo.cartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Controller
public class CartController {
    @Autowired
    com.atguigu.guilicart.interceptor.cartInterceptor cartInterceptor;


    @Autowired
    productFeignService productFeignService;

    @Autowired
    cartService cartService;

    /**
     *
     *
     * 浏览器有一个cookie ：user-keys 标识
     * 如果第一次使用jd的购物车功能，jd会给用户发送一个临时用户cookie
     * 浏览器保存后，以后每次访问都会带上这个cookie
     *
     * 登入：session有用户session
     * 没登入：按照cookie里面带来的user-key
     * 如果没有临时用户就创建一个临时用户
     *
     * 缓存到redis中
     * 已经登入的key 为 prifix+userid  ， hashkey 为 skuid  value为cartitem详情
     * 没登入的key 为  prifix+userkey ， hashkey为 skuid  value为cartItem详情
     *
     *
     * 以上都在拦截器中做完了，显示页面也要判断是否登入、
     * 没登录显示临时购物车
     * 登录了，则需要合并已登录购物车和临时购物车
     *
     * 获取整个购物车功能
     *
    */

    @GetMapping("/cart")
    public String cart(Model model){
        UserInfoTo userInfoTo = com.atguigu.guilicart.interceptor.cartInterceptor.threadLocal.get();
        cart cart= cartService.getCart();
        model.addAttribute("cart",cart);
        return "cartList";
    }
    /*
    添加商品到跳转成功页
     */
    @GetMapping("/addToCart")
    public String addCart(Model model, @RequestParam Long skuid,
                          @RequestParam Integer number, RedirectAttributes redirectAttributes
                          ) throws ExecutionException, InterruptedException {
         cartItem cartItem= cartService.addtoCart(skuid,number);
//        model.addAttribute("Skuid",skuid);
        redirectAttributes.addAttribute("skuid",skuid);
        return "redict:http://cart.gulimall.com:90/addToCartSuccess.html";
    }
    /*
    跳转成功页，只用查，不用添加，防止了重复提交
     */
    @GetMapping("/addToCartSuccess.html")
    public String addCartSuccess(Model model,@RequestParam(value = "skuid") Long skuid
                                ) throws ExecutionException, InterruptedException {

        cartItem cartItem= cartService.getCartItem(skuid);
        model.addAttribute("item",cartItem);
        return "success";
    }




    @GetMapping("/2")
    public String index22(Model model){
        model.addAttribute("list", Arrays.asList("sekiro","darksour","Bloodborne"));
        return "cartList";
    }


    @ResponseBody
    @GetMapping("/getAllItems")
    public List<cartItem> getCartItems(){
        System.out.println();
        cart cart = cartService.getCart();
        List<cartItem> items = cart.getItems();
        items.stream().filter(cartItem -> cartItem.isCheck()).map(cartItem -> {
                    R price = productFeignService.getPrice(cartItem.getSkuId());
                    BigDecimal data = (BigDecimal) price.get("data");
                    cartItem.setPrice(data);//  更新实时价格
            return cartItem;
        }
        ).collect(Collectors.toList());
        return items;
    }
    @GetMapping("/currentUserCarItems")
    @ResponseBody
    public List<cartItem> getCurrentUserCarItems() {

        return cartService.getUserCarItems();
    }
}
