package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.login.Configuration;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName HuangXiangXiang
 * @Date 2020/9/8 23:12
 * @Version V1.0
 **/


@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    /**
     * ES操作Api
     */

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModel) throws IOException {

        //包存到es
        //1、给es中建立索引。product
        BulkRequest bulkRequest = new BulkRequest();

        for (SkuEsModel esModel : skuEsModel) {
            //设置这个索引为EsConstant.PRODUCT_INDEX
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            //构造json数据
            String s = JSON.toJSONString(esModel);
            //设置唯一id
            indexRequest.id(esModel.getSkuId().toString());
            //设置数据传json
            indexRequest.source(s, XContentType.JSON);
            bulkRequest.add(indexRequest);


        }
        //2、给es中保存这些数据
        //restHighLevelClient.index() 一个一个保存太慢了

        //使用批量保存bulk
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

        //TODO 如果批量有错
        boolean b = bulk.hasFailures();
        List<String> collect = Arrays.stream(bulk.getItems()).map(item -> {
            return item.getId();
        }).collect(Collectors.toList());

        log.info("商品上架完成：{}, 返回数据：", collect, bulk.toString());

        return b;
    }
}
