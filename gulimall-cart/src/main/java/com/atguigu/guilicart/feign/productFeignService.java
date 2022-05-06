package com.atguigu.guilicart.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

@Service
@FeignClient(value = "gulimall-product")
public interface productFeignService {

    @GetMapping("product/skuinfo/{skuId}/price")
    public R getPrice(@PathVariable Long skuId);

    @RequestMapping("product/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);

    @GetMapping("product/skusaleattrvalue/StringList/{skuid}")
    public List<String> getSkuSaleAttrValue(@PathVariable long skuid);
}
