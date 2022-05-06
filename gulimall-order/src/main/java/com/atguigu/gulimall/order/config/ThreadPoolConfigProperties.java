package com.atguigu.gulimall.order.config;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "gulimall.thread")
public class ThreadPoolConfigProperties  {
//    @Value("${core-size}")
    private Integer coreSize;
//    @Value("${max-size}")
    private Integer maxSize;
//    @Value("${keep-alive-time}")
    private Integer keepAliveTime;


//    public static Integer CORE_SIZE;
//
//    public static Integer MAX_SIZE;
//
//    public static Integer KEEP_ALIVE_TIME;
//
//    @Override
//    public void afterPropertiesSet() throws Exception {
//        this.CORE_SIZE=coreSize;
//        this.MAX_SIZE=maxSize;
//        this.KEEP_ALIVE_TIME=keepAliveTime;
//    }
}
