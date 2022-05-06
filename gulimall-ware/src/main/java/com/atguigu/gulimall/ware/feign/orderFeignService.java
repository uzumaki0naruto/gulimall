package com.atguigu.gulimall.ware.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Service
@FeignClient(value = "guliamll-order")
public interface orderFeignService {


    @GetMapping("order/order/getOrder/{orderSn}")
    public R getOrderBysn(@PathVariable(value = "orderSn") String orderSn);

}
