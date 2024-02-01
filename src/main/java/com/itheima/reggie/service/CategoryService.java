package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.domain.Category;

public interface CategoryService extends IService<Category> {
    public void remove(Long id);
}
