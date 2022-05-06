package com.example.gulimallsekill.service;

import com.baomidou.mybatisplus.extension.service.IService;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface seckillService {
    void uploadSeckillSkuLast3Days();

    List<com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo> getCurrentSeckillSkus();

    String kill(String killId, String key, Integer num);
}
