package com.atguigu.guilicart.vo;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class cartItem  {
    private long skuId;
    private boolean check;
    private String title;
    private String defaultImage;
    private BigDecimal price;
    private Integer count;
    private BigDecimal totalPrice;

    public List<String> getSkuAttr() {
        return skuAttr;
    }

    public void setSkuAttr(List<String> skuAttr) {
        this.skuAttr = skuAttr;
    }

    private List<String> skuAttr;

    public cartItem(long skuId, boolean check, String title, String defaultImage, BigDecimal price, List<String> skuAttr,Integer count) {
        this.skuId = skuId;
        this.check = check;
        this.title = title;
        this.defaultImage = defaultImage;
        this.price = price;
        this.count = count;
        this.skuAttr=skuAttr;
        this.totalPrice =price.multiply(new BigDecimal(count));
    }

    public long getSkuId() {
        return skuId;
    }

    public void setSkuId(long skuId) {
        this.skuId = skuId;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDefaultImage() {
        return defaultImage;
    }

    public void setDefaultImage(String defaultImage) {
        this.defaultImage = defaultImage;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public BigDecimal getTotalPrice() {
        return this.price.multiply(new BigDecimal(this.count));
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
