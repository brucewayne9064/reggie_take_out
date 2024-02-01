package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.domain.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.SMSUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;


    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){

        //获取手机号
        String mobile = user.getPhone();

        if(StringUtils.isNotEmpty(mobile)){
            //生成验证码,4位
            String validateCode = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("生成的验证码为：{}", validateCode);

            //调用短信api发送短信
            //SMSUtils.sendMessage("瑞吉外卖", "", mobile, validateCode);

            //将验证码存入session
            session.setAttribute(mobile , validateCode);

            return R.success("手机验证码发送成功");
        }

        return R.error("发送失败");

    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map<String,String> user, HttpSession session){

        log.info("用户登录信息：{}", user);

        //获取手机号
        String mobile = user.get("phone").toString();
        //获取验证码
        String code = user.get("code").toString();
        //从session中得到手机号和验证码，key是手机号，value是验证码
        String validateCode = (String) session.getAttribute(mobile);

        if(validateCode != null && validateCode.equals(code)){
            //验证码正确
            //判断用户是否存在
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, mobile);
            User user1 = userService.getOne(queryWrapper);
            if(user1 == null) {
                //用户不存在，注册
                user1 = new User();
                user1.setPhone(mobile);
                user1.setStatus(1);
                userService.save(user1);
            }
            session.setAttribute("user", user1.getId());
            return R.success(user1);
        }
        return R.error("验证码错误");
    }

}
