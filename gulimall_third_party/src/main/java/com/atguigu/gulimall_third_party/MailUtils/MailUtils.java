package com.atguigu.gulimall_third_party.MailUtils;

import com.sun.mail.util.MailSSLSocketFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.security.GeneralSecurityException;
import java.util.Properties;
import java.util.Random;


public class MailUtils {



    public static String sendMail(String email) {

        String code="uxkwljzpftnrbghe";// 激活码

        Random random = new Random();
        int nextInt = random.nextInt(9999 - 1000 + 1)+1000;

        String Ucode=nextInt+"";
        try {
            String from = "2089076142@qq.com";// 发件人电子邮箱
            String host = "smtp.qq.com"; // 指定发送邮件的主机smtp.qq.com(QQ)|smtp.163.com(网易)

            Properties properties = System.getProperties();// 获取系统属性
            properties.setProperty("mail.smtp.host", host);// 设置邮件服务器
            properties.setProperty("mail.smtp.auth", "true");// 打开认证
//        props.setProperty("mail.smtp.auth", "true");    // 发送服务器需要身份验证
//        props.setProperty("mail.host", host);        // 设置邮件服务器主机名
//        props.setProperty("mail.transport.protocol", "smtp");
            //QQ邮箱需要下面这段代码，163邮箱不需要
            MailSSLSocketFactory sf = new MailSSLSocketFactory();
            sf.setTrustAllHosts(true);
            properties.put("mail.smtp.ssl.enable", "true");
            properties.put("mail.smtp.ssl.socketFactory", sf);


            //1.获取默认session对象
            Session session = Session.getDefaultInstance(properties, new Authenticator() {
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(from, code); // 发件人邮箱账号、授权码

//             return new PasswordAuthentication("632906889@qq.com", "pibpzzvusfdhbccb"); // 发件人邮箱账号、授权码
                }
            });
            // 2.创建邮件对象
            Message message = new MimeMessage(session);
            // 2.1设置发件人
            message.setFrom(new InternetAddress(from));
            // 2.2设置接收人
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            // 2.3设置邮件主题
            message.setSubject("账号激活");
            // 2.4设置邮件内容
            String content = "<html><head></head><body><h1>这是一封激活邮件,激活请点击以下链接</h1>" +
                    "<h3><a href='http://localhost:8080/RegisterDemo/ActiveServlet?code="
                    + code + "'>http://localhost:8080/RegisterDemo/ActiveServlet?code=" + code+"验证码是"+Ucode
                    + "</href></h3></body></html>";
            message.setContent(content, "text/html;charset=UTF-8");
            // 3.发送邮件
            Transport.send(message);
            System.out.println("邮件成功发送!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Ucode;
    }

}
