package com.atguigu.gulimall.order.config;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;

@Configuration
public class MyMqConfig {


//    监听器获取
    @RabbitListener(queues = {"order.release.order.queue"})
    public void receiveMessage(OrderEntity orderEntity, Channel channel,Message message) throws IOException {

        System.out.println("收到过期消息，准备关闭订单"+orderEntity.getOrderSn());
        /**
         * 手动确认
         * message.getMessageProperties().getDeliveryTag()  返回消息的标志位
         * false 是否批量确认
         */
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
//        关闭订单
    }

    /*
    容器中的bingding,queue,exchange都会自动创建
    只要rabbitmq中有，就算属性发生变化也不会覆盖
     */
    //    延迟队列
    @Bean
    public Queue orderDelayQueue(){
        HashMap<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange","order-event-exchange"); //指定死信路由
        arguments.put("x-dead-letter-routing-key","order.release.order");//指定死信key
        arguments.put("x-message-ttl",1000);  //过期时间
        return new Queue("order.delay.queue",true,false,false,arguments);
    }

    //死信队列
    @Bean
    public Queue orderReleaseQueue(){
        return new Queue("order.release.order.queue",true,false,false);

    }
    @Bean
    public TopicExchange orderEventExchange(){
        return new TopicExchange("order-event-exchange",true,false,null);

    }
    @Bean
    public Binding orderCreateOrderBinding(){
        return new Binding("order.delay.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange","order.create.order",null);
    }

    @Bean
    public Binding orderReleaseOrderBinding(){
        return new Binding("order.release.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange","order.release.order",null);
    }

    /**
     * 订单释放直接和库存进行绑定
     *
     * @return
     */

    @Bean
    public Binding orderReleaseOtherBinding() {
        return new Binding ("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.other.#", null);
    }


//    @Bean
//    public Binding closeOrderBinding(){
//        return new Binding(
//                "order.release.order.queue",
//                Binding.DestinationType.QUEUE,
//                "order-event-exchange","order.release.order",null);
//    }

//    @Bean
//    public Queue closeQueus(){
//        HashMap<String, Object> arguments = new HashMap<>();
//        arguments.put("x-dead-letter-exchange","order-event-exchange"); //指定死信路由
//        arguments.put("x-dead-letter-routing-key","order.release.order");//指定死信key
//        arguments.put("x-message-ttl",1000);  //过期时间
//        return new Queue("order.release.order.queue",true,false,false);
//    }


    @Bean
    public Queue orderSeckillOrderQueue() {
        /**
         * String name,
         * boolean durable, 是否持久化
         * boolean exclusive, 是否排他的（大家都能监听用、谁抢到、是谁的）
         * boolean autoDelete, 是否自动删除
         * Map<String, Object> arguments  参数信息、这是普通队列（不需要参数）
         */
        return new Queue ("order.seckill.order.queue", true, false, false);
    }


    /**
     * 绑定秒杀和订单的交换机
     */
    @Bean
    public Binding orderSeckillOrderQueueBinding() {
        /**
         * String destination, 目的地
         * DestinationType destinationType, 目的地的类型：是队列
         * String exchange, 交换机
         * String routingKey, 路由键
         * 	Map<String, Object> arguments 参数信息、这是普通队列（不需要参数）
         */
        return new Binding ("order.seckill.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.seckill.order",
                null);
    }
}
