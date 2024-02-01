package com.itheima.reggie.common;

/*
*  全局异常处理器
* */

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;


//aop的思想，对所有的controller进行增强，增强的内容就是异常处理
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler  {

    //处理数据库异常
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException e){
        log.error( "数据库异常: {} ", e.getMessage());
        //如果是唯一索引冲突异常，则返回提示信息
        if(e.getMessage().contains("Duplicate entry"))
        {
            String username = e.getMessage().split(" ")[2].split("'")[1];
            String msg = username + "已存在";
            return R.error(msg);
        }
        return R.error("数据库异常，请联系管理员");
    }


    //处理自定义异常
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException e){
        log.error( "自定义异常: {} ", e.getMessage());
        return R.error(e.getMessage());
    }
}
