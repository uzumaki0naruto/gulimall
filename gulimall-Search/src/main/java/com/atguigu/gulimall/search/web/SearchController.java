package com.atguigu.gulimall.search.web;

import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author 小坏
 * @Date 2020/11/18 11:59
 * @Version 1.0
 * @program: 父工程 gulimall 万物起源之地
 *
 *
 * 这是搜索页传递参数的接口
 */

@Controller
public class SearchController {

    @Autowired
    private MallSearchService searchService;

    /**
     * 自动将页面提交过来的所有请求查询参数封装成指定的对象
     * 改为list.html 是因为点击搜索携带的路径是list.html加参数
     * @param param
     * @return
     */
    @GetMapping("/list.html")
    public String pageList(SearchParam param, Model model, HttpServletRequest request) {
        System.out.println("jiansuo");

//        String queryString = request.getQueryString();
        param.set_queryString(request.getQueryString());
        //1、根据传递来的页面的查询参数，去es中检索商品
        SearResult result = searchService.search(param);
        model.addAttribute("result",result);
        System.out.println("result"+result);
        return "list";
    }
    @GetMapping("/l")
    public String pageList2() {


        return "list";
    }
}
