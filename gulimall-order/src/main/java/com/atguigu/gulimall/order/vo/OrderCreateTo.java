package com.atguigu.gulimall.order.vo;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author 小坏
 * @Date 2021/2/9 10:11
 * @Version 1.0
 * @program: 父工程 gulimall 万物起源之地
 * 订单创建的对象
 */

@Data
public class OrderCreateTo {
    private OrderEntity order;
    private List<OrderItemEntity> orderItems;
    private BigDecimal payPrice; //订单计算的应付价格
    private BigDecimal fare; //运费


}
