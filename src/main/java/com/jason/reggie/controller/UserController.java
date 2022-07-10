package com.jason.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jason.reggie.common.R;
import com.jason.reggie.entity.User;
import com.jason.reggie.service.UserService;
import com.jason.reggie.utils.SMSUtils;
import com.jason.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        log.info("获取验证码ing");
        //获取手机号
        String phone = user.getPhone();

        if(StringUtils.isNotEmpty(phone)){
            //生成随机的4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();

            log.info("生成的验证码为 "+code);

            //调用阿里云提供的短信服务API完成发送短信
//            SMSUtils.sendMessage("瑞吉外卖,","",phone,code);

            //用其他方式获取验证码完成登录

            //需要将生成的验证码保存到Session
            session.setAttribute(phone,code);

            return R.success("短信发送成功");
        }
        return R.error("短息发送失败");
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){
        log.info("正在登录的用户账号为"+map.get("phone").toString());
        //获取手机号
        String phone = map.get("phone").toString();

        //获取验证码
        String code = map.get("code").toString();

        //从Session中获取保存的验证码
        Object codeInSession = session.getAttribute(phone);

        //进行验证码的比对(页面提交的验证码和Session中保存的验证码比对)
        if(codeInSession != null && codeInSession.equals(code)){
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);

            User one = userService.getOne(queryWrapper);
            //判断当前用户是否为新用户，如果是新用户就自动完成注册
            if(one == null){
                one = new User();
                one.setPhone(phone);
                userService.save(one);
            }
            session.setAttribute("user",one.getId());
            return R.success(one);
        }



        return R.error("登陆失败");
    }
}
