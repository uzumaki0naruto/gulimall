package com.atguigu.authService.Feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Service
@FeignClient(value ="thirdParty")
public interface thirdPartyFeignService {
    @GetMapping("/sendCode/{email}")
    public String sendMail(@PathVariable String email);

}
