package com.atguigu.gulimall.order.Listen;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Component
@Slf4j
@RabbitListener(queues = "order.release.order.queue")
public class closeListen {

    @Autowired
    private  OrderService orderService;


    @RabbitHandler
    public void closeOrder(Channel channel, Message message, OrderEntity orderEntity) throws IOException {
        //        订单如何关闭
//                System.out.println ("收到过期的信息、准备关闭订单" + orderEntity.getOrderSn ());
                log.info("收到过期的信息、准备关闭订单");
                try {
                    orderService.closeOrder(orderEntity);
                    //手动调用支付宝的收单功能--->在线文档https://opendocs.alipay.com/apis/api_1/alipay.trade.close
                    channel.basicAck (message.getMessageProperties ().getDeliveryTag (), false);
                } catch (Exception e) {
                    //消息已拒绝、还返回到队列里面
                    channel.basicReject (message.getMessageProperties ().getDeliveryTag (), true);
                }
    }


}
