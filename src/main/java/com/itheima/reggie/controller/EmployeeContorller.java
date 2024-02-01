package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.domain.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeContorller {

    @Autowired
    private EmployeeService employeeService;

    /*
     *
     * 登录
     * @parm request
     * @parm employee
     * @return R<Employee>
     *
     * */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        //1、将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());


        //2、根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);


        //3、如果没有查询到则返回登录失败结果
        if (null == emp) {
            return R.error("登录失败");
        }


        //4、密码对比，如果不一致则返回登录失败结果
        if (!emp.getPassword().equals(password)) {
            return R.error("登录失败");
        }


        //5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if (emp.getStatus() == 0) {
            return R.error("账号已禁用");
        }

        //6、登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);
    }

    /*
     *
     * 退出
     * @parm request
     * @return R<String>
     *
     * */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }


    /*
     *
     * 保存员工
     * @parm employee
     * @return R<String>
     *
     * 新增员工的时候，没有输入密码，所以需要统一设置一个默认密码，比如：123456，后面可以通过修改密码的方式修改，
     * 需要在新增员工的时候，将密码进行md5加密处理
     */
    @PostMapping
    public R<String> save(@RequestBody Employee employee, HttpServletRequest request) {
        log.info("新增员工信息为：{}", employee.toString());
        //1、设置默认密码,并进行md5加密处理,初始化密码为123456
        String password = DigestUtils.md5DigestAsHex("123456".getBytes(StandardCharsets.UTF_8));
        employee.setPassword(password);


        //2、设置创建时间
        //employee.setCreateTime(LocalDateTime.now());

        //3、设置更新时间
        //employee.setUpdateTime(LocalDateTime.now());

        //4、设置创建人
        //long employeeId = (long) request.getSession().getAttribute("employee");
        //employee.setCreateUser(employeeId);

        //5、设置更新人
        //employee.setUpdateUser(employeeId);

        //6、返回保存结果
        employeeService.save(employee);
        return R.success("新增员工成功");
    }

    /*
    * 员工信息分页查询
    * */
    @GetMapping("/page")
    public R<Page<Employee>> page(int page, int pageSize, String name) {
        log.info("page = {}, pageSize = {}, name = {}", page,pageSize, name);
        //构造分页构造器
        Page pageInfo = new Page(page,pageSize);

        //构造条件构造器
        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件(模糊查询)
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        //添加排序条件
        lambdaQueryWrapper.orderByDesc(Employee::getUpdateTime);


        //执行查询
        employeeService.page(pageInfo, lambdaQueryWrapper);
        return R.success(pageInfo);
    }


    //通用的update方法
    @PutMapping
    public R<String> update(@RequestBody Employee employee, HttpServletRequest httpServletRequest){
        log.info(employee.toString());
        //获取当前线程id
        long id = Thread.currentThread().getId();
        log.info("当前线程id为：{}", id);
        //管理员防止自己把自己禁用
        if(employee.getId().equals(httpServletRequest.getSession().getAttribute("employee"))){
            return R.error("不能禁用自己");
        }
        //statue已经在前端发送的时候改好了，发0代表禁用了
        //employee.setUpdateTime(LocalDateTime.now());
        //employee.setUpdateUser((Long)httpServletRequest.getSession().getAttribute("employee"));

        employeeService.updateById(employee);

        return R.success("员工信息更新成功");
    }


    /*
    *  根据id查询员工信息
    *  @parm id
    *  @return R<Employee>
    *
    * */
    @GetMapping( "/{id}")
    public R<Employee> getById( @PathVariable Long id){
        log.info("根据id查询员工信息，id = {}", id);
        Employee employee = employeeService.getById(id);
        if(null == employee){
            return R.error("员工信息不存在");
        }
        return R.success(employee);
    }

}

