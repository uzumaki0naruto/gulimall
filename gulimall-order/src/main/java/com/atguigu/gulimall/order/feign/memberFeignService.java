package com.atguigu.gulimall.order.feign;

import com.atguigu.gulimall.order.vo.MemberAddressVo;
import com.atguigu.gulimall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


import java.math.BigDecimal;
import java.util.List;

@FeignClient(value = "gulimall-member")
@Service
public interface memberFeignService {

    @GetMapping("member/memberreceiveaddress/{memberID}/address")
    public List<MemberAddressVo> getMemberAddressListById(@PathVariable(value = "memberID") Long memberId);

    @GetMapping("/getAllItems")
    public List<OrderItemVo> getCartItems();


}
