package com.atguigu.gulimall.order.Listen;


import com.atguigu.common.to.mq.SeckillOrderTo;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.web.service.seckillService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import com.atguigu.gulimall.order.web.service.seckillService;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

//@RabbitListener(queues = "order.seckill.order.queue")
@Slf4j
@Component
public class seckillListen {
//
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    seckillService seckillService;
//
    @Autowired
    OrderService orderService;


    @RabbitHandler
    public void createSeckillOrder(Channel channel, Message message, SeckillOrderTo seckillOrderTo) throws ExecutionException, InterruptedException {
        log.info("订单服务监听到秒杀服务创建订单消息:"+seckillOrderTo);
        R info=orderService.createSeckillOrder(seckillOrderTo);

    }
}
