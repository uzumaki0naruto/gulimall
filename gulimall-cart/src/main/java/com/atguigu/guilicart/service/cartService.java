package com.atguigu.guilicart.service;

import com.atguigu.guilicart.vo.cart;
import com.atguigu.guilicart.vo.cartItem;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

//extends IService<cartItem>
@Service
public interface cartService  {
    cartItem addtoCart(Long id, Integer number) throws ExecutionException, InterruptedException;

    cartItem getCartItem(Long skuid);

    cart getCart();

    void clearCart(String cartKey);

    List<cartItem> getUserCarItems();
}
