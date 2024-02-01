package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.Dto.DishDto;
import com.itheima.reggie.domain.Dish;

public interface DishService extends IService<Dish> {

    //保存菜品信息
    public void saveWithFlavors(DishDto dishDto);

    //根据id查询菜品的基本信息和口味信息
    public DishDto getByIdWithFlavors(Long id);

    //更新菜品信息
    public void updateWithFlavor(DishDto dishDto);
}
