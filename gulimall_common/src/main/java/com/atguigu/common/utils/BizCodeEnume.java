package com.atguigu.common.utils;




        /*  10: 通用
         *      001：参数格式校验
         *  11: 商品
         *  12: 订单
         *  13: 购物车
         *  14: 物流
         *  15:用户
         *  21：库存
         *
                 *
                 */
    public enum BizCodeEnume {
        VALID_EXCEPTION(1001,"数据校验异常"),
        EXCEPTION(1002,"其它异常"),
        TO_MANY_REQUEST(10002, "请求流量过大"),
        UNKNOW_EXCEPTION(10000, "系统未知异常"),
//        VAILD_EXCEPTION(10001, "参数格式校验失败"),
        SMS_EXCEPTION(10002, "验证码获取频率太高，稍后再试"),
        PRODUCT_UP_EXCEPTION(11000, "商品商家异常"),
        USER_EXIT_EXCEPTION(15001, "用户存在"),
        PHONE_EXIT_EXCEPTION(15002, "手机号存在"),
        NO_STOCK_EXCEPTION(21000, "商品库存不足"),
        LOGIN_PASSWORD_INVAILD_EXCEPTION(15003, "账户密码错误");

    int code;
    String message;

    BizCodeEnume(int code, String message) {
        this.code=code;
        this.message=message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
