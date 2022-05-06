package com.atguigu.guilicart.vo;

import java.math.BigDecimal;
import java.util.List;

public class cart {
    List<cartItem> items;

    private Integer countNum;

    private Integer countType;//商品类型数量

    private BigDecimal totalAmount;//商品总价

    private BigDecimal reduce;

    public List<cartItem> getItems() {
        return items;
    }

    public void setItems(List<cartItem> items) {
        this.items = items;
    }

    public Integer getCountNum() {

        this.items.stream().forEach(item->{
           this.countNum+=item.getCount();

        });
        return countNum;
    }

    public void setCountNum(Integer countNum) {
        this.countNum = countNum;
    }

    public Integer getCountType() {
        return countType;
    }

    public void setCountType(Integer countType) {
        this.countType = countType;
    }

    public BigDecimal getTotalAmount() {
        this.items.forEach(cartItem -> {
        this.totalAmount=this.totalAmount.add(cartItem.getTotalPrice());
        });
        return totalAmount.subtract(this.getReduce());
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
