package com.atguigu.gulimall_third_party.controller;

import com.atguigu.gulimall_third_party.MailUtils.MailUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class smsSendController {

    @GetMapping("/sendCode/{email}")
    public String sendMail(@PathVariable String email){
        System.out.println("第三方验证码已发送");
        String code = MailUtils.sendMail(email);


        return code;
    }
}
