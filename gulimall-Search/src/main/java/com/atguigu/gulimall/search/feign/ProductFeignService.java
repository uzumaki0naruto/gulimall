package com.atguigu.gulimall.search.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @Author 小坏
 * @Date 2020/12/19 11:49
 * @Version 1.0
 * @program: 父工程 gulimall 万物起源之地
 */

@FeignClient("gulimall-product")
public interface ProductFeignService {

    @GetMapping("/product/attr/info/{attrId}")
    public R attrInfo(@PathVariable("attrId") Long attrId);


    @RequestMapping("/product/brand/infos")
    public R brandInfo(@RequestParam("brandIds") List<Long> brandIds);
}
