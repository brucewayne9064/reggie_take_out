package com.itheima.reggie.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.domain.Category;
import org.apache.ibatis.annotations.Mapper;

//这里用的是mybatis-plus的BaseMapper接口，只需要继承就可以了
@Mapper
public interface CategoryDao extends BaseMapper<Category>{
}
