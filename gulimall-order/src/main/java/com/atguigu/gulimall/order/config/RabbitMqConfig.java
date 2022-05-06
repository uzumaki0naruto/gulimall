package com.atguigu.gulimall.order.config;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.ConfirmCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.AbstractMessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.MessagingMessageConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.Nullable;

import javax.annotation.PostConstruct;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class RabbitMqConfig {

    @Autowired
    RabbitTemplate rabbitTemplate;

//    @Primary
//    @Bean
//    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
//        RabbitTemplate template = new RabbitTemplate();
//        RabbitTemplateConfigurer configurer = new RabbitTemplateConfigurer();
////        configurer.setMessageConverter(messageConverter.getIfUnique());
////        configurer.setMessageConverter(new ObjectProvider<MessageConverter>().getIfUnique());
////        configurer.setMessageConverter(new ObjectProvider<MessagingMessageConverter>());
////        configurer.setMessageConverter(messageConverter());
//
//        configurer.configure(template, connectionFactory);
//        return template;
//    }

//    @Primary
//    @Bean
//    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
//        //这个方法的目的是解决、出现循环依赖
//        RabbitTemplate rabbitTemplate = new RabbitTemplate (connectionFactory);
//        this.rabbitTemplate = rabbitTemplate;
//        rabbitTemplate.setMessageConverter (messageConverter ());
//        initRabbitTemplate ();
////        RabbitTemplateConfigurer
//        return rabbitTemplate;
//    }



    /**
     *
     *  correlationData:消息的唯一标识
     * ack： 是否成功应答
     *  cause: 原因
     * @return
     */
    @PostConstruct
    public void initRabbitTemplate(){
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                log.info("发送成功");
//                System.out.println("唯一标识是:"+correlationData.getId());
//                System.out.println("---->是否成功:"+ack+"--->失败原因"+cause);
            }
        });
        /**
         message–出问题消息的返回的消息。
         replyCode–出问题消息的回复代码。
         replyText–出问题消息的回复文本。
         exchange  出问题消息的交换机。
         routingKey–出问题消息的路由密钥。
         */
        rabbitTemplate.setReturnsCallback(new RabbitTemplate.ReturnsCallback() {
            @Override
            public void returnedMessage(ReturnedMessage returned) {
                System.out.println(
                        "失败消息是:"+returned.getMessage()
                        +"-->失败返回码:"+returned.getReplyCode()
                        +"-->错误交换机:"+returned.getExchange()
                        +"-->错误绑定队列:"+returned.getRoutingKey()
                        +"-->replytext:"+returned.getReplyText()
                );
            }
        });

    }





}
