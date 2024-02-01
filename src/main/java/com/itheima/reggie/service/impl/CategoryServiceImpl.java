package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dao.CategoryDao;
import com.itheima.reggie.domain.Category;
import com.itheima.reggie.domain.Dish;
import com.itheima.reggie.domain.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetMealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, Category> implements CategoryService {


    @Autowired
    private DishService dishService;

    @Autowired
    private SetMealService setMealService;

    /*
    *  根据id删除分类，但是要判断该分类下是否有商品，如果有商品则不能删除
    * */
    @Override
    public void remove(Long id) {
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<Setmeal> setMealLambdaQueryWrapper = new LambdaQueryWrapper<>();

        //添加查询条件，根据分类id查询菜品
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
        int countDish = dishService.count(dishLambdaQueryWrapper); //查询菜品数量

        //查询当前分类是否关联了菜品，如果已经关联，抛出一个业务异常
        if(countDish > 0){
            throw new CustomException("该分类下有菜品，不能删除");
        }

        //添加查询条件，根据分类id查询套餐
        setMealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        int countSetMeal = setMealService.count(setMealLambdaQueryWrapper);

        //查询当前分类是否关联了套餐，如果已经关联，抛出一个业务异常
        if(countSetMeal > 0){
            throw new CustomException("该分类下有套餐，不能删除");
        }


        //正常删除分类
        super.removeById(id);
    }
}
