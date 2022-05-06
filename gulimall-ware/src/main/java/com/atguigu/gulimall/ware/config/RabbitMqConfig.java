package com.atguigu.gulimall.ware.config;

import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class RabbitMqConfig {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    /**
     *
     *  correlationData:消息的唯一标识
     * ack： 是否成功应答
     *  cause: 原因
     * @return
     */
    @PostConstruct
    public void initRedisTemplate(){
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
//                System.out.println("唯一标识是:"+correlationData.getId());
//                System.out.println("---->是否成功:"+ack+"--->失败原因"+cause);
            }
        });
        /**
         *
         *
         *
         message–出问题消息的返回的消息。
         replyCode–出问题消息的回复代码。
         replyText–出问题消息的回复文本。
         exchange  出问题消息的交换机。
         routingKey–出问题消息的路由密钥。

         */
        rabbitTemplate.setReturnsCallback(new RabbitTemplate.ReturnsCallback() {
            @Override
            public void returnedMessage(ReturnedMessage returned) {
                System.out.println("失败消息是"+returned.getMessage()+"-->失败返回码"+returned.getReplyCode()
                +"--->错误交换机:"+returned.getExchange()+"---->错误绑定队列"+returned.getRoutingKey()
                        +"--->replytext"+returned.getReplyText()
                );
            }
        });

    }





}
