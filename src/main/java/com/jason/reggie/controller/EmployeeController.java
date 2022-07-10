package com.jason.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jason.reggie.common.R;
import com.jason.reggie.entity.Employee;
import com.jason.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;


    /**
     * 员工登录
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //1.将页面提交的密码获取并md5加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2.根据提交的用户名获取数据库用户信息
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);//数据库设置了用户名唯一

        //3.如果没有查询到返回失败结果
        if(emp == null){
            return R.error("用户不存在,登录失败");
        }

        //4.密码比对
        if(!emp.getPassword().equals(password)){
            return R.error("密码错误");
        }

        //5.查看员工状态是否禁用
        if(emp.getStatus() == 0){
            return R.error("用户账号已禁用");
        }

        //6.登陆成功，并将用户id保存到服务器session中
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    /**
     * 登出功能
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("employee");
        return R.success("退出登录成功");
    }

    /**
     * 新增员工功能
     * @param request
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        log.info("新增员工，员工信息:{}",employee.toString());
//        //查看当前用户名是否已经存在(用异常处理替代了)
//        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(Employee::getUsername,employee.getUsername());
//        Employee emp = employeeService.getOne(queryWrapper);
//        if(employeeService.getOne(queryWrapper)!=null){
//            return  R.error("员工账号已存在");
//        }

        //设置初始密码,使用mb5加密123456
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

//        //设置创建时间
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//
//        //设置该用户的创建者,获取当前登录用户的id
//        Long empId  = (Long) request.getSession().getAttribute("employee");
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);

        boolean save = employeeService.save(employee);
        if(save){
            return R.success("新增员工成功");
        }
        else{
            return R.error("添加失败");
        }
    }

    /**
     * 获取员工列表分页功能
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        log.info("page={},pageSize={},name={}",page,pageSize,name);
        //构造分页构造器
        Page pageInfo = new Page(page,pageSize);

        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();

        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);

        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询
        //返回值和pageinfo为同一个对象
        employeeService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 修改员工启用禁用状态
     * @param request
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        log.info("update操作,employee:{}",employee);
        //获取当前登录用户的ID，设置修改人
        Long empId = (Long) request.getSession().getAttribute("employee");
//        employee.setUpdateUser(empId);
//        employee.setUpdateTime(LocalDateTime.now());

        employeeService.updateById(employee);
        return R.success("修改成功");
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据Id查询");
        Employee employee = employeeService.getById(id);
        if(employee!=null){
            return R.success(employee);
        }
        return R.error("没有查询到对应员工信息");
    }

}
