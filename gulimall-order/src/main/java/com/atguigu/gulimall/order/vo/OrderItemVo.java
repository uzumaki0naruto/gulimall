package com.atguigu.gulimall.order.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author 小坏
 * @Date 2021/2/4 11:09
 * @Version 1.0
 * @program: 父工程 gulimall 万物起源之地
 * 购物项
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemVo {

    private Long skuId;
    private Boolean check;
    private String title;
    private String image;
    private List<String> skuAttr;
    private BigDecimal price;
    private Integer count;
    private BigDecimal totalPrice; //总价

//    private boolean hasStock; //显示有货无货
}
