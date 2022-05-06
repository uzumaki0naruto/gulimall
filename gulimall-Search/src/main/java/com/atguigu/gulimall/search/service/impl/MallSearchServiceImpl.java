package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.feign.ProductFeignService;
import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.AttrResponseVo;
import com.atguigu.gulimall.search.vo.BrandVo;
import com.atguigu.gulimall.search.vo.SearResult;
import com.atguigu.gulimall.search.vo.SearchParam;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author 小坏
 * @Date 2020/11/20 9:49
 * @Version 1.0
 * @program: 父工程 gulimall 万物起源之地
 */


@Service
public class MallSearchServiceImpl implements MallSearchService {

    //ES API

    @Autowired
    private RestHighLevelClient client;


    @Autowired
    private ProductFeignService productFeignService;


    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        list.add("1_500");

        list.forEach(e -> {
            String[] s = e.split("_");
            System.out.println(s[0] + s[1]);
        });
    }

    /**
     * 去ES进行检索
     *
     * @param param
     * @return
     */
    @Override
    public SearResult search(SearchParam param) {
        SearResult result = null;
        //1、new一个ES的 SearchRequest、准备检索请求
        SearchRequest searchRequest = buildSearchRequest(param);
        //动态构建出查询需要的DSL语句
        try {
            //2、执行检索请求
            SearchResponse response = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

            //3、分析响应数据封装成我们需要的格式
            result = buildSearchResult(response, param);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 1、准备检索请求
     * <p>
     * 模糊匹配，过滤 (按照属性，分类，品牌，价格区间、库存)  排序、分页、高亮、聚合分析
     * <p>
     * 根据 json 格式套 、大的套小的
     *
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam param) {

        /**
         * 1、构建DLS语句
         *           整个检索请求对象
         */
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();


        /**
         * 2、模糊匹配，过滤 (按照属性，分类，品牌，价格区间、库存)
         *    QueryBuilders是一个工具类帮我们构建出 QueryBuilder
         *    queryBuilder是我们整个queryBuilder构建的内容
         *
         *    构建 boolQuery
         */
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        //1、1 must -模糊匹配
        if (!StringUtils.isEmpty(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }

        //1.2 filter  按照三级分类id查询
        if (param.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }


        //1.2 filter  按照品牌id查询
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }

        /**
         * 1.2 filter  按照所有指定的属性进行查询
         * 1、构建嵌入式的Query
         * 2、ScoreMode.None  不让参与评分
         * 3、attrStr =1_5寸：8寸 & attrStr=2_16G：8G
         *       截取获取相应的key , value
         *
         * 4、 构建 BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
         *
         * 5、构建 must 下的参数
         *       nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrId", attrId));
         *                 nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.values", attrValues));
         *                 nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.values", attrValues));
         */
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            for (String attrStr : param.getAttrs()) {
                //attrStr =1_5寸：8寸 & attrStr=2_16G：8G
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();

                String[] s = attrStr.split("_");
                String attrId = s[0];    //检索属性id
                String[] attrValues = s[1].split(":"); //检索属性用的值

                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));

                //每一个都生成一个 nested 查询
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }


        if (param.getHasStock() != null) {
            //1.2 filter  按照库存是否有进行查询hasStock
            boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }


        //1.2 filter  按照价格区间
        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            /**
             * 1_500 / _500 /  500_
             *        截取param.getSkuPrice() 的价格、做对应的操作
             */

            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");

            String[] s = param.getSkuPrice().split("_");
            if (s.length == 2) {
                //区间
                rangeQuery.gte(s[0]).lte(s[1]);
            } else if (s.length == 1) {
                if (param.getSkuPrice().startsWith("_")) {
                    rangeQuery.lte(s[0]);
                }

                if (param.getSkuPrice().endsWith("_")) {
                    rangeQuery.gte(s[0]);
                }
            }
            boolQuery.filter(rangeQuery);
        }

        /**
         * 3、构建了请求数据第一个 query
         *   查看 resources 下的 dsl.json
         *   把以前所有条件都拿来进行封装
         */
        sourceBuilder.query(boolQuery);


        /**
         * 排序、分页、高亮
         *  构建 sourceBuilder
         */
        //2.1 排序

        if (!StringUtils.isEmpty(param.getSort())) {
            String sort = param.getSort();
            /**
             * 分析 ：
             *       sort = hotScore_asc/desc
             *    截取分割
             *       hotScore 排序字段
             *       asc/desc 升降序字段
             *
             *  截取之后
             *           s[0] 排序字段
             *           s[1] 升降序
             *
             */

            String[] s = sort.split("_");

            //1、equalsIgnoreCase 不区分大小写
            SortOrder asc = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            //2、排序使用
            sourceBuilder.sort(s[0], asc);

        }


        /**
         * 2.2 分页  pageSize: 5
         *  pageNum: 1  from:0 size:5 [0,1,2,3,4]
         *  pageNum: 2  from:5   size:5
         *  form = (pageNum - 1) * size
         */
        sourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);


        /**
         * 2.3 高亮 构建
         * 需要高亮的字段
         *      builder.field("skuTitle");
         *
         * 高亮的标签
         *      builder.preTags("<b style = 'color: red' >");
         */
        if (!StringUtils.isEmpty(param.getKeyword())) {
            HighlightBuilder builder = new HighlightBuilder();
            builder.field("skuTitle");
            builder.preTags("<b style = 'color: red' >");
            builder.postTags("</b>");
            sourceBuilder.highlighter(builder);
        }


        /**
         * 聚合分析
         */
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);
        //1、品牌聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        // TODO 1、聚合品牌信息
        sourceBuilder.aggregation(brand_agg);

        //2、分类聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        // TODO 1、聚合分类信息
        sourceBuilder.aggregation(catalog_agg);

        //3、属性聚合 attr_agg
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        //聚合出当前所有的attrId
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        //聚合分析出当前attr_id 对应的名字
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));


        //聚合分析出当前attr_id对应的所有可能的属性值attrValue
        /**
         * 查到的商品可能涉及到多个值所以 size 50
         */
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attr_agg.subAggregation(attr_id_agg);
        // TODO 1、聚合属性信息
        sourceBuilder.aggregation(attr_agg);

        String s = sourceBuilder.toString();
        System.out.println("构建的 DSL" + s);
        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);

        return searchRequest;
    }

    /**
     * 构建结果数据
     *
     * @param response
     * @return
     */
    private SearResult buildSearchResult(SearchResponse response, SearchParam param) {
        SearResult result = new SearResult();

        //1、返回的所有商品封装
        SearchHits hits = response.getHits();
        List<SkuEsModel> list = new ArrayList<>();
        if (hits.getHits() != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel skuEsModel = JSONObject.parseObject(sourceAsString, SkuEsModel.class);
                if (!StringUtils.isEmpty(param.getKeyword())) {
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String string = skuTitle.getFragments()[0].string();
                    skuEsModel.setSkuTitle(string);
                }
                list.add(skuEsModel);
            }
        }
        result.setProducts(list);


        //2、当前所有商品涉及到的所有属性信息
        List<SearResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");

        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            SearResult.AttrVo attrVo = new SearResult.AttrVo();
            long attrId = bucket.getKeyAsNumber().longValue();

            String attrName = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();

            //值可能有多个
            List<String> attrValues = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(e -> {
                String keyAsString = ((Terms.Bucket) e).getKeyAsString();
                return keyAsString;
            }).collect(Collectors.toList());
            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attrName);
            attrVo.setAttrValue(attrValues);


            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);

        //3、当前所有商品涉及到的所有品牌信息
        List<SearResult.BrandVo> brandVos = new ArrayList<>();
        //Aggregation
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            SearResult.BrandVo brandVo = new SearResult.BrandVo();
            //1、得到品牌id
            long brandId = bucket.getKeyAsNumber().longValue();
            //2、得到品牌名
            String brandName = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
            //3、得到名牌图片
            String brandImg = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();

            brandVo.setBrandId(brandId);
            brandVo.setBrandName(brandName);
            brandVo.setBrandImg(brandImg);
            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);

        //4、当前所有商品涉及到的所有分类信息
        //Aggregation
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        List<SearResult.CatalogVo> catalogVos = new ArrayList<>();
        List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearResult.CatalogVo catalogVo = new SearResult.CatalogVo();
            //得到分类id
            String keyAsString = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(keyAsString));

            //得到分类名
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            String catalog_name = catalog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalog_name);
            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);

