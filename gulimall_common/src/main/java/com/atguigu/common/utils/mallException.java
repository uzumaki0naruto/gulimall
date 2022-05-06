package com.atguigu.common.utils;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;

@Slf4j
@RestControllerAdvice(basePackages = "com.atguigu.gulimall.product.controller")
public class mallException {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R ValidException(MethodArgumentNotValidException e){
        System.out.println();
        log.error("异常信息:{},异常类型{}",e.getMessage(),e.getClass());
        HashMap<String, String> resMap = new HashMap<>();
        e.getBindingResult().getFieldErrors().stream().forEach(item->{
            resMap.put(item.getField(),item.getDefaultMessage());
           log.error("异常字段名:{}",item.getField());
           log.error("错误信息:{}",item.getDefaultMessage());
        });
        return R.error(BizCodeEnume.VALID_EXCEPTION.getCode(), BizCodeEnume.VALID_EXCEPTION.getMessage()).put("data",resMap);
    }

    @ExceptionHandler(Throwable.class)
    public R AllException(Throwable throwable){
        log.error("异常信息:{},异常类型:{}",throwable.getMessage(),throwable.getClass());
        return R.error(BizCodeEnume.EXCEPTION.getCode(), BizCodeEnume.EXCEPTION.getMessage());
    }
}
