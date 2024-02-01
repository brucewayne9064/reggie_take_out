package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.domain.Category;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
*  商品分类管理
* */


@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;


    /*
    *  新增商品分类
    * */
    @PostMapping
    public R<String> save(@RequestBody Category category){
        log.info( "新增商品分类:{}",category);
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    /*
    *  查询商品分类
    * */
    @GetMapping("/page")
    public R<Page<Category>> page(int page, int pageSize){
        log.info("page = {}, pageSize = {}", page,pageSize);
        //构造分页构造器
        Page pageInfo = new Page(page, pageSize);

        //构造条件构造器
        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加排序条件
        lambdaQueryWrapper.orderByAsc(Category::getSort);

        //执行查询
        categoryService.page(pageInfo, lambdaQueryWrapper);
        return R.success(pageInfo);

    }


    /*
    *  修改商品分类
    *
     */
    @DeleteMapping
    public R<String> delete(Long ids){
        log.info("ids = {}", ids);
        //categoryService.removeById(ids);
        categoryService.remove(ids);
        return R.success( "删除分类成功");
    }

    @PutMapping
    public R<String> update(@RequestBody Category category){
        log.info( "修改商品分类:{}",category);
        categoryService.updateById(category);
        return R.success("修改分类成功");
    }


    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        log.info("category = {}", category);

        //条件构造器
        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加排序条件
        lambdaQueryWrapper.eq(category.getType()!= null, Category::getType, category.getType());
        //添加排序条件
        lambdaQueryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        //执行查询
        List<Category>  categoryList = categoryService.list(lambdaQueryWrapper);
        return R.success(categoryList);
    }

}
