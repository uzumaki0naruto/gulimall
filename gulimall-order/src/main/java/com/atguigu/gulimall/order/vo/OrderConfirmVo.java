package com.atguigu.gulimall.order.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class OrderConfirmVo {

    //收货地址
    @Setter
    @Getter
    List<MemberAddressVo> address;

    //所有选中的购物项
    @Setter
    @Getter
    List<OrderItemVo> items;

    //优惠卷信息
    @Setter
    @Getter
    Integer integration;


    //放重令牌
    @Setter
    @Getter
    String orderToken;

    @Getter
    @Setter
    Map<Long,Boolean> stocks;





    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0");
        if (items != null) {
            for (OrderItemVo item : items) {
                /**
                 * 拿到当前的价格 item.getPrice()也是最新价格
                 * multiply() 乘以 item.getCount() 数量
                 */
                //当前项的价格
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                //叠加
                sum = sum.add(multiply);
            }
        }
        return sum;
    }

}
