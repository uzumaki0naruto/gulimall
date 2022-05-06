package com.atguigu.gulimall_third_party.oss.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public interface ossService {
    Map<String, String> uploadFile() throws IOException;
}
