package com.atguigu.gulimall.ware.service;

import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.stockLockedTo;
import com.atguigu.gulimall.ware.vo.LockStockResult;
import com.atguigu.gulimall.ware.vo.SkuStockVo;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author zhangding
 * @email 651019052@qq.com
 * @date 2020-06-09 23:55:10
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SkuStockVo> getSkuHasStock(List<Long> skuIdList);

    Boolean orderLock(WareSkuLockVo wareSkuLockVo);
     void unLockStock( stockLockedTo stockLockedTo) throws IOException;

    public void unLockStock2(OrderTo orderTo);
}

