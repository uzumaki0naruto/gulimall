package com.atguigu.gulimall.search.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @ClassName HuangXiangXiang
 * @Date 2020/12/19 23:13
 * @Version V1.0
 **/
public class Thread {
    public static ExecutorService service = Executors.newFixedThreadPool(2);

    public static void main(String[] args) throws ExecutionException, InterruptedException {

//没有返回值
//        CompletableFuture.runAsync(()->{
//            System.out.println("当前线程id"+ java.lang.Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("运行结构"+i);
//        },service);


        //有返回值
//        CompletableFuture<Integer> integerCompletableFuture = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程id" + java.lang.Thread.currentThread().getId());
//            int i = 10 / 0;
//            System.out.println("运行结构" + i);
//            return i;
//        }, service).whenComplete((res, ex) -> {
//            //虽然能得到异常信息、但是没法修改返回数据。
//            System.out.println("异步任务完成了，，结果是" + res + ";异常是" + ex);
//        }).exceptionally(e -> {
//            //可以感知异常、同时返回默认值
//            return 500;
//        });
//
//        Integer integer = integerCompletableFuture.get();
//        System.out.println(integer);


        /**
         * 方法完成后的处理
         */
//        CompletableFuture<Integer> integerCompletableFuture = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程id" + java.lang.Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("运行结构" + i);
//            return i;
//        }, service).handle((res, exe) -> {
//            if (res != null) {
//                return res * 2;
//            }
//            if (exe != null) {
//                return 0;
//            }
//            return 0;
//        });
//        Integer integer = integerCompletableFuture.get();
//        System.out.println(integer);

        /**
         * 1)、thenRun(thenRunAsync): 不能获取到上一步的执行结果、无返回值
         *  .thenRunAsync(() -> {
         *             System.out.println("任务2启动了");
         *         }, service);
         *
         * 2)、能接受到上一步结果、但是无返回值
         *  .thenAcceptAsync(res -> {
         *             System.out.println("任务2启动了" + res);
         *         }, service);
         *
         * 3)、能接受到上一步结果、但是还有返回值
         *  .thenApplyAsync(res -> {
         *             System.out.println("任务2启动了" + res);
         *             return "hello" + res;
         *         }, service);
         *          System.out.println(stringCompletableFuture.get());
         *
         */

//        CompletableFuture<String> stringCompletableFuture = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程id" + java.lang.Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("运行结构" + i);
//            return i;
//        }, service).thenApplyAsync(res -> {
//            System.out.println("任务2启动了" + res);
//            return "hello" + res;
//        }, service);
//
//        System.out.println(stringCompletableFuture.get());


//        CompletableFuture<Object> future01 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务1线程" + java.lang.Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("任务1结束：");
//            return i;
//        }, service);
//
//
//        CompletableFuture<Object> future02 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务2线程" + java.lang.Thread.currentThread().getId());
//            System.out.println("任务2结束：");
//            return "hello";
//        }, service);


        /*等待两个线程都完成 任务三才开始*/

//        future01.runAfterBothAsync(future02,()->{
//            System.out.println("任务三开始");
//        },service);


//        future01.thenAcceptBothAsync(future02, (f1, f2) -> {
//            System.out.println("任务三开始。。。之前的结果" + f1 + "-->" + f2);
//        }, service);


        //可以拿到前任结果、还能进行处理得到返回值
//        CompletableFuture<String> stringCompletableFuture = future01.thenCombineAsync(future02, (f1, f2) -> {
//            return f1 + ":" + f2;
//        }, service);
//        System.out.println(stringCompletableFuture.get());


        /*两个线程有一个完成 任务三就开始*/

        //runAfterEitherAsync: 不感知结果、自己也无返回值
//        future01.runAfterEitherAsync(future02, () -> {
//            System.out.println("任务三开始。。之前的结果");
//        },service);


        //acceptEitherAsync 感知结果、自己没有返回值
//        future01.acceptEitherAsync(future02, (res) -> {
//            System.out.println("任务三开始。。之前的结果" + res);
//        }, service);


        //applyToEitherAsync  感知结果\自己有返回值
//        CompletableFuture<String> stringCompletableFuture = future01.applyToEitherAsync(future02, res -> {
//            System.out.println("任务三开始。。之前的结果" + res);
//            return res.toString() + "->哈哈";
//        }, service);
//
//        System.out.println(stringCompletableFuture.get());
//        service.execute(new pag());


        CompletableFuture<String> futureImg = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品图片信息");
            return "hello.jpg";
        }, service);


        CompletableFuture<String> futureAttr = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品属性");
            return "黑色+256G";
        }, service);


        CompletableFuture<String> futureDesc = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品介绍");
            return "华为";
        }, service);

//        //所有的事都做完
//        CompletableFuture<Void> completableFuture = CompletableFuture.allOf(futureImg, futureAttr, futureDesc);
//        completableFuture.get(); //等待所有结果完成
//        System.out.println(futureImg.get() + futureAttr.get() + futureDesc.get());



        //这三个只要有一个执行成就算
        CompletableFuture<Object> anyOf = CompletableFuture.anyOf(futureImg, futureAttr, futureDesc);
        anyOf.get(); //等待所有结果完成
        System.out.println(anyOf.get());

        /**
         * 七大参数
         * corePoolSize:[5] 核心线程数[一直存在除非（allowCoreThreadTimeOut）]；线程池，创建好以后就准备就绪的线程数量、就等待来接受异步任务执行
         *          *  就相当于 new 了5个  Thread thread =new Thread(); thread.start()开启
         *
         * maximumPoolSize:[200] 最大线程数量；控制资源
         * keepAliveTime：存活时间。如果当前的线程数量大于core数量。
         *       释放空闲线程（maximumPoolSize - corePoolSize）。只要线程空闲大于指定keepAliveTime；
         * unit: 设置存活时间的单位
         * BlockingQueue<Runnable> workQueue：阻塞队列。如果任务有多个(比如线程池100个、进来500个 其余400放到队列里面)、就会将目前多的任务放在队列里面。
         *      只要有线程空闲。就会去队列里面取出新的任务继续执行。
         * ThreadFactory： 线程的创建工厂
         * RejectedExecutionHandler：如果队列满了、按照我们指定的拒绝策略拒绝执行任务
         *
         *
         */
//        ThreadPoolExecutor executor = new ThreadPoolExecutor(
//                5,
//                200,
//                10,
//                TimeUnit.SECONDS,
//                new LinkedBlockingDeque<>(100000),
//                Executors.defaultThreadFactory(),
//                new ThreadPoolExecutor.AbortPolicy()
//        );
    }


    public static class pag implements Runnable {
        @Override
        public void run() {
            System.out.println("你是狗");
        }
    }


}
