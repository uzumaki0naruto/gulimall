package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.catalog2Vo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    @Autowired
    RedissonClient redisson;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }
    @Override
    public List<CategoryEntity> listWithTree() {
        // 1.查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        // 2.组装成父子树型结构
        // 2.1 找到所有的一级分类
//        List<CategoryEntity> leve1Menus = entities.stream().filter((CategoryEntity)->{
//            return CategoryEntity.getParentCid() == 0;
//        }).collect(Collectors.toList());
        // 简化写法
        List<CategoryEntity> leve1Menus = entities.stream().filter(CategoryEntity->
            CategoryEntity.getParentCid() == 0
        ).map((menu)->{
            menu.setChildren(getChildens(menu,entities));
            return menu;
        }).sorted((menu1,menu2)->{
            return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());
        return leve1Menus;
    }
    @Override

    public void removeMenuByIds(List<Long> asList) {
        //TODO 1、检查当前删除的菜单，是否被别的地方引用
        baseMapper.deleteBatchIds(asList);
    }
    /**
     * 获取catelogId的完整路径
     * @param catelogId
     * @return
     */
    @Override
    public Long[] findCategoryPath(Long catelogId) {
        // [2,25,225]
        List<Long> paths = new ArrayList<>();
        List<Long> partentPath = findPartentPath(catelogId, paths);
        // 使用集合工具类逆序转换partentPath（225,25,2 -> 2,25,225）
        Collections.reverse(partentPath);
        return partentPath.toArray(new Long[partentPath.size()]);
    }
    /**
     * 级联更新所欲的关联数据
     * @param category
     */
    @CacheEvict(value = "category",allEntries = true)
    @Override
    public void updateCascade(CategoryEntity category) {
        // 保证冗余字段的数据一致
        // 先把自己表里的分类更新
        this.updateById(category);
        // 若此次更新有更新分类名字
        if(StringUtils.isNotEmpty(category.getName())){
            // 同步更新其他关联表中的数据，注入CategoryBrandRelationService
            categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
            //TODO 更新其他关联
        }
    }
     /*获得所有的一级分类*/
    @Cacheable(value = "category",key = "#root.methodName",sync = true)
    @Override
    public List<CategoryEntity> getLevel1Category() {
        List<CategoryEntity> resList = baseMapper.selectList(
                new QueryWrapper<CategoryEntity>().eq("parent_cid", 0)
        );
        return resList;
    }
    //cacheable允许了缓存为空值
    @Override
    @Cacheable( value = "category",key = "#root.methodName",sync = true)
    public Map<String, List<catalog2Vo>> getCatalogJson() {

        List<CategoryEntity> entityList = this.list();  //查询所有总分类
            List<CategoryEntity> level1Catefory =  getParent_cid(entityList,0l);  //查询所有一级分类
            Map<String, List<catalog2Vo>> map = level1Catefory.stream().collect(Collectors.toMap(k -> {
                return k.getCatId().toString();  //封装后的map的key为一级分类的id
            }, v -> {
                List<catalog2Vo> resList = new ArrayList<catalog2Vo>();     //用于封装二级分类
                ArrayList<catalog2Vo.Catalog3Vo> catalog3Vos = new ArrayList<>();//用于封装三级分类
                Long catId = v.getCatId();  //获取一级分类的id
                List<CategoryEntity> twoCatagorys = getParent_cid(entityList,catId);  //获取到一级分类所属的二级分类
                twoCatagorys.stream().forEach(category2Entity -> {
                    Long entityCatIdId = category2Entity.getCatId(); //二级分类的id
                    List<CategoryEntity> list =    getParent_cid(entityList,entityCatIdId);  //获取到所有三级分类
                    list.stream().forEach(category3Entity -> {
                        com.atguigu.gulimall.product.vo.catalog2Vo.Catalog3Vo catalog3Vo = new catalog2Vo.Catalog3Vo(
                                category3Entity.getCatId().toString(),
                                category3Entity.getParentCid().toString(),
                                category3Entity.getName()
                        );
                        catalog3Vos.add(catalog3Vo); //封装三级集合
                    });
                    catalog2Vo catalog2Vo = new catalog2Vo(
                            category2Entity.getParentCid().toString(),
                            catalog3Vos,   //三级集合放入二级分类
                            category2Entity.getCatId().toString(),
                            category2Entity.getName());
                    resList.add(catalog2Vo);   //封装二级分类
                });
                return resList;   //  最终map的key为一级分类id，value为二级分类集合
            }));
            return map;
    }



    //    TODO 分布式锁
    /*  构建三级分类结构  */
    public Map<String, List<catalog2Vo>> buildCatalogJson3() {
        String catalogJSON = (String) redisTemplate.opsForValue().get("catalogJSON");
        if(StringUtils.isEmpty(catalogJSON)){
            Map<String, List<catalog2Vo>> stringListMap = getCatalogJsonFmDb();
            if(stringListMap==null){
                redisTemplate.opsForValue().set("catalogJSON",null);  //防止缓存穿透
            }
            String s = JSON.toJSONString(stringListMap);
            return stringListMap;
        }
//        System.out.println("缓存命中,不用查询数据库"+Thread.currentThread().getName());
        Map<String, List<catalog2Vo>> resMap = JSON.parseObject(
                catalogJSON, new TypeReference<Map<String, List<catalog2Vo>>>() {
        });
        return resMap;
    }


    /* 分布式锁防止缓存击穿，传统写法 */
    public Map<String, List<catalog2Vo>> getCatalogJsonFmDb() {
        RLock productLock = redisson.getLock("product-tree");
        productLock.lock();
         System.out.println("获取分布式锁成功"+Thread.currentThread().getName());
        /*
        1.假如锁1还没释放就过期了，这是会有其它线程获得锁，这时锁1释放可能会把其它线程的锁释放了，因此每个线程都需要一个独一无二的锁
         */
//        String uuid = UUID.randomUUID().toString();
//        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid,10,TimeUnit.HOURS);//设置锁,并且设置过期时间,防止死锁
//        if(lock){  //设置成功
        Map<String, List<catalog2Vo>> map=null  ;
        try{
        map=getCatalogJsonDataFmDb();//返回数据
        }  catch (Exception e){
            e.printStackTrace();
        }
        finally {
         System.out.println("分布式锁解锁"+Thread.currentThread().getName());
         productLock.unlock();
        }
            /*
         //根据锁的值判断是不是同一个锁，同一个就释放 ，
         但是在向redis获得数据会损耗时间，这段时间可能刚好锁过期，造成无法释放锁，所以获得锁的值和释放这个锁要是原子的
           if (redisTemplate.opsForValue().get("lock").equals(uuid)){
                redisTemplate.delete("lock");   //释放锁
            }
             */
//            boolean lock1 = releaseLock("lock", uuid);
        return map;  //返回数据
//            System.out.println("释放锁成功"+Thread.currentThread().getName());
//        }else{     //没有获取到锁，等待然后再次询问
//            System.out.println("获取分布式锁失败"+Thread.currentThread().getName());
//            try{
//                Thread.sleep(200);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return getCatalogJsonFmDb();  //自旋锁
//        }
    }

    /*redistemplate实现分布式锁，lua脚本进行解锁 */
    public boolean releaseLock(String key, String value) {
        //如果redis执行了get keys1的命令而且等于argv1的值就执行删除这个keys1 否则就返回0
        String lua = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(lua, Long.class);
        Long result = (Long) redisTemplate.execute(redisScript, Collections.singletonList(key), value);
        return 1L == result;
    }

    /*    从数据库中获取到分类好的数据列表 */
    public Map<String, List<catalog2Vo>> getCatalogJsonDataFmDb() {
        String catalogJSON = (String) redisTemplate.opsForValue().get("catalogJSON");
        if(StringUtils.isEmpty(catalogJSON)) {
            System.out.println("缓存不命中，查询数据库"+Thread.currentThread().getName());
            List<CategoryEntity> entityList = this.list();  //查询所有总分类
            List<CategoryEntity> level1Catefory =  getParent_cid(entityList,0l);  //查询所有一级分类
            Map<String, List<catalog2Vo>> map = level1Catefory.stream().collect(Collectors.toMap(k -> {
                return k.getCatId().toString();  //封装后的map的key为一级分类的id
            }, v -> {
                List<catalog2Vo> resList = new ArrayList<catalog2Vo>();     //用于封装二级分类
                ArrayList<catalog2Vo.Catalog3Vo> catalog3Vos = new ArrayList<>();//用于封装三级分类
                Long catId = v.getCatId();  //获取一级分类的id
                List<CategoryEntity> twoCatagorys = getParent_cid(entityList,catId);  //获取到一级分类所属的二级分类
                twoCatagorys.stream().forEach(category2Entity -> {
                    Long entityCatIdId = category2Entity.getCatId(); //二级分类的id
                    List<CategoryEntity> list =    getParent_cid(entityList,entityCatIdId);  //获取到所有三级分类
                    list.stream().forEach(category3Entity -> {
                        com.atguigu.gulimall.product.vo.catalog2Vo.Catalog3Vo catalog3Vo = new catalog2Vo.Catalog3Vo(
                                category3Entity.getCatId().toString(),
                                category3Entity.getParentCid().toString(),
                                category3Entity.getName()
                        );
                        catalog3Vos.add(catalog3Vo); //封装三级集合
                    });
                    catalog2Vo catalog2Vo = new catalog2Vo(
                            category2Entity.getParentCid().toString(),
                            catalog3Vos,   //三级集合放入二级分类
                            category2Entity.getCatId().toString(),
                            category2Entity.getName());
                    resList.add(catalog2Vo);   //封装二级分类
                });
                return resList;   //  最终map的key为一级分类id，value为二级分类集合
            }));
//            有了@cacheable就不用在这里往缓存里设置值了
//            redisTemplate.opsForValue().set
//                    ("getCatalogJson",JSON.toJSONString(map),new Random().nextInt(20*6),TimeUnit.MINUTES);//解决缓存雪崩
            return map;
        }else{
//            缓存已命中，直接转换格式返回
            Map<String, List<catalog2Vo>> resMap = JSON.parseObject(
                    catalogJSON, new TypeReference<Map<String, List<catalog2Vo>>>() {
                    });
            return  resMap;
        }
    }


    /*获取父id所属的所有分类,减少了从数据库查询的次数*/
    private List<CategoryEntity>  getParent_cid(List<CategoryEntity> categoryEntityList,Long catId) {
        List<CategoryEntity> list = categoryEntityList.stream().filter(categoryEntity -> {return categoryEntity.getParentCid().equals(catId);}
        ).collect(Collectors.toList());
        return list;
    }

    // 225,25,2
    private List<Long> findPartentPath(Long catelogId, List<Long> paths) {
        // 收集当前节点id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0){ //如果当前id存在父id
            findPartentPath(byId.getParentCid(), paths);
        }
        return paths;
    }

    //遍历查找所有菜单的子菜单
    private List<CategoryEntity> getChildens(CategoryEntity root,List<CategoryEntity> all){
        List<CategoryEntity> childen = all.stream().filter((CategoryEntity)->{
            return CategoryEntity.getParentCid().equals(root.getCatId());
        }).map((CategoryEntity)->{
            // 1.找到子菜单
            CategoryEntity.setChildren(getChildens(CategoryEntity,all));
            return CategoryEntity;
        }).sorted((menu1,menu2)->{
            // 2.菜单的排序
            return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());
        return childen;
    }

}