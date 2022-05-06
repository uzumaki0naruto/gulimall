package com.example.gulimallsekill.config;


import com.atguigu.common.utils.BizCodeEnume;
import com.atguigu.common.utils.R;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author 小坏
 * @Date 2021/3/4 15:53
 * @Version 1.0
 * @program: 父工程 gulimall 万物起源之地
 * 自定义 Sentinel 配置类、配置自定义类的返回
 * 请求被限制以后的我处理器
 */

@Configuration
public class SeckillSentinelConfig {

    /**
     * 请求被限制以后的我处理器
     * 自定义
     */
    public SeckillSentinelConfig() {

//        WebCallbackManager.setUrlBlockHandler (new UrlBlockHandler () {
//            @Override
//            public void blocked(HttpServletRequest request, HttpServletResponse response, BlockException e) throws IOException {
//                R error = R.error (BizCodeEnume.TO_MANY_REQUEST.getCode (), BizCodeEnume.TO_MANY_REQUEST.getMessage());
//                response.setCharacterEncoding ("UTF-8");
//                response.setContentType ("application/json");
//                response.getWriter ().write (JSON.toJSONString (error));
//            }
//        });
    }
}
