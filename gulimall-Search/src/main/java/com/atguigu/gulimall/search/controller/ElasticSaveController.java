package com.atguigu.gulimall.search.controller;


import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.BizCodeEnume;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.rmi.runtime.Log;

import java.io.IOException;
import java.util.List;

/**
 * @ClassName HuangXiangXiang
 * @Date 2020/9/8 23:04
 * @Version V1.0
 **/
@Slf4j
@RequestMapping("/search/save")
@RestController
public class ElasticSaveController {


    @Autowired
    private ProductSaveService productSaveService;

    //上架商品
    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModel) {
        boolean b = false;
        try {
            b = productSaveService.productStatusUp(skuEsModel);
        } catch (Exception e) {
            log.error("ElasticSaveController商品上架错误 {}", e);
            return R.error(BizCodeEnume.EXCEPTION.getCode(), BizCodeEnume.EXCEPTION.getMessage());
        }


        if (!b) {
            return R.ok();
        } else {
            return R.error(BizCodeEnume.EXCEPTION.getCode(), BizCodeEnume.EXCEPTION.getMessage());
        }
    }

}
