package com.itheima.reggie.common;

/*
*  基于ThreadLocal实现的工具类，用于保存和获取当前登录用户的ID
* */

public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    public static Long getCurrentId() {
        return threadLocal.get();
    }

}
