package com.atguigu.authService.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author 小坏
 * @Date 2021/1/11 22:14
 * @Version 1.0
 * @program: 父工程 gulimall 万物起源之地
 * WebMvcConfigurer  代替CONTROLLER 转发跳转页面
 */

@Configuration
public class GulimallWebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
//        registry.addViewController("/").setViewName("login"); //任务130：230、商城业务-认证服务-页面效果完成
        registry.addViewController("/reg.html").setViewName("reg");
    }
}