//   ===========以上聚合信息中获取========================

        result.setPageNum(param.getPageNum());
        //分页信息-页码
        long total = hits.getTotalHits().value;

        result.setTotal(total);
        //分页信息总页码
        int totalPages = (int) total % EsConstant.PRODUCT_PAGESIZE == 0 ? (int) total / EsConstant.PRODUCT_PAGESIZE : ((int) total / EsConstant.PRODUCT_PAGESIZE + 1);
        result.setTotalPages(totalPages);

        //导航页码
        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);


        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            //6、构建面包导航数据
            List<SearResult.NavVo> collect = param.getAttrs().stream().map(attr -> {
                SearResult.NavVo navVo = new SearResult.NavVo();
                //attrs=2_5存：6存
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                R r = productFeignService.attrInfo(Long.parseLong(s[0]));
                result.getAttrIds().add(Long.parseLong(s[0]));
                if (r.getCode() == 0) {

                    AttrResponseVo data = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setNavName(data.getAttrName());
                } else {
                    navVo.setNavName(s[0]);
                }
                //取消了面包屑以后，我们要跳转到那个地方、将请求地址URL里面的当前制空
                //拿到所有的查询条件、去掉当前、
                //attrs = 15_海思（Hisilicon）
                String replace = replaceString(param, attr, "attrs");
                navVo.setLink("http://search.gulimall.com/list.html?" + replace);
                return navVo;
            }).collect(Collectors.toList());
            result.setNavs(collect);

            /**
             * 品牌分类
             * 如果属性预先有它生成的把 对象里面new 的覆盖、如果没有用空的这个list
             */
            if (param.getBrandId() != null && param.getBrandId().size() > 0) {
                List<SearResult.NavVo> navs = result.getNavs();
                SearResult.NavVo navVo = new SearResult.NavVo();
                navVo.setNavName("品牌");
                //TODO 远程查询所有品牌
                R r = productFeignService.brandInfo(param.getBrandId());
                if (r.getCode() == 0) {
                    List<BrandVo> brand = r.getData("brand", new TypeReference<List<BrandVo>>() {
                    });

                    StringBuffer buffer = new StringBuffer();
                    String replace = "";
                    for (BrandVo brandVo : brand) {
                        buffer.append(brandVo.getBrandName() + ";");
                        replace = replaceString(param, brandVo.getBrandId() + "", "brandId");
                    }
                    navVo.setNavValue(buffer.toString());
                    navVo.setLink("http://search.gulimall.com/list.html?" + replace);
                }
                navs.add(navVo);
            }
//TODO  分类：不需要导航取消


        }
        return result;
    }

    private String replaceString(SearchParam param, String value, String key) {
        String encode = null;
        try {
            encode = URLEncoder.encode(value, "UTF-8");
            //浏览器对空格编码和java不一样
            encode = encode.replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return param.get_queryString().replace("&" + key + "=" + encode, "");
    }


}
