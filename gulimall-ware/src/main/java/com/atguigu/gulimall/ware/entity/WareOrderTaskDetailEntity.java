package com.atguigu.gulimall.ware.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存工作单
 * 
 * @author zhangding
 * @email 651019052@qq.com
 * @date 2020-06-09 23:55:10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor

@TableName("wms_ware_order_task_detail")
public class WareOrderTaskDetailEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * sku_id
	 */
	private Long skuId;
	/**
	 * sku_name
	 */
	private String skuName;
	/**
	 * 购买个数
	 */
	private Integer skuNum;
	/**
	 * 工作单id
	 */
	private Long taskId;

	/**
	 * 仓库id
	 */
	private Long wareId;
	/**
	 *  锁定状态
	 *  1-已锁定  2-已解锁  3-扣减
	 *
	 */
	private Integer lockStatus;

}
