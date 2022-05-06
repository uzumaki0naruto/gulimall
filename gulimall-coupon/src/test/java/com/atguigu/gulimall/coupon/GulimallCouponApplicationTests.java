package com.atguigu.gulimall.coupon;


import javafx.util.converter.LocalDateStringConverter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

//@SpringBootTest
class GulimallCouponApplicationTests {

    @Test
    void contextLoads() {
        System.out.println("1");
        LocalDateStringConverter localDateStringConverter = new LocalDateStringConverter();
//        LocalDate localDate = new LocalDate();
        System.out.println(localDateStringConverter);
        LocalDate now = LocalDate.now();
        System.out.println(now);
//        LocalDate plus = now.plus(new Duration(1));
        LocalDate localDate = now.plusDays(1);
        LocalDate localDate1 = now.plusDays(2);
        System.out.println(localDate);
        System.out.println(localDate1);
        System.out.println(LocalTime.MIN+"localtime");
        System.out.println(LocalTime.MAX);
//        LocalDateTime localDateTime = localDate.atStartOfDay();
//        System.out.println(localDateTime);
        LocalDateTime of = LocalDateTime.of(now, LocalTime.MIN);
        LocalDateTime of1 = LocalDateTime.of(localDate1, LocalTime.MAX);
        System.out.println("---"+of);
        System.out.println("---"+of1);
        System.out.println("-----------------");
        LocalDateTime now2 = LocalDateTime.now();
        LocalDateTime d2 = now2.plusDays(1);
        System.out.println("==?"+d2);
    }

    @Test
    public void tes2(){
        LocalDateTime now = LocalDateTime.now();
        String format = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println(format);
    }

}
