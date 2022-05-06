package com.atguigu.gulimall.coupon.service.impl;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.coupon.dao.SeckillSessionDao;
import com.atguigu.gulimall.coupon.entity.SeckillSessionEntity;
import com.atguigu.gulimall.coupon.service.SeckillSessionService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );
        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getSeckillLast3Day() {
        String startTime = getStartTime();
        String endTime = getEndTime();
        System.out.println(startTime+"-"+endTime);
        QueryWrapper<SeckillSessionEntity> wrapper
                = new QueryWrapper<>();
        wrapper.between("start_time",startTime,endTime);
        List<SeckillSessionEntity> list = baseMapper.selectList(wrapper);
        System.out.println("list:"+list);
        return list;
    }
    public String getStartTime(){
        LocalDate now = LocalDate.now();
        LocalDateTime of = LocalDateTime.of(now, LocalTime.MIN);
        String start = of.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return start;
    }
    public String getEndTime(){
        LocalDate now = LocalDate.now();
        LocalDate localDate = now.plusDays(1);
        LocalDateTime of = LocalDateTime.of(localDate, LocalTime.MAX);
        String end= of.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return end;
    }
}