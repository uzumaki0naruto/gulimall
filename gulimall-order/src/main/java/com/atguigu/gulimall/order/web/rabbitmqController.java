package com.atguigu.gulimall.order.web;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderReturnApplyEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

//@RabbitListener(queues = {"hello-java-queue"})
@RestController
public class rabbitmqController {
//    @Autowired
//    RabbitTemplate rabbitTemplate;
//    @GetMapping("/send")
//        public String sendMq(@RequestParam(value = "num",defaultValue = "10") Integer num){
//            for (int i = 0; i < num; i++){
//                if (i%2==0){
//                    OrderReturnApplyEntity orderReturnApplyEntity = new OrderReturnApplyEntity();
//                    orderReturnApplyEntity.setId(1L);
//                    orderReturnApplyEntity.setCreateTime(new Date());
//                    orderReturnApplyEntity.setReturnName("哈哈哈");
//                    //配置MyRabbitConfig，让发送的对象类型的消息，可以是一个json
//                    rabbitTemplate.convertAndSend("hello-java-exchange","hello.java",orderReturnApplyEntity, new CorrelationData(UUID.randomUUID().toString()));
//                }else {
//                    OrderEntity entity = new OrderEntity();
//                    entity.setOrderSn(UUID.randomUUID().toString());
//                    rabbitTemplate.convertAndSend("hello-java-exchange","hello.java",entity, new CorrelationData(UUID.randomUUID().toString()));
//                }
//            }
//            return "OK";
//    }
//
//
//    //    @RabbitListener(queues = {"hello-java-queue"})
//    public void receiveMessage(Object message){
//
//        System.out.println("message:"+message+"----->type :"+message.getClass());
//    }
//
//
//
//    @RabbitHandler
//    public void receiverMessage1(Message message,OrderReturnApplyEntity content,
//                                Channel channel) throws InterruptedException {
//        //消息体
//        byte[] body = message.getBody();
//        //消息头属性信息
//        MessageProperties properties = message.getMessageProperties();
//        System.out.println("接收到消息...内容:" + content);
////        Thread.sleep(3000);
//        System.out.println("消息处理完成=》"+content.getReturnName());
//
//        long deliveryTag = message.getMessageProperties().getDeliveryTag();
//        System.out.println("deliveryTag:"+deliveryTag);
//        //签收货物，非批量模式
//        try{
//            if (deliveryTag % 2 == 0){
//                //收货
//                channel.basicAck(deliveryTag,false);
//                System.out.println("签收了货物。。。"+deliveryTag);
//            }else {
//                //退货requeue=false 丢弃  requeue=true发挥服务器，服务器重新入队。
//                channel.basicNack(deliveryTag,false,true);
//                System.out.println("没有签收货物..."+deliveryTag);
//            }
//
//        }catch (Exception e) {
//            //网络中断
//        }
//
//    }
//
//////
////    @RabbitHandler(isDefault = true)
////    public void receiverMessage2(OrderEntity orderEntity) throws InterruptedException{
////        System.out.println("接收到消息...内容:" + orderEntity);
////    }



}
