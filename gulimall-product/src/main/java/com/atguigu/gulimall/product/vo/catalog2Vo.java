package com.atguigu.gulimall.product.vo;

import com.sun.org.apache.xml.internal.resolver.Catalog;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


/*
总的json模型是
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class catalog2Vo {

    private     String catalog1Id;  //1级父分类id
    private    List<Catalog3Vo> catalog3List;  //三级子分类
    private   String id;
    private    String name;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Catalog3Vo {
        private    String id;
        private    String catalog2Id;  //2级父分类id
        private    String name;


    }





}
