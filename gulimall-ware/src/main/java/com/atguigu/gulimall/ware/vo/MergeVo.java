package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @ClassName HuangXiangXiang
 * @Date 2020/7/20 16:17
 * @Version V1.0
 **/

@Data
public class MergeVo {
    private Long purchaseId;  //整单id
    private List<Long> items; //合并项集合


}
