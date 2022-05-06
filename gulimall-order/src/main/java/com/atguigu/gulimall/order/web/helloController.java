package com.atguigu.gulimall.order.web;

import com.atguigu.gulimall.order.entity.OrderEntity;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

@RestController
public class helloController {
    @Autowired
    AmqpAdmin amqpAdmin;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @GetMapping("/create/order")
    public String DelayQueueTest(){
//        创建订单
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(UUID.randomUUID().toString());
        orderEntity.setReceiverName("我");
        orderEntity.setMemberUsername("我和你");
        orderEntity.setModifyTime(new Date());
//        給mq发送消息
        rabbitTemplate.convertAndSend("order-event-exchange",
                "order.create.order",
                orderEntity,
                new CorrelationData());
        return "ok";
    }


}
