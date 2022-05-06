package com.atguigu.gateway;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
public class guliMain {

    public static void main(String[] args) {
//        System.setProperty("csp.sentinel.app.type", "1");
        SpringApplication.run(guliMain.class,args);
    }
}
