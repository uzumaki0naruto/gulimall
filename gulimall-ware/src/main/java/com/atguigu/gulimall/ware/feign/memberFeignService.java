package com.atguigu.gulimall.ware.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(value = "gulimall-member")
@Service
public interface memberFeignService {
    @RequestMapping("member/memberreceiveaddress/info/{id}")
   R info(@PathVariable("id") Long id);
}
