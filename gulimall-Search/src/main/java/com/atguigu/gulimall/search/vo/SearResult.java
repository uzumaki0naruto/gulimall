package com.atguigu.gulimall.search.vo;

import com.atguigu.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author 小坏
 * @Date 2020/11/20 10:45
 * @Version 1.0
 * @program: 父工程 gulimall 万物起源之地
 */


@Data
public class SearResult {

    //查询到所有商品的信息
    //所有的商品信息都在这、其他的在下面
    private List<SkuEsModel> products;

    /**
     * 一下是分页信息
     */
    private Integer pageNum; //当前页码
    private Long total; //总记录数
    private Integer totalPages; //总页码

    private List<Integer> pageNavs; //导航页码

    private List<BrandVo> brands; //当前查询到的结果、所有涉及到的品牌
    private List<CatalogVo> catalogs; //当前查询到的结果、所有涉及到的分类
    private List<AttrVo> attrs; //当前查询到的结果、所有涉及到的属性


    //================以上是返回给页面的所有信息===================================


    //面包屑导航数据
    private List<NavVo> navs = new ArrayList<>();

    private List<Long> attrIds = new ArrayList<>();

    @Data
    public static class NavVo {
        private String navName;
        private String navValue;
        private String link;
    }


    //品牌
    @Data
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    //分类
    @Data
    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
    }

    //属性
    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }
}
