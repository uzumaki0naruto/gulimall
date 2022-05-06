package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.stockDetailTo;
import com.atguigu.common.to.mq.stockLockedTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.exception.NoStockException;
import com.atguigu.gulimall.ware.feign.orderFeignService;
import com.atguigu.gulimall.ware.service.WareOrderTaskDetailService;
import com.atguigu.gulimall.ware.service.WareOrderTaskService;
import com.atguigu.gulimall.ware.service.orderTaskDetailService;
import com.atguigu.gulimall.ware.vo.*;
import com.rabbitmq.client.Channel;
import lombok.Data;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    WareOrderTaskService wareOrderTaskService;

    @Autowired
    com.atguigu.gulimall.ware.feign.orderFeignService orderFeignService;
    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuStockVo> getSkuHasStock(List<Long> skuIdList) {
        List<SkuStockVo> resList = skuIdList.stream().map(
                skuId -> {
                    SkuStockVo skuStockVo = new SkuStockVo();
                    WareSkuEntity wareSkuEntity = baseMapper.selectById(skuId);
                    if (wareSkuEntity.getStock() > 0) {
                        skuStockVo.setHasStock(true);
                    } else {
                        skuStockVo.setHasStock(false);
                    }
                    skuStockVo.setSkuId(skuId);
//                    resList.add(skuStockVo);
                    return skuStockVo;
                }
        ).collect(Collectors.toList());
        return resList;
    }

    @Override
    @Transactional
    public Boolean orderLock(WareSkuLockVo wareSkuLockVo) {
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(wareSkuLockVo.getOrderSn());
//        1.根据下单的收货地址找到最近的仓库  --判断仓库里有没有这件商品
//        2.获得仓库里的商品数量，获取需要锁住的库存信息
        List<OrderItemVo> locks = wareSkuLockVo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(orderItemVo -> {
            SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
            Long skuId = orderItemVo.getSkuId();
            skuWareHasStock.setSkuId(skuId);
            skuWareHasStock.setCount(orderItemVo.getCount());
            WareSkuEntity wareSkuEntity = baseMapper.selectById(skuId);
            List<Long> wareIdList=baseMapper.listSkuWareHasStock(skuId);
            skuWareHasStock.setWareId(wareIdList);
            return skuWareHasStock;
        }).collect(Collectors.toList());
        for (SkuWareHasStock skuWareHasStock : collect) {
            Boolean skustocklock=false;
            List<Long> wareIds = skuWareHasStock.getWareId();
            Long id = skuWareHasStock.getSkuId();
            Integer c=skuWareHasStock.getCount();
            if(wareIds==null || wareIds.size()==0){
                throw new NoStockException(skuWareHasStock.getSkuId());
            }
            for (Long wareId : wareIds) {
                Long aLong = baseMapper.lockOrder(skuWareHasStock.getSkuId(), wareId, skuWareHasStock.getCount());
                if(aLong==1){
//              锁住了，不用换下一个仓库,锁住了，发送延时消息
                    WareOrderTaskDetailEntity detailEntity =  //订单详情信息
                            new WareOrderTaskDetailEntity(
                                    null,
                                    skuWareHasStock.getSkuId(),
                                    "",
                                    skuWareHasStock.getCount(),
                                    taskEntity.getId(),
                                    wareId,
                                    1
                                    );
                    stockLockedTo stockLockedTo = new stockLockedTo();
                    stockLockedTo.setId(taskEntity.getId());
                    stockDetailTo stockDetailTo = new stockDetailTo();
                    BeanUtils.copyProperties(detailEntity,stockDetailTo);
                    //只发id不行，防止订单回滚找不到数据
                    stockLockedTo.setStockDetailTo(stockDetailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange",
                            "stock.lock",
                            stockLockedTo,
                            new CorrelationData());
                    skustocklock=true; //标志位,位true就是锁成功了

                    break;
                }else{
//               没锁住，执行本地事务回滚,
                    throw new NoStockException(skuWareHasStock.getSkuId());
                }
            }
//          肯定全部是锁定成功的
        }
        return true;
    }
//    封装的商品id对应的,所有仓库id
    @Data
    class SkuWareHasStock{
        private Long skuId;
        private Integer count;
        private  List<Long> wareId;
    }
    /**
     * 库存解锁逻辑：
     * 回滚的两种情况：锁成功，但是后面的程序失败导致回滚。
     * 有工作单。证明库存锁定成功了，解锁还有看订单情况：
     * 订单已经取消--解锁。出现原因：用户主动取消，或者超时
     * 没取消--不能解锁：
     * 订单不存在
     * 锁失败-回滚：没有工作单，库存回滚了，这种情况无需解锁，因为已经添加了事务
     *
     * @param stockLockedTo
     * @throws IOException
     */
    public void unLockStock( stockLockedTo stockLockedTo) throws IOException {
        System.out.println("收到解锁库存消息");
//        Long id = stockLockedTo.getId();
        stockDetailTo detailTask = stockLockedTo.getStockDetailTo();
//        判断是否有工作单
//        Long detailId = detailTask.getId();
        Long taskId = detailTask.getTaskId();
        WareOrderTaskEntity byId = wareOrderTaskService.getById(taskId);
        if(byId!=null){//        存在工作单
            detailTask.setLockStatus(1);  //存在工作单说明已经锁定了，设置状态
            String orderSn = byId.getOrderSn();  //获取订单号
            R r = orderFeignService.getOrderBysn(orderSn); //获取结果
            OrderVo orderVo = r.getData(new TypeReference<OrderVo>() {});
            Integer status = orderVo.getStatus();
            if(status==4  || orderVo==null ){  //订单已经关闭,或者订单不存在.就执行解锁

                Long aLong = baseMapper.unlockOrder(detailTask.getSkuId(), detailTask.getWareId(), detailTask.getSkuNum());
                if(aLong==1){   //解锁成功,修改订单状态位,手动应答
                    detailTask.setLockStatus(2);
                }else{   //如果解锁失败，就消息拒绝，让其重新入队
                    throw new RuntimeException ("远程调用失败");
                }
            }else{  //如果订单存在，那么就不应该解锁
            }
        }else{ //没有工作单说明没锁成功，不需要解锁
        }
    }


    /**
     * 订单解锁
     * 防止订单服务卡顿，导致订单状态消息一直改不了、库存消息优先到期。查订单状态新建状态，什么都不做就走了
     * 导致卡顿的订单，永远不能解锁库存
     *
     * @param orderTo
     */

    @Transactional
    @Override
    public void unLockStock2(OrderTo orderTo) {

        String orderSn = orderTo.getOrderSn ();
        //查一下最新库存状态，防止重复解锁库存、根据他的id能找到每一个商品被锁定什么样
        WareOrderTaskEntity task = wareOrderTaskService.getOrderTasByOrderSn (orderSn);


        Long id = task.getId ();

        //按照工作单 找到所有  没用解锁库存、
        List<WareOrderTaskDetailEntity> list = wareOrderTaskDetailService.list (
                new QueryWrapper<WareOrderTaskDetailEntity> ()
                        .eq ("task_id", id) //库存工作单id
                        .eq ("lock_status", 1) //1、新建状态、还未解锁
        );

        //Long skuId,Long wareId,Integer num,Long taskDetailId
        for (WareOrderTaskDetailEntity entity : list) {
            //解锁 entity.getWareId ()-》那个仓库解锁     entity.getSkuNum ()-》解锁几件    entity.getId ()-》工作单的详情id
            baseMapper.unlockOrder(entity.getSkuId (), entity.getWareId (), entity.getSkuNum ());
        }

    }
}