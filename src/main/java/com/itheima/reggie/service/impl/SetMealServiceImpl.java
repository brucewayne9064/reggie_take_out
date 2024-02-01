package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.Dto.SetmealDto;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dao.SetMealDao;
import com.itheima.reggie.domain.Setmeal;
import com.itheima.reggie.domain.SetmealDish;
import com.itheima.reggie.service.SetMealDishService;
import com.itheima.reggie.service.SetMealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class SetMealServiceImpl extends ServiceImpl<SetMealDao, Setmeal> implements SetMealService {

    @Autowired
    private SetMealDishService setMealDishService;



    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息，操作setmeal，执行insert操作
        this.save(setmealDto); //setmealDto是Setmeal的子类，所以可以直接传入
        //保存套餐和菜品的关联关系，操作setmeal_dish，执行insert操作

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes(); //获取setmealDto中的setmealDishes
        //遍历setmealDishes，设置setmealId
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealDto.getId());
        }
        //执行批量插入
        setMealDishService.saveBatch(setmealDishes);

    }

    /*
    *  根据id删除套餐，但是要判断套餐是否已经上架，如果已经上架，不能删除
     */
    @Override
    @Transactional
    public void remove(List<Long> id) {

        //sql: select count(*) from setmeal where id in (1,2,3) and status = 1
        //根据id查询套餐，判断套餐是否已经上架，如果已经上架，不能删除
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(Setmeal::getId, id);
        lambdaQueryWrapper.eq(Setmeal::getStatus, 1);
        int count = this.count(lambdaQueryWrapper);

        //查询当前套餐是否已经上架，如果已经上架，抛出一个业务异常
        if(count > 0){
            throw new CustomException("该套餐已经上架，不能删除");
        }

        //根据id删除套餐
        this.removeByIds(id);


        //根据套餐id查询套餐和菜品的关联关系
        //sql: select * from setmeal_dish where setmeal_id in (1,2,3)
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SetmealDish::getSetmealId, id);

        //根据套餐id删除套餐和菜品的关联关系
        setMealDishService.remove(queryWrapper);


    }

    @Override
    public void updateStatus(List<Long> ids, Integer status) {
        for (Long id : ids) {
            //根据id查询套餐
            Setmeal setmeal = this.getById(id);
            //设置套餐的状态
            setmeal.setStatus(status);
            //更新套餐
            this.updateById(setmeal);
        }
    }
}
