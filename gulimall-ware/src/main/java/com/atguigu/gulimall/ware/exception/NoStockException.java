package com.atguigu.gulimall.ware.exception;



public class NoStockException extends RuntimeException{
//    @ExceptionHandler
//    public R noStockException
//    public defineException(String message){
//        super("自定义的异常"+message);
//    }

    public NoStockException(Long skuId) {

        super("没有足够的商品库存了:"+skuId);
    }

}
