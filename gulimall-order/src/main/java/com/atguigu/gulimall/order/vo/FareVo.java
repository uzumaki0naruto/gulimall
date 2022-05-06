package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author 小坏
 * @Date 2021/2/9 10:25
 * @Version 1.0
 * @program: 父工程 gulimall 万物起源之地
 */

@Data
public class FareVo {
private MemberAddressVo address;
private BigDecimal fare; //运费
}
