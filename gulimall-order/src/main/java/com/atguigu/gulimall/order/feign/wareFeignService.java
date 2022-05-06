package com.atguigu.gulimall.order.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("gulimall-ware")
public interface wareFeignService {

    @GetMapping("ware/waresku/hasStock")
    public R hasStock(List<Long> skuIdList);

    @GetMapping("ware/wareinfo/fare")
    public R getFare(@RequestParam("addrId") Long addrId);

    @GetMapping("ware/waresku/lock/order")
    public R lockOrder(WareSkuLockVo wareSkuLockVo);

    }
