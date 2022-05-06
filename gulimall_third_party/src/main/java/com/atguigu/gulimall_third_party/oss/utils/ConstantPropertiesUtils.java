package com.atguigu.gulimall_third_party.oss.utils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@RefreshScope
public class ConstantPropertiesUtils implements InitializingBean {



    @Value(value = "${aliyun.oss.file.endpoint}")
    public  String endPonit;

    @Value(value ="${aliyun.oss.file.KeyId}")
    public  String access_key_id;

    @Value(value = "${aliyun.oss.file.KeySecret}")
    public  String access_key_secret;

    @Value(value ="${aliyun.oss.file.bucketname}")
    public String bucket_name;

    public static String END_POINT;
    public static String ACCESS_KEY_ID;
    public static String ACCESS_KEY_SECRET;
    public static String BUCKET_NAME;

    @Override
    public void afterPropertiesSet() throws Exception {
        END_POINT=endPonit;
        ACCESS_KEY_ID=access_key_id;
        ACCESS_KEY_SECRET = access_key_secret;
        BUCKET_NAME=bucket_name;
    }
}
