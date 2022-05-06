package com.atguigu.gulimall_third_party.oss.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.atguigu.gulimall_third_party.oss.service.ossService;
import com.atguigu.gulimall_third_party.oss.utils.ConstantPropertiesUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class ossServiceImpl implements ossService {

    @Override
    public Map<String, String> uploadFile() throws IOException {
        HashMap<String, Object> map = new HashMap<>();
        // yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
        String endpoint = ConstantPropertiesUtils.END_POINT;
    // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId =  ConstantPropertiesUtils.ACCESS_KEY_ID;
        String accessKeySecret =  ConstantPropertiesUtils.ACCESS_KEY_SECRET;
        String buckName=ConstantPropertiesUtils.BUCKET_NAME;
        String host="https://"+buckName+"."+endpoint;

        System.out.println("endpoint"+endpoint+"/t"+"accessKeyId"+"/t"+accessKeyId+"/t"+"accessKeySecret"+"/t"+accessKeySecret+"/t"+"buckName"+buckName);


//1.在文件名称里面添加随机唯一的值
        String uuid = UUID.randomUUID().toString().replaceAll("-","");
//        2.把文件按照日期分类
//        2019/11/12/01.jpg
//        获取当前日期,用作文件前缀，前端封装的最终名称是 日期+uuid+文件名
        String dataPath = new DateTime().toString("yyyy/MM/dd");
        String dir=dataPath;
// 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint,accessKeyId, accessKeySecret);

////              填写本地文件的完整路径。如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件流。
//        InputStream inputStream = file.getInputStream();
//// 依次填写Bucket名称（例如examplebucket）和Object完整路径（例如exampledir/exampleobject.txt）。Object完整路径中不能包含Bucket名称。
//        /*
//        发送？
//         */
//        ossClient.putObject(buckName, fileName, inputStream);
//
//        String data = new Date().toString("yyyy-MM-dd");
//
//// 关闭OSSClient。
//        ossClient.shutdown();
////        把需要上传到阿里云oss路径手动拼接出来
//        String url="https://"+buckName+"."+endpoint+"/"+fileName;
        Map<String, String> respMap=null;
        try {
            long expireTime = 30;
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            // PostObject请求最大可支持的文件大小为5 GB，即CONTENT_LENGTH_RANGE为5*1024*1024*1024。
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);

            String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = ossClient.calculatePostSignature(postPolicy);

            respMap = new LinkedHashMap<String, String>();
            respMap.put("accessid", accessKeyId);
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", dir);
            respMap.put("host", host);
            respMap.put("expire", String.valueOf(expireEndTime / 1000));
            // respMap.put("expire", formatISO8601Date(expiration));


        } catch (Exception e) {
            // Assert.fail(e.getMessage());
            System.out.println(e.getMessage());
        } finally {
            ossClient.shutdown();

        }
        return respMap;

    }
}
