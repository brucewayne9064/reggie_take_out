package com.itheima.reggie.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

//@Data注解 提供类的get、set、equals、hashCode、canEqual、toString方法, 不需要手动编写，是lombok提供的
@Data
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String name;

    private String password;

    private String phone;

    private String sex;

    private String idNumber;

    private Integer status;

    //这里用的是mybatis-plus的@TableField注解，是用来标识数据库表中的字段的，这里的fill属性是用来标识字段的填充策略的
    @TableField(fill = FieldFill.INSERT) //插入的时候自动填充
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE) //插入和更新的时候自动填充
    private LocalDateTime updateTime;



    @TableField(fill = FieldFill.INSERT)  //插入的时候自动填充
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)  //插入和更新的时候自动填充
    private Long updateUser;

}
