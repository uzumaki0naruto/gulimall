package com.atguigu.gulimall.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class MyThreadConfig {
    @Bean
    public ThreadPoolExecutor threadPoolExecutor(ThreadPoolConfigProperties threadPoolConfigProperties) {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                threadPoolConfigProperties.getCoreSize(),
                threadPoolConfigProperties.getMaxSize(),
                threadPoolConfigProperties.getKeepAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()) {
        };
        return threadPoolExecutor;

    }
}


//不在 ThreadPoolConfigProperties 里面加@Component 的时候选哟配置这个
//@ConfigurationProperties(ThreadPoolConfigProperties.class)
//@Configuration
//public class MyThreadConfig {
//
//    @Bean
//    public ThreadPoolExecutor threadPoolExecutor(ThreadPoolConfigProperties hxx) {
//        /**
//         * 1、核心线程数(系统内存100、调一个20)
//         * 2、最大核心数(200)
//         * 3、空闲线程剩多少给他关闭（10秒）
//         * 4、时间单位 秒
//         * 5、阻塞队列的长度限制一下(10w)
//         * 6、线程工厂使用默认的 Executors.defaultThreadFactory()
//         * 7、拒绝策略(抛弃) new ThreadPoolExecutor.AbortPolicy()
//         */
//        return new ThreadPoolExecutor(
//                hxx.getCoreSize(),
//                hxx.
//                hxx.getMaximumPoolSize(),
//                hxx.getKeepAliveTime(),
//                TimeUnit.SECONDS,
//                new LinkedBlockingDeque<>(100000),
//                Executors.defaultThreadFactory(),
//                new ThreadPoolExecutor.AbortPolicy()
//        );
//    }
