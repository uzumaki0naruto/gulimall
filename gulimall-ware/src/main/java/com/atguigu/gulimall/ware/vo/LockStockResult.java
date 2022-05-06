package com.atguigu.gulimall.ware.vo;

import lombok.Data;

/**
 * @Author 小坏
 * @Date 2021/2/9 14:06
 * @Version 1.0
 * @program: 父工程 gulimall 万物起源之地
 */


@Data
public class LockStockResult {

    private Long skuId; //商品id
    private Integer num; //锁定数量
    private Boolean locked; //是否锁定
}
