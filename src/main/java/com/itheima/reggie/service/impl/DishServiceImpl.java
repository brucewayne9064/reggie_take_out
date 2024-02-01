package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.Dto.DishDto;
import com.itheima.reggie.dao.DishDao;
import com.itheima.reggie.dao.DishFlavorDao;
import com.itheima.reggie.domain.Dish;
import com.itheima.reggie.domain.DishFlavor;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishDao, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Override
    @Transactional
    public void saveWithFlavors(DishDto dishDto) {
        //保存菜品的基本信息到dish
        this.save(dishDto);

        Long dishId = dishDto.getId();
        List<DishFlavor> dishFlavors = dishDto.getFlavors();

        for (DishFlavor dishFlavor : dishFlavors) {
            dishFlavor.setDishId(dishId);
        }

//        dishFlavors = dishFlavors.stream().map((item) -> {
//            item.setDishId(dishId);
//            return item;
//        }).collect(Collectors.toList());


        //保存菜品的口味信息到dish_flavor
        dishFlavorService.saveBatch(dishFlavors);

    }

    @Override
    @Transactional
    public DishDto getByIdWithFlavors(Long id) {
        //根据id查询菜品的基本信息
        Dish dish = this.getById(id);
        //根据id查询菜品的口味信息

        //将dish的属性拷贝到dishDto
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);

        //根据dish的id查询dish_flavor表中的数据
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> dishFlavors = dishFlavorService.list(lambdaQueryWrapper);

        //将dishFlavors设置到dishDto中
        dishDto.setFlavors(dishFlavors);


        return dishDto;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表中的数据
        this.updateById(dishDto);

        //跟新dish_flavor表中的数据
        //先删除dish_flavor表中的数据delete,发送的sql是delete from dish_flavor where dish_id = ?
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(lambdaQueryWrapper);
        //再插入当前的数据insert
        List<DishFlavor> flavors = dishDto.getFlavors();
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishDto.getId());
        }
        dishFlavorService.saveBatch(flavors);

    }
}
