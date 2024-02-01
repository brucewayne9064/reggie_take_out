package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.Dto.SetmealDto;
import com.itheima.reggie.domain.Setmeal;

import java.util.List;

public interface SetMealService extends IService<Setmeal> {
    public void saveWithDish(SetmealDto setmealDto);//新增套餐,同时新增套餐和菜品的关联关系

    public void remove(List<Long> id);

    public void updateStatus(List<Long> ids, Integer status);
}
