package com.atguigu.gulimall.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;


/**
 * @ClassName HuangXiangXiang
 * @Date 2020/9/5 22:39
 * @Version V1.0
 **/

@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallSearchApp {


    public static void main(String[] args) {
        SpringApplication.run(GulimallSearchApp.class, args);
    }
}
