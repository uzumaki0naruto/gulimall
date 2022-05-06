package com.atguigu.gulimall.seckill.vo;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * @Author 小坏
 * @Date 2021/2/25 20:56
 * @Version 1.0
 * @program: 父工程 gulimall 万物起源之地
 */

@Data
@Component
public class SeckillSessionWithSku {


    /**
     * id
     */

    private Long id;
    /**
     * 场次名称
     */
    private String name;
    /**
     * 每日开始时间
     */
    private Date startTime;
    /**
     * 每日结束时间
     */
    private Date endTime;
    /**
     * 启用状态
     */
    private Integer status;
    /**
     * 创建时间
     */
    private Date createTime;


    private List<com.atguigu.gulimall.seckill.vo.SeckillSkuVo> seckillSkuRelationEntityList;
}
