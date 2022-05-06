package com.atguigu.authService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients

@EnableRedisHttpSession
/* 核心原理
         *  1)、@EnableRedisHttpSession 导入了 @RedisHttpSessionConfiguration配置
 *      1、给容器中加了一个组件(R相当于存session的库)
         *            SessionRepository=>>>。【RedisOperationsSessionRepository】==》(相当于ridis操作session的dao)  session的增删改查封装类
         *      2、SessionRepositoryFilter==》Filter: session存储的过滤器;每个请求都必须经过filter
         *           1、创建的时候。就自动从容器中获取到了SessionRepository
         *           2、原始的request,request都被包装了SessionRepositoryRequestWrapper，SessionRepositoryResponseWrapper
         *           3、以后获取session。request.getSession()
         *           4、wrappedRequest.getSession(); ==> SessionRepository 中获取到的
         *
         *     还有：自动延期：redis中的数据也有过期时间
         *
         */
public class AuthMainClass {
    public static void main(String[] args) {
        SpringApplication.run(AuthMainClass.class,args);
    }
}
