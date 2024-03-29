package com.itheima.reggie.Dto;


import com.itheima.reggie.domain.Setmeal;
import com.itheima.reggie.domain.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
