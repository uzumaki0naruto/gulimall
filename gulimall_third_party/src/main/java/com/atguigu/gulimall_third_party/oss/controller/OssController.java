package com.atguigu.gulimall_third_party.oss.controller;


import com.atguigu.common.utils.R;
import com.atguigu.gulimall_third_party.oss.service.ossService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;


@RequestMapping("/thirdparty/oss")
@RestController
public class OssController {
    @Autowired
    com.atguigu.gulimall_third_party.oss.service.ossService ossService;

    @PostMapping("/policy")
    public R uploadFileAvatar() throws IOException {
        System.out.println("qingqiu进入");
        Map<String, String> map= ossService.uploadFile();
      return R.ok().put("data",map);

    }
}
