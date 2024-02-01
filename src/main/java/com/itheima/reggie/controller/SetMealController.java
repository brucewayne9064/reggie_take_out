package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.Dto.SetmealDto;
import com.itheima.reggie.common.R;
import com.itheima.reggie.domain.Category;
import com.itheima.reggie.domain.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetMealDishService;
import com.itheima.reggie.service.SetMealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetMealController {
    @Autowired
    private SetMealService setMealService;

    @Autowired
    private SetMealDishService setMealDishService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping({"/page"})
    public R<Page<SetmealDto>> page(int page, int pageSize, String name){
        log.info("page = {}, pageSize = {}", page,pageSize);
        //构造分页构造器
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> pageDtoInfo = new Page<>();


        //构造条件构造器
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        //添加排序条件
        lambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);

        //添加模糊查询条件
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(name),Setmeal::getName, name);

        //执行查询
        setMealService.page(pageInfo, lambdaQueryWrapper);


        //对象拷贝,将pageInfo的属性拷贝到pageDtoInfo,但是不拷贝records属性,因为records属性是Dish类型的,而pageDtoInfo的records属性是DishDto类型的
        BeanUtils.copyProperties(pageInfo, pageDtoInfo, "records");
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> list =  records.stream().map((item)->{
            SetmealDto setmealDto = new SetmealDto();

            BeanUtils.copyProperties(item, setmealDto);

            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if(category != null){
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }

            return setmealDto;
        }).collect(Collectors.toList());

        pageDtoInfo.setRecords(list);

        return R.success(pageDtoInfo);

    }


    //新增套餐
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("setmealDto = {}", setmealDto);
        setMealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }


    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        // pathvariable是用来接收路径参数的，requestparam是用来接收请求参数的，复杂类型要加@RequestParam，简单类型可以不加
        //请求参数（Request Parameters）：请求参数通常用于GET请求中，附加在URL后面，
        // 以键值对的形式出现，多个键值对之间用&分隔，比如http://example.com/api?param1=value1&param2=value2。
        // 请求参数也可以在POST或其他类型的请求中使用，此时它们通常包含在请求体中。
        // 请求参数适用于传递较小的、非敏感的数据，或者用于过滤、排序或分页的参数。
        //
        //路径参数（Path Variables）：路径参数是嵌入在URL路径中的，比如http://example.com/api/resource/{id}，
        // 其中{id}就是一个路径参数。路径参数通常用于标识特定的资源。
        // 由于路径参数是URL的一部分，因此它们应该是安全的、可编码的，
        // 并且不应该包含任何URL中的保留字符。
        log.info("ids = {}", ids);
        setMealService.remove(ids);
        return R.success("删除套餐成功");
    }

    //http://localhost/setmeal/status/0?ids=1751321666778161153
    @PostMapping("/status/{status}")
    public R<String> setStatues(@PathVariable int status,@RequestParam List<Long> ids){
        log.info("ids = {}, status = {}", ids, status);
        setMealService.updateStatus(ids, status);
        return R.success("修改套餐状态成功");

    }


    @GetMapping("/list")
    public R<List<Setmeal>> list(Long categoryId, int status){
        log.info("categoryId = {}, status = {}", categoryId, status);
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(categoryId != null ,Setmeal::getCategoryId, categoryId);
        lambdaQueryWrapper.eq(Setmeal::getStatus, status);
        lambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setMealService.list(lambdaQueryWrapper);
        return R.success(list);

    }


}
