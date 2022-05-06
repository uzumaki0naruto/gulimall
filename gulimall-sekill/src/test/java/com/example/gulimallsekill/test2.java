package com.example.gulimallsekill;


import com.alibaba.csp.sentinel.annotation.SentinelResource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class test2 {

    @Test
    public void test(){
//        1651304487406  1651304707242
        long time = new Date().getTime();
        System.out.println(time);
        LocalDateTime days = LocalDateTime.now().plusDays(-1);
    }


    @Test
    public void test2(){
                Semaphore semaphore = new Semaphore(3);
                for (int i = 0; i < 5; i++) {
                    new Thread(()->{
                        try {
//                    抢占信号量
                            semaphore.acquire();
                            System.out.println(Thread.currentThread().getName()+"---抢占成功");
                            TimeUnit.SECONDS.sleep(new Random().nextInt(5));
                            System.out.println(Thread.currentThread().getName()+"---释放");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }finally {
                            semaphore.release();
                        }
                    },String.valueOf(i)).start();
                }





    }



    @Service
    public class TestService {

        @SentinelResource(value = "sayHello")
        public String sayHello(String name) {
            return "Hello, " + name;
        }
    }

    @RestController
    public class TestController {

        @Autowired
        private TestService service;

        @GetMapping(value = "/hello/{name}")
        public String apiHello(@PathVariable String name) {
            System.out.println("path");
            return service.sayHello(name);
        }
    }

}
