package com.itheima.reggie.Dto;


import com.itheima.reggie.domain.Dish;
import com.itheima.reggie.domain.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/*
*  菜品数据传输对象,用于封装菜品的口味
 */
@Data
public class DishDto extends Dish {

    //封装菜品的口味
    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
