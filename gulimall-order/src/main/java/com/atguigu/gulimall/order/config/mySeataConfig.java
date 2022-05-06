package com.atguigu.gulimall.order.config;

import com.zaxxer.hikari.HikariDataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

//import static org.springframework.boot.autoconfigure.jdbc.DataSourceConfiguration.createDataSource;

@Configuration
public class mySeataConfig {

//    @Autowired
//    DataSourceProperties dataSourceProperties;
//
//    @Bean
//
//    public DataSource dataSource(DataSourceProperties properties){
//        HikariDataSource hikariDataSource = properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
//        if (StringUtils.hasText(properties.getName())) {
//            hikariDataSource.setPoolName(properties.getName());
//        }
//        return  new DataSourceProxy(hikariDataSource);
//
//    }
}
