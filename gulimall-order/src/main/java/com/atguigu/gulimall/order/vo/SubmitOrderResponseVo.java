package com.atguigu.gulimall.order.vo;

import com.atguigu.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @Author 小坏
 * @Date 2021/2/8 14:11
 * @Version 1.0
 * @program: 父工程 gulimall 万物起源之地
 */

@Data
public class SubmitOrderResponseVo{

    private OrderEntity order;
    private Integer code; //0成功    错误状态码

}
