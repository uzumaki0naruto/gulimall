package com.example.gulimallsekill.scheduling;

import com.example.gulimallsekill.service.seckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@EnableScheduling
@EnableAsync
@Component
public class seckillSkuScheduled {

    @Autowired
    com.example.gulimallsekill.service.seckillService seckillService;
    @Autowired
    RedissonClient redissonClient;
    public static final String SCKILL_SESSION_LOCK="sckill:lock";

//
//    @Async
////    @Scheduled(cron = "0 36 15 * * ?")
//    @Scheduled(cron = "*/3 * * * * *")
//    public void uploadSeckillSku(){
//        System.out.println("执行商品上架");
//        RLock lock = redissonClient.getLock(SCKILL_SESSION_LOCK);
//        lock.lock(10, TimeUnit.SECONDS);
//        try {
//            seckillService.uploadSeckillSkuLast3Days();
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//        finally {
//            lock.unlock();
//        }
//    }

    @Async
    @Scheduled(cron = "0 36 15 * * ?")
    public void hel(){
        System.out.println("定时任务测试");
    }

}
