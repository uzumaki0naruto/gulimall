package com.atguigu.gulimall.ware.dao;

import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author zhangding
 * @email 651019052@qq.com
 * @date 2020-06-09 23:55:10
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    List<Long>  listSkuWareHasStock(@Param(value = "skuId") Long skuId);

    Long lockOrder(@Param(value = "skuId") Long skuId,
                   @Param(value = "wareId")Long wareId,
                   @Param(value = "count") Integer count);


    Long unlockOrder(@Param(value = "skuId") Long skuId,
                   @Param(value = "wareId")Long wareId,
                   @Param(value = "count") Integer count);
}
