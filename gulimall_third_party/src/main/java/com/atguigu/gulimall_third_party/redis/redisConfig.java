package com.atguigu.gulimall_third_party.redis;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;

@Configuration
@EnableCaching
public class redisConfig  extends CachingConfigurerSupport {

//        private final static Logger logger = LoggerFactory.getLogger(config.class);


    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 初始化缓存管理器，在这里我们可以缓存的整体过期时间什么的，我这里默认没有配置
//        logger.info("初始化 -> [{}]", "CacheManager RedisCacheManager Start");
        RedisCacheManager manager = RedisCacheManager.create(connectionFactory);
        return manager;
    }

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory)
            throws UnknownHostException {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        //ObjectMapper 指定在转成json的时候的一些转换规则
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        template.setConnectionFactory(redisConnectionFactory);
//        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
//        //把自定义objectMapper设置到jackson2JsonRedisSerializer中（可以不设置，使用默认规则）
//        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
//
//        //RedisTemplate默认的序列化方式使用的是JDK的序列化
//        //设置了key的序列化方式
//        template.setKeySerializer(new StringRedisSerializer());
//        //设置了value的序列化方式
//        template.setValueSerializer(jackson2JsonRedisSerializer);
//        return template;
        // 3.创建 序列化类
        GenericToStringSerializer genericToStringSerializer = new GenericToStringSerializer(Object.class);
        // 6.序列化类，对象映射设置
        // 7.设置 value 的转化格式和 key 的转化格式
        template.setValueSerializer(genericToStringSerializer);
        template.setKeySerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

}