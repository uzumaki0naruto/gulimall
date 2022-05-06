package com.atguigu.gulimall.product.config;


import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class myRedissionConfig {
    @Bean(destroyMethod = "shutdown")
    public  RedissonClient Redission(){
//        RedissonClient redisson = Redisson.create();
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.164.132:6379").setPassword("haohaoxue123");
        RedissonClient redisson = Redisson.create(config);
        return  redisson;
    }
}
