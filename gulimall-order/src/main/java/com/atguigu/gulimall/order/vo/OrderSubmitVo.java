package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author 小坏
 * @Date 2021/2/8 12:04
 * @Version 1.0
 * @program: 父工程 gulimall 万物起源之地
 *
 * 封装订单提交的数据
 */


@Data
public class OrderSubmitVo {
    private Long addrId; //收货地址的id
    private Integer payType; //支付方式
    //无需提交需要购买的商品，去购物车在获取一遍
    //优惠发票
    private String orderToken; //防重令牌
    private BigDecimal payPrice; //应付价格、验价
    private String note; //订单备注
}
