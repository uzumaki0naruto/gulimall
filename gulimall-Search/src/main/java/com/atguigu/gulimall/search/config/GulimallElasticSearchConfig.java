package com.atguigu.gulimall.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName HuangXiangXiang
 * @Date 2020/9/5 22:36
 * @Version V1.0
 **/
@Configuration
public class GulimallElasticSearchConfig {
// 官方配置、的分解操作
//    RestHighLevelClient client = new RestHighLevelClient(
//            RestClient.builder(
//                    new HttpHost("localhost", 9200, "http"),
//                    new HttpHost("localhost", 9201, "http")));


    public static final RequestOptions COMMON_OPTIONS;

    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
//        builder.addHeader("Authorization", "Bearer " + TOKEN);
//        builder.setHttpAsyncResponseConsumerFactory(
//                new HttpAsyncResponseConsumerFactory
//                        .HeapBufferedResponseConsumerFactory(30 * 1024 * 1024 * 1024));
        COMMON_OPTIONS = builder.build();
    }


    @Bean
    public RestHighLevelClient restHighLevelClient() {
        RestClientBuilder http = RestClient.builder(new HttpHost("192.168.7.120", 9200, "http"));
        return new RestHighLevelClient(http);

    }
}
