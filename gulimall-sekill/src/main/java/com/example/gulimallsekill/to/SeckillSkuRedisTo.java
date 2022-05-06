package com.atguigu.gulimall.seckill.to;

import com.atguigu.gulimall.seckill.vo.SkuInfoVo;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author 小坏
 * @Date 2021/2/25 21:29
 * @Version 1.0
 * @program: 父工程 gulimall 万物起源之地
 */

@Data
public class SeckillSkuRedisTo {

    /**
     * id
     */
    private Long id;
    /**
     * 活动id
     */
    private Long promotionId;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private Integer seckillCount;
    /**
     * 每人限购数量
     */
    private Integer seckillLimit;
    /**
     * 排序
     */
    private Integer seckillSort;


    //sku的详细信息
    private  SkuInfoVo skuInfoVo;

    //当前商品秒杀的开始时间
    private  Long startTime;

    //当前商品秒杀的结束时间
    private  Long endTime;

    /**
     * 商品秒杀随机码
     */
    private  String randomCode;

}
