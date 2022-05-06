package com.atguigu.gulimall_third_party.oss.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall_third_party.oss.utils.ConstantPropertiesUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class testController {

    @Value(value = "${third.name}")
    private String a;


    @Value(value = "${aliyun.oss.file.endpoint}")
    private String end;

}
