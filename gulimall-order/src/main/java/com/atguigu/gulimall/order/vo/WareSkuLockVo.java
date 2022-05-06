package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author 小坏
 * @Date 2021/2/9 13:56
 * @Version 1.0
 * @program: 父工程 gulimall 万物起源之地
 */

@Data
public class WareSkuLockVo {
private String orderSn; //订单号
private List<OrderItemVo> locks; //需要锁住的所有库存信息
    //主要取出来每一个商品需要锁几件
}
