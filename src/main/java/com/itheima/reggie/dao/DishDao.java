package com.itheima.reggie.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.domain.Dish;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DishDao extends BaseMapper<Dish> {

}
