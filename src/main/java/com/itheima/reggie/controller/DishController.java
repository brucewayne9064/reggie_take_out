package com.itheima.reggie.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.Dto.DishDto;
import com.itheima.reggie.common.R;
import com.itheima.reggie.domain.Category;
import com.itheima.reggie.domain.Dish;
import com.itheima.reggie.domain.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.awt.datatransfer.FlavorEvent;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@RestController
@Slf4j
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @GetMapping("/page")
    public R<Page<DishDto>> page(int page, int pageSize, String name)
    {
        log.info("page = {}, pageSize = {}", page,pageSize);
        //构造分页构造器
        Page<Dish> pageInfo = new Page<>(page, pageSize);  //Dish里面只有category id，没有category name
        Page<DishDto> pageDtoInfo = new Page<>(); //DishDto里面有category name


        //构造条件构造器
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        //添加查询条件
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(name), Dish::getName, name);
        //添加排序条件
        lambdaQueryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行查询
        dishService.page(pageInfo, lambdaQueryWrapper);

        //对象拷贝,将pageInfo的属性拷贝到pageDtoInfo,但是不拷贝records属性,因为records属性是Dish类型的,而pageDtoInfo的records属性是DishDto类型的
        BeanUtils.copyProperties(pageInfo,pageDtoInfo,"records");
        List<Dish> records = pageInfo.getRecords();

        //将Dish类型的records属性转换为DishDto类型的records属性
        List<DishDto> dishDtoList = records.stream().map((item) -> {

                    DishDto dishDto = new DishDto();

                    BeanUtils.copyProperties(item,dishDto);

                    Long categoryId = item.getCategoryId(); //获取菜品分类id
                    Category category = categoryService.getById(categoryId);//根据菜品分类id获取菜品分类名称

                    if(category != null){
                        String categoryName = category.getName();
                        dishDto.setCategoryName(categoryName);
                    }

                    return dishDto;

                }).collect(Collectors.toList());

        pageDtoInfo.setRecords(dishDtoList);


        return R.success(pageDtoInfo);
    }

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info("新增菜品:{}",dishDto);

        dishService.saveWithFlavors(dishDto);

        //清理所有菜品的缓存数据
//        Set<String> keys = stringRedisTemplate.keys("dish_*");
//        stringRedisTemplate.delete(keys);

        //清理当前菜品分类下的缓存数据
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        stringRedisTemplate.delete(key);


        return R.success("新增菜品成功");

    }

    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        log.info("根据id查询菜品信息，id = {}", id);


        DishDto dishDto = dishService.getByIdWithFlavors(id);

        return R.success(dishDto);
    }

    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info("修改菜品:{}",dishDto);

        dishService.updateWithFlavor(dishDto);

        //清理所有菜品的缓存数据
//        Set<String> keys = stringRedisTemplate.keys("dish_*");
//        stringRedisTemplate.delete(keys);

        //清理当前菜品分类下的缓存数据
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        stringRedisTemplate.delete(key);

        return R.success("修改菜品成功");
    }


    //根据条件查询对应的菜品数据
//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish){
//        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>(); //构造条件构造器
//        lambdaQueryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId()); //根据菜品分类id查询
//        lambdaQueryWrapper.eq(Dish::getStatus, 1); //只查询状态为1的菜品
//        lambdaQueryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime); //根据排序字段升序排列，根据更新时间降序排列
//        List<Dish> list = dishService.list(lambdaQueryWrapper); //执行查询
//        return R.success(list);
//
//    }

    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        List<DishDto> Dtolist = null;

        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        //从redis中获取菜品数据
        Object s = stringRedisTemplate.opsForValue().get(key);
        Dtolist = JSON.parseArray((String) s, DishDto.class); //将json字符串转换为java对象

        //如果存在，直接返回，不用查询数据库
        if(Dtolist != null){
            return R.success(Dtolist);
        }


        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>(); //构造条件构造器
        lambdaQueryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId()); //根据菜品分类id查询
        lambdaQueryWrapper.eq(Dish::getStatus, 1); //只查询状态为1的菜品
        lambdaQueryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime); //根据排序字段升序排列，根据更新时间降序排列
        List<Dish> list = dishService.list(lambdaQueryWrapper); //执行查询

        Dtolist = list.stream().map((item) -> {

            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId(); //获取菜品分类id
            Category category = categoryService.getById(categoryId);//根据菜品分类id获取菜品分类名称

            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper1 = new LambdaQueryWrapper<>();
            lambdaQueryWrapper1.eq(DishFlavor::getDishId,item.getId());
            //SQL: select * from dish_flavor where dish_id = ?
            List<DishFlavor> list1 = dishFlavorService.list(lambdaQueryWrapper1);
            dishDto.setFlavors(list1);

            return dishDto;

        }).collect(Collectors.toList());

        //如果不存在，查询数据库，然后存入redis，然后返回
        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(Dtolist), 60, TimeUnit.MINUTES);


        return R.success(Dtolist);

    }



}
