package com.atguigu.gulimall.order.web.service;

import com.atguigu.common.to.mq.SeckillOrderTo;
import com.atguigu.common.utils.R;
import org.springframework.stereotype.Service;

@Service
public interface seckillService {
    R createSeckillOrder(SeckillOrderTo seckillOrderTo);
}
