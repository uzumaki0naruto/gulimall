package com.atguigu.gulimall.order.intercept;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.vo.MemberResVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Slf4j
public class LoginUserInterceptor implements HandlerInterceptor {
    public static ThreadLocal<MemberResVo> threadLocal=new ThreadLocal<MemberResVo>(){};
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        log.info("进入了订单拦截器");
        HttpSession session = request.getSession();
     MemberResVo memberResVo= (MemberResVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
     if(memberResVo!=null){
//         用户登入成功,session
         session.setAttribute("loginUser",memberResVo);
         threadLocal.set(memberResVo);
         return true;
     }else {

         log.info("用户没有订单，不能使用购物车服务");
            response.sendRedirect("http://auth.gulimall.com:90/login.html");
            return false;
        }

    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }
}
