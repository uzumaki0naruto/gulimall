package com.atguigu.gulimall.order.dao;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author zhangding
 * @email 651019052@qq.com
 * @date 2020-06-09 23:46:24
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
