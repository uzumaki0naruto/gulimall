package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.catalog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;


@Controller
public class indexController {
    @Autowired
    CategoryService categoryService;


    @GetMapping({"/","index.html"})
    public String index(Model model){
//        long l = System.currentTimeMillis();
        List<CategoryEntity> categoryList= categoryService.getLevel1Category();
         model.addAttribute("categorys",categoryList);
//        long l1 = System.currentTimeMillis() - l;
//        System.out.println("时间差为"+l1+"毫秒");

        return "index";
    }


    @ResponseBody
    @GetMapping("/index/json/catalog.json")
    public Map<String, List<catalog2Vo>> getCatalogListJson(){
        Map<String, List<catalog2Vo>> resMap=categoryService.getCatalogJson();
        return resMap;
    }


    @GetMapping("/item")
    public String item(){

        return "item";
    }




}
