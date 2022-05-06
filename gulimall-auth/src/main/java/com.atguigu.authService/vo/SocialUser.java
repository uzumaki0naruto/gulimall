package com.atguigu.authService.vo;

import lombok.Data;

/**
 * @Author 小坏
 * @Date 2021/1/20 11:28
 * @Version 1.0
 * @program: 父工程 gulimall 万物起源之地
 */

@Data
public class SocialUser {

    private String access_token;
    private String remind_in;
    private String expires_in;
    private String uid;
    private String isRealName;

}
