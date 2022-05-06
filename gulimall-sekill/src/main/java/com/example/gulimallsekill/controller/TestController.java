package com.example.gulimallsekill.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.atguigu.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;





    @RestController
    public class TestController {

//        @Autowired
//        private TestService service;
//
//        @GetMapping(value = "/hello/{name}")
//        public String apiHello(@PathVariable String name) {
//            System.out.println("path");
//            return service.sayHello(name);
//        }

        public R testSen(BlockException e){
            System.out.println("test successs");
            return R.ok();
        }

        @SentinelResource(value = "testUrl",blockHandler = "testSen")
        @GetMapping("/test")
        public R test(){
            System.out.println("test!");
            return R.ok();
        }

    }

