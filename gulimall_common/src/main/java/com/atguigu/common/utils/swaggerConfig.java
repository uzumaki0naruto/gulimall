package com.atguigu.common.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;

@Configuration
@EnableSwagger2
public class swaggerConfig {

    public static final Contact DEFAULT_CONTACT =
            new Contact("吴嘉伟", "http://wujiawei.com", "632906889@qq.com");
    //    配置了Swagger的docket实例
    @Bean
    public Docket docket(){
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo());
    }
    public ApiInfo apiInfo(){
        return new ApiInfo("webApi", //title
                "描述",  //描述名字
                "1.0", //版本号
                "urn:tos", //组织名字
                DEFAULT_CONTACT,
                "Apache 2.0", //开源版本号
                "http://www.apache.org/licenses/LICENSE-2.0",
                new ArrayList());
    }
}
