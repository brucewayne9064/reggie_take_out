package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.domain.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;


    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("shoppingCart:{}",shoppingCart);
        //获取user id
        Long user = BaseContext.getCurrentId();
        //给shoppingCart设置userId
        shoppingCart.setUserId(user);

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,user);

        //查询当前菜品或者套餐是否已经存在购物车中
        // 口味不一样也算不一样,但是前端做了限制，口味不一样不能添加到购物车
        Long dishId = shoppingCart.getDishId();
        if(dishId != null){
            //填加到购物车的是菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
//            String dishFlavor = shoppingCart.getDishFlavor();
//            queryWrapper.eq(ShoppingCart::getDishFlavor,dishFlavor);
        }else {
            //填加到购物车的是套餐
            Long setmealId = shoppingCart.getSetmealId();
            queryWrapper.eq(ShoppingCart::getSetmealId,setmealId);
        }
        // select * from shopping_cart where user_id = ? and ((dish_id = ?) or setmeal_id = ?)
        ShoppingCart shoppingCartGetOne = shoppingCartService.getOne(queryWrapper);


        //如果已经存在，直接在数量上加一
        if(shoppingCartGetOne != null){
            shoppingCartGetOne.setNumber(shoppingCartGetOne.getNumber()+1);
            shoppingCartService.updateById(shoppingCartGetOne);

        } else {
            //如果不存在，添加到购物车，数量为1
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            shoppingCartGetOne = shoppingCart;
        }
        //返回结果
        return R.success(shoppingCartGetOne);
    }

    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        log.info("查询购物车");
        Long user = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,user);
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(queryWrapper);
        return R.success(shoppingCartList);
    }


    @DeleteMapping("/clean")
    public R<String> clean(){
        log.info("清空购物车");
        Long user = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,user);
        shoppingCartService.remove(queryWrapper);
        return R.success("清空购物车成功");
    }

    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){

        log.info("shoppingCart:{}",shoppingCart);
        //获取user id
        Long user = BaseContext.getCurrentId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,user);

        //查询当前菜品或者套餐是否已经存在购物车中
        Long dishId = shoppingCart.getDishId();
        if(dishId != null){
            //说明是菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else {
            //说明是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        // select * from shopping_cart where user_id = ? and ((dish_id = ?) or setmeal_id = ?)
        ShoppingCart shoppingCartGetOne = shoppingCartService.getOne(queryWrapper);
        if(shoppingCartGetOne != null){  //说明购物车中有这个菜品或者套餐
            Integer number = shoppingCartGetOne.getNumber();
            if(number > 1){
                //说明数量大于1，可以减一
                shoppingCartGetOne.setNumber(number-1);
                shoppingCartService.updateById(shoppingCartGetOne);
            }else {
                //说明数量等于1，直接删除
                shoppingCartService.removeById(shoppingCartGetOne.getId());
                shoppingCartGetOne.setNumber(0);
            }
        }
        //返回结果, 如果减一了，返回减一后的结果，如果删除了，返回删除之前的shoppingCartGetOne但是number为0
        return R.success(shoppingCartGetOne);

    }
}
