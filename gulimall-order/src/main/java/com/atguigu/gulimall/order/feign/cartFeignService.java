package com.atguigu.gulimall.order.feign;

import com.atguigu.gulimall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Service
@FeignClient("gulimall-cart")
public interface cartFeignService{
    //远程调用获取购物项
    @GetMapping("/currentUserCarItems")
    List<OrderItemVo> getCurrentUserCarItems() ;
}
