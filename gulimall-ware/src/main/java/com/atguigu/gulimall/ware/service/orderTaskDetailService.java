package com.atguigu.gulimall.ware.service;

import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

@Service
public interface orderTaskDetailService extends IService<WareOrderTaskDetailEntity> {
     WareOrderTaskDetailEntity getById(Long detailId);
}
