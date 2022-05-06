package com.atguigu.gulimall.coupon.dao;

import com.atguigu.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author zhangding
 * @email 651019052@qq.com
 * @date 2020-06-09 21:58:19
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
