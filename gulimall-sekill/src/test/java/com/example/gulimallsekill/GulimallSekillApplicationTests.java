package com.example.gulimallsekill;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//@SpringBootTest
class GulimallSekillApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    public void test(){

        LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(3);

        RejectedExecutionHandler handler = new
                ThreadPoolExecutor.DiscardPolicy();

        ThreadPoolExecutor.DiscardPolicy handler1=new ThreadPoolExecutor.DiscardPolicy();


        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                2,5,60, TimeUnit.MINUTES,queue,handler);


    }

}
