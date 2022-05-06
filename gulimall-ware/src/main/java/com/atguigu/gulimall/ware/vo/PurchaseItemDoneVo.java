package com.atguigu.gulimall.ware.vo;

import lombok.Data;

/**
 * @ClassName HuangXiangXiang
 * @Date 2020/7/26 11:50
 * @Version V1.0
 **/


@Data
public class PurchaseItemDoneVo {

    private Long itemId;
    private Integer status;
    private String reason;
}
