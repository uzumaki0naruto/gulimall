package com.example.gulimallsekill.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

@Service
@FeignClient(value = "gulimall-coupon")
public interface couponFeignService {
//    coupon/seckillsession/getSeckillLast3Day
    @GetMapping("coupon/seckillsession/getSeckillLast3Day")
    public R getSeckillLast3Day();
}
