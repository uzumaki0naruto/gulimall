package com.atguigu.gulimall.search.service;

import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearResult;

/**
 * @Author 小坏
 * @Date 2020/11/20 9:49
 * @Version 1.0
 * @program: 父工程 gulimall 万物起源之地
 */
public interface MallSearchService {

    /**
     * 检索的所有参数
     * @param param
     * @return
     */
    SearResult search(SearchParam param);
}
