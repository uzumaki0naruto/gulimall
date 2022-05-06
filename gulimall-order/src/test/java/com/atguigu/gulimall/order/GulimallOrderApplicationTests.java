package com.atguigu.gulimall.order;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.lang.Nullable;
import org.springframework.test.context.junit4.SpringRunner;
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
class GulimallOrderApplicationTests {

    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;
//    @Test
//    public void createExchange() {
//        DirectExchange directExchange = new DirectExchange(
//         "hello-java-exchange",
//        true,
//        false,
//        null);
//        amqpAdmin.declareExchange(directExchange);
//        log.info("exchange has creaed","hello-java-exchange");
//
//    }
//    @Test
//    public void createQueue(){
//        Queue queue = new Queue(
//        "hello-java-queue",
//        true,
//        false,
//        false);
//        amqpAdmin.declareQueue(queue);
//        log.info("queue{} has creaed","hello-java-queue");
//    }
//    @Test
//    void createBinding(){
//        Binding binding = new Binding(
//                "hello-java-queue",
//                Binding.DestinationType.QUEUE,
//                "hello-java-exchange",
//                "hello.java",
//                null);
//        amqpAdmin.declareBinding(binding);
//        log.info("binding{} has creaed","hello-java-bingding");
//
//    }
//
//    @Test
//    public void sendMessage(){
//        String msg="hello-world1";
//        rabbitTemplate.convertAndSend("hello-java-exchange",
//                "hello.java",msg);
//        log.info("success:{}",msg);
//
//    }

}
