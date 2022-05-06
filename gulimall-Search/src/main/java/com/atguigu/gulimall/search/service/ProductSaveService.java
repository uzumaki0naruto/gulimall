package com.atguigu.gulimall.search.service;

import com.atguigu.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @ClassName HuangXiangXiang
 * @Date 2020/9/8 23:09
 * @Version V1.0
 **/
public interface ProductSaveService {
    public boolean productStatusUp(List<SkuEsModel> skuEsModel) throws IOException;
}
