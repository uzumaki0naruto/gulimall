package com.atguigu.authService.Feign;


import com.atguigu.authService.vo.SocialUser;
import com.atguigu.authService.vo.UserLoginVo;
import com.atguigu.authService.vo.UserRegisVo;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@FeignClient("gulimall-member")
public interface MemberFeignService {

    @PostMapping("/member/member/regist")
    R regist(@RequestBody UserRegisVo vo);

    @PostMapping("/member/member/login")
    R login(UserLoginVo vo);


    @PostMapping("/member/member/oauth2/login")
    R oauth2Login(@RequestBody SocialUser user) throws Exception;

}
