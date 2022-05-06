package com.atguigu.gulimall.product.exception;


import com.atguigu.common.utils.BizCodeEnume;
import com.atguigu.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhangding
 * @email 651019052@qq.com
 * @date 2020-08-23 11:15
 */
/**
 * 集中处理所有异常
 */
@Slf4j  //日志
@RestControllerAdvice(basePackages = "com.atguigu.gulimall.product.controller")
// @RestControllerAdvice ：包含@ControllerAdvice 和 @ResponseBody
public class GulimallExceptionControllerAdvice {
    @ExceptionHandler(value = Exception.class)  //指定此方法可以处理什么异常
    public R handleValidException(MethodArgumentNotValidException exception){
        Map<String,String> map=new HashMap<>();
        BindingResult bindingResult = exception.getBindingResult();
        bindingResult.getFieldErrors().forEach(fieldError -> {
            String message = fieldError.getDefaultMessage();
            String field = fieldError.getField();
            map.put(field,message);
        });
        log.error("数据校验出现问题{},异常类型{}",exception.getMessage(),exception.getClass());
        return R.error(BizCodeEnume.VALID_EXCEPTION.getCode(), BizCodeEnume.VALID_EXCEPTION.getMessage()).put("data",map);
    }

    // 默认异常处理
    @ExceptionHandler(value = Throwable.class)  //指定此方法可以处理什么异常
    public R handleException(Throwable throwable){
        log.error("未知异常{}，异常类型{}", throwable.getMessage(),throwable.getClass());
        return R.error(BizCodeEnume.EXCEPTION.getCode(), BizCodeEnume.EXCEPTION.getMessage());
    }
}