package com.atguigu.gulimall.ware.exception;

public class testException extends RuntimeException{
    public testException(Long skuId){
        super("sss"+skuId);
    }
}
