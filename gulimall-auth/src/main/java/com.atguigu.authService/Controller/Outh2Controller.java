package com.atguigu.authService.Controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import com.atguigu.authService.Feign.MemberFeignService;
import com.atguigu.authService.vo.SocialUser;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberResVo;


import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;

/**
 * @Author 小坏
 * @Date 2021/1/19 17:53
 * @Version 1.0
 * @program: 父工程 gulimall 万物起源之地
 */

@Slf4j
@Controller
public class Outh2Controller {

    @Autowired
    private MemberFeignService memberFeignService;


    @GetMapping("/success")
    public String hxx() {
        System.out.println("回调成功");
        return "111";
    }

    @GetMapping("/weibo/success")
    public String weiBo(@RequestParam("code") String code, HttpSession session) throws Exception {
        System.out.println("微博登入");

        //1、根据code换取accessToken
        HashMap<String, String> map = new HashMap<>();

        map.put("client_id", "2524840732");
        map.put("client_secret", "8da929f8169af0b5e851518a97fea7b7");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://auth.gulimall.com:90/weibo/success");
        map.put("code", code);
        HttpResponse post = HttpUtils.doPost1("https://api.weibo.com", "/oauth2/access_token", "POST", null, null, map);

        System.out.println("post状态码"+post.getStatusLine().getStatusCode());
        //2、处理
        if (post.getStatusLine().getStatusCode() == 200) {
            //获取到了AccessToken
            String toString = EntityUtils.toString(post.getEntity());
            SocialUser user = JSON.parseObject(toString, SocialUser.class);

            //知道当前是那个社交用户
            //当用户是第一次进网站，自动注册进来(为当前社交用户生成一个会员信息账户，以后这个社交账号就对应指定的会员)
            R r = memberFeignService.oauth2Login(user);
            if (r.getCode() == 0) {
                MemberResVo data = r.getData("data", new TypeReference<MemberResVo>() {
                });
                log.info("社交日志" + data);
                //1、第一次使用session;命令浏览器保存卡号。JSESSIONID这 个cookie;
                //以后浏览器访问哪个网站就会带上这个网站的cookie;
                //子域之间: gulimall.com    auth.gulimall.com  order.gul imall. com
                //发卡的时候(指定域名为父域名)，即使是子域系统发的卡，也能让父域直接使用。
                //TODO 1、默认发的令牌。session=dsajkdjl。作用域：当前域；（解决子域session共享问题）
                //TODO 2、使用JSON的序列化方式来
                session.setAttribute("loginUser", data); //redisSessionc存储解决共享
                return "redirect:http://gulimall.com:90";
            } else {
                return "redirect:http://auth.gulimall.com:90/login.html";
            }
        } else {
            return "redirect:http://auth.gulimall.com:90/login.html";
        }
    }
}
