package com.atguigu.authService.Controller;

import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.common.utils.StringUtils;

import com.atguigu.authService.Feign.MemberFeignService;
import com.atguigu.authService.vo.UserLoginVo;
import com.atguigu.authService.vo.UserRegisVo;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.utils.BizCodeEnume;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberResVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
public class LoginController {
    @Autowired
    com.atguigu.authService.Feign.thirdPartyFeignService thirdPartyFeignService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    MemberFeignService memberFeignService;


//    MS_CODE_CACHE_PREFIX = "sms:code:632906";
//
    @ResponseBody
    @GetMapping("/sms/sendCode/{email}")
    public R sendCode(@PathVariable String email){
        System.out.println("执行了发送验证码");
        String redisCode = (String)
                redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + email);
        if(!StringUtils.isEmpty(redisCode)){
            String[] s = redisCode.split("_", 1);
            long l = Long.parseLong(s[1]);
//           时间大于60s,报错
            if(System.currentTimeMillis()-l>60000){
                return R.error(BizCodeEnume.EXCEPTION.getCode(), BizCodeEnume.EXCEPTION.getMessage());
            }
        }
        String code = thirdPartyFeignService.sendMail(email);
        String codeValue=code+"_"+System.currentTimeMillis();
        String codeKey=AuthServerConstant.SMS_CODE_CACHE_PREFIX + email;
        redisTemplate.opsForValue().set(codeKey,codeValue);
        return R.ok();
    }



    @GetMapping("/regist")
    public String regist(@RequestBody UserRegisVo userRegisVo,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
        System.out.println("进入了regist方法");
        //1、有校验错误就转发到注册页
        if (result.hasErrors()) {
            //把校验错误的数据封装到Map中、再把返回到视图中
            Map<String, String> errors = result.getFieldErrors().stream().collect(
                    Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            /**
             * 页面跳转问题
             * 1、用户注册-》regist[post]  -->转发/reg.html(路径映射默认都是get方式访问的)
             *    转发时候刷新注册页会出现表单重复提交问题
             *        1、不要转发来到注册页、
             *        2、因为发现一旦注册成功是一次转发的模式、转发的时候路径不变、再来刷新就是一次重复提交
             *
             * 2、所以使用重定向的方式(重定向的方式还会有问题)
             *     1、model的数据是默认存在请求域当中的、我们可以获取到
             *     2、但是重定向它获取不到、
             *                重定向想要获取数据、我们也可以使用MVC自带的 RedirectAttributes 模拟重定向携带数据
             *                重定向携带数据。利用session原理。将数据放在session中。重定向新页面之后、再从session中取出来、只要跳到下一个页面取出这个页面以后
             *                 session里面的数据就会被删掉
             *
             *
             *    TODO 3、遇到session、肯定会出现分布式session、后面再解决
             */

//            model.addAttribute("errors", errors);
            redirectAttributes.addFlashAttribute("errors", errors); //模拟重定向携带数据、就是模拟session
//            return "reg"; 这个是返回的视图的逻辑地址、它会前后拼串得到注册页
//            return "forward:/reg.html"; //转发不拼串、因为已经指定了完整的地址、相当于会来到
//            GulimallWebConfig映射会来到资源下的注册页
            return "redirect:http://auth.gulimall.com:90/reg.html";
        }

        String code = userRegisVo.getCode();
        String email = userRegisVo.getEmail();
        String rediscodeValue = (String) redisTemplate.opsForValue().get
                (AuthServerConstant.SMS_CODE_CACHE_PREFIX + email);
        if (!StringUtils.isEmpty(rediscodeValue)) {
        if (code.equals(rediscodeValue.split("_")[0])) {
            redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + email);
            R r = memberFeignService.regist(userRegisVo);
//            登入成功
            if (r.getCode().equals(0)) {
                return "redirect:http://auth.gulimall.com:90/login.html";

            } else {
//                登入失败
                HashMap<String, String> errors = new HashMap<>();
                errors.put("msg", r.getData("msg", new TypeReference<String>() {
                }));
                redirectAttributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.gulimall.com:90/reg.html";
            }
        } else {
            //验证不通过
            HashMap<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误");
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com:90/reg.html";
        }

    } else {
        //如果等于null、就是没有查到
        HashMap<String, String> errors = new HashMap<>();
        errors.put("code", "验证码错误");
        redirectAttributes.addFlashAttribute("errors", errors);
        //校验出错，转发到注册页
        return "redirect:http://auth.gulimall.com:90/reg.html";
    }
    }

    @GetMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes, HttpSession session) {

        R login = memberFeignService.login(vo);
        if (login.getCode() == 0) {

            MemberResVo data = login.getData("data", new TypeReference<MemberResVo>() {
            });
            //存储到redis
            session.setAttribute(AuthServerConstant.LOGIN_USER, data);
            //成功
            return "redirect:http://gulimall.com:90";
        }
        //远程登录
        HashMap<String, String> map = new HashMap<>();
        map.put("msg", login.getData("msg", new TypeReference<String>() {
        }));
        redirectAttributes.addFlashAttribute("errors", map);
        return "redirect:http://auth.gulimall.com:90/login.html";
    }

//    远程登入
    @GetMapping("login.html")
    public String loginPage(HttpSession session){
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if(attribute ==null){
            return "index";
        }else {
            return "redirect:http://gulimall.com:90/";
        }
    }



}
