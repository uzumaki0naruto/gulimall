package com.atguigu.guilicart.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class guliFeignConfig {

    @Bean(value = "RequestInterceptor")
    public RequestInterceptor requestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
//                requestTemplate就是新请求
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes)
                        RequestContextHolder.getRequestAttributes();
//                老请求
                HttpServletRequest request = requestAttributes.getRequest();
//                获取cookie
                String cookie = request.getHeader("Cookie");
//                同步到新请求
                requestTemplate.header("Cookie",cookie);
            }
        };

    }
}
