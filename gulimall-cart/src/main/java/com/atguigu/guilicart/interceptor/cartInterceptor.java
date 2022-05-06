package com.atguigu.guilicart.interceptor;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.constant.CartConstant;
import com.atguigu.common.vo.MemberResVo;
import com.atguigu.guilicart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.devtools.remote.client.HttpHeaderInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

@Component
@Slf4j
public class cartInterceptor implements HandlerInterceptor {

//    ThreadLocal<UserInfoTo> threadLocal=new  ThreadLocal<UserInfoTo>();
    public static ThreadLocal<UserInfoTo> threadLocal=new ThreadLocal<UserInfoTo>();
    /**
     * 方法执行前执行拦截,拦截请求判断用户是否登入,
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("进入了购物车拦截器");
        UserInfoTo userInfoTo = new UserInfoTo();  //购物车需要的用户信息
        HttpSession session = request.getSession();
        MemberResVo memberResVo= (MemberResVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if(memberResVo!=null){
//            登入成功了,设置用户id
            userInfoTo.setUserId(memberResVo.getId());
        }else{
//            登入失败,先获取所有cookie,判断cookie中是否有userkey,有直接放置到userinfoto
            Cookie[] cookies = request.getCookies();
            for (Cookie cookie : cookies) {
                    if(cookie.getName().equals(CartConstant.TEMP_USER_COOKIE_NAME)){
                        userInfoTo.setUserKey(cookie.getValue());
                    }
            }
        }
//        没有userkey就设置一个
        if(userInfoTo.getUserId()==null){
            userInfoTo.setTempUser(true);
            userInfoTo.setUserKey(UUID.randomUUID().toString());
        }

        threadLocal.set(userInfoTo);

        return true;
    }
//方法执行后执行,为什么把cookie保存这一项放在业务完成之后呢，我认为的是如果业务中断了也就没有必要长期保存临时用户key了
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//        System.out.println("postHandler");
        log.info("购物车拦截器---postHandle,如果是临时用户的话那就新创建了一个cookie，并且把临时用户的cookie设置到cookie中");
        UserInfoTo userInfoTo = threadLocal.get();
        if(userInfoTo.isTempUser()){
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIME);
            response.addCookie(cookie);
        }
    }
}
