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
//        //???????????????????????????????????????????????????
//        RabbitTemplate rabbitTemplate = new RabbitTemplate (connectionFactory);
//        this.rabbitTemplate = rabbitTemplate;
//        rabbitTemplate.setMessageConverter (messageConverter ());
//        initRabbitTemplate ();
////        RabbitTemplateConfigurer
//        return rabbitTemplate;
//    }



    /**
     *
     *  correlationData:?????????????????????
     * ack??? ??????????????????
     *  cause: ??????
     * @return
     */
    @PostConstruct
    public void initRabbitTemplate(){
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                log.info("????????????");
//                System.out.println("???????????????:"+correlationData.getId());
//                System.out.println("---->????????????:"+ack+"--->????????????"+cause);
            }
        });
        /**
         message???????????????????????????????????????
         replyCode????????????????????????????????????
         replyText????????????????????????????????????
         exchange  ??????????????????????????????
         routingKey????????????????????????????????????
         */
        rabbitTemplate.setReturnsCallback(new RabbitTemplate.ReturnsCallback() {
            @Override
            public void returnedMessage(ReturnedMessage returned) {
                System.out.println(
                        "???????????????:"+returned.getMessage()
                        +"-->???????????????:"+returned.getReplyCode()
                        +"-->???????????????:"+returned.getExchange()
                        +"-->??????????????????:"+returned.getRoutingKey()
                        +"-->replytext:"+returned.getReplyText()
                );
            }
        });

    }





}
