package com.example.gulimallsekill.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import org.springframework.stereotype.Service;

@Service
public class TestService {

    @SentinelResource(value = "sayHello")
    public String sayHello(String name) {
        System.out.println("?");
        return "Hello, " + name;
    }


}