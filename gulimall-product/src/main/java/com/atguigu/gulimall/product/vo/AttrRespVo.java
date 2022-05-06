package com.atguigu.gulimall.product.vo;

import lombok.Data;

/**
 * @author zhangding
 * @email 651019052@qq.com
 * @date 2020-09-16 22:28
 */
@Data
public class AttrRespVo extends AttrVo {
    // "catelogName": "手机/数码/手机", //所属分类名字
    // "groupName": "主体", //所属分组名字
    private String catelogName;
    private String groupName;

    private Long[] catelogPath;
}
