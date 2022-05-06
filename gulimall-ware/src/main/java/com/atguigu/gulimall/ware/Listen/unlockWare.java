package com.atguigu.gulimall.ware.Listen;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.mq.OrderTo;
import com.atguigu.common.to.mq.stockDetailTo;
import com.atguigu.common.to.mq.stockLockedTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.service.WareOrderTaskDetailService;
import com.atguigu.gulimall.ware.service.WareOrderTaskService;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.vo.OrderVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class unlockWare {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    WareOrderTaskService wareOrderTaskService;

    @Autowired
    com.atguigu.gulimall.ware.feign.orderFeignService orderFeignService;
    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;
    @Autowired
    WareSkuService wareSkuService;
    @Autowired
    WareSkuDao wareSkuDao;

    /**
     * 库存服务解锁
     * @param channel
     * @param message
     * @param stockLockedTo
     * @throws IOException
     */
    @RabbitListener(queues = {"stock.release.stock.queue"})
    public void unlock(Channel channel, Message message, stockLockedTo stockLockedTo) throws IOException {
        System.out.println ("库存的自动解锁功能");

        try {
            //当前消息是否被第二次及以后（重新）派发过来了、可以先判断一下是否处理过、但是太暴力了不用
//            Integer delay = message.getMessageProperties ().getReceivedDelay ();
            //解锁库存的方法、这里调用

            wareSkuService.unLockStock(stockLockedTo);
            //那个消息成了就说那个成了、false不是批量批量回复、只回复自己的
            channel.basicAck (message.getMessageProperties ().getDeliveryTag (), false);
        } catch (Exception e) {
            //有任何异常就让他归队、把当前消息返回队列
            channel.basicReject (message.getMessageProperties ().getDeliveryTag (), true);
        }

    }



    @RabbitListener(queues = {"stock.release.stock.queue"})
    public void unlock(Channel channel, Message message, OrderTo order) throws IOException {
        System.out.println ("订单关闭准备解锁。。。。。。。。");

        try {
            //解锁订单的方法、这里调用
            wareSkuService.unLockStock2(order);
            //那个消息成了就说那个成了、false不是批量批量回复、只回复自己的
            channel.basicAck (message.getMessageProperties ().getDeliveryTag (), false);
        } catch (Exception e) {
            //有任何异常就让他归队、把当前消息返回队列
            channel.basicReject (message.getMessageProperties ().getDeliveryTag (), true);
        }
    }







}
