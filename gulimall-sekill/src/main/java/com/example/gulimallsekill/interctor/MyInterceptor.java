package com.example.gulimallsekill.interctor;


import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.vo.MemberResVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Slf4j
@Component
public class MyInterceptor  implements HandlerInterceptor {

    public static ThreadLocal<MemberResVo> threadLocal=new ThreadLocal<MemberResVo>(){

    };


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        boolean kill = antPathMatcher.match("kill", requestURI);
        if(kill){
            log.info("进了了秒杀服务");
            HttpSession session = request.getSession();
            MemberResVo memberResVo= (MemberResVo) session.getAttribute(AuthServerConstant.LOGIN_USER);

            if(memberResVo!=null){
                threadLocal.set(memberResVo);
                return true;
            }else{
                log.info("用户没有登入，不能使用秒杀服务");
                response.sendRedirect("http://auth.gulimall.com:90/login.html");
                return false;
            }
        }else{
            return true;
        }



    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }
}
