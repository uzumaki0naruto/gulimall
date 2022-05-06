package com.example.gulimallsekill.config;



import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class MyRabbitMqConfig {

//    @Bean
//    public Queue orderQueue(){
//        return new Queue("order.seckill.order.queue",true,false,false);
//    }
//    @Bean
//    public TopicExchange orderExchange(){
//        return new TopicExchange("order-event-exchange",true,false);
//    }
//
//    @Bean
//    public Binding orderBinding(){
//        return new Binding("order.seckill.order.queue",
//                Binding.DestinationType.EXCHANGE,
//                "order-event-exchange",
//                "order.seckill.order",
//                null);
//    }

}
