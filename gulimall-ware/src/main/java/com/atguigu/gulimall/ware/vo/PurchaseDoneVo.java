package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @ClassName HuangXiangXiang
 * @Date 2020/7/21 19:54
 * @Version V1.0
 **/


@Data
public class PurchaseDoneVo {

    @NotNull
    private Long id;


    private List<PurchaseItemDoneVo> items;

}
