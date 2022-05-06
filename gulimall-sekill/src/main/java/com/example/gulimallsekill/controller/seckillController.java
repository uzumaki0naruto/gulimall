package com.example.gulimallsekill.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.atguigu.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import com.atguigu.gulimall.seckill.vo.SeckillSessionWithSku;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
@RestController
@Slf4j
public class seckillController {

    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    com.example.gulimallsekill.service.seckillService seckillService;


    /**
     * 返回当前时间可以参与秒杀的商品
     * @return
     */
    @GetMapping("/getCurrentSeckillSkus")

    public R getCurrentSeckillSkus(){
        List<com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo>  list=seckillService.getCurrentSeckillSkus();
        return R.ok();
    }


    @GetMapping("/upload")
    public R upload(){
        seckillService.uploadSeckillSkuLast3Days();
        return R.ok();
    }
    @GetMapping("/kill")
    public R kill(@RequestParam(value = "killId")String killId,@RequestParam(value = "key")String key,
                  @RequestParam(value = "num")Integer num){
//        登入检测->合法性校验->获取信号量->       成功->快速创建秒杀单，用户，订单号，商品->    失败，秒杀时间，随机码，对应关系，幂等性->结束
//      1  -》（前端返回秒杀成功，正在为您准备订单）->收货地址确认页->支付确认页->结束
//      2  -》发送mq消息，订单服务监听，准备创建订单
        String orderSn= seckillService.kill( killId,key,num);
        return R.ok().setData(orderSn);
    }
}
