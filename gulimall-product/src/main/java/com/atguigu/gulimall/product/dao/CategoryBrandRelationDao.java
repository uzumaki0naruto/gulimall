package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.CategoryBrandRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 品牌分类关联
 * 
 * @author zhangding
 * @email 651019052@qq.com
 * @date 2020-06-08 16:21:52
 */
@Mapper
public interface CategoryBrandRelationDao extends BaseMapper<CategoryBrandRelationEntity> {

    // 此处会关联mybatis中的方法，alt+enter会出现Genernte statement,自动在CategoryBrandRelationDao.xml中生成sql
    void updateCategory(@Param("catId") Long catId,@Param("name") String name);
}
