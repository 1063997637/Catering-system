package com.jason.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/*
全局异常捕获
所有标记了RestController,RestController注解的类会被设置异常处理
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {
    /**
     * sql异常处理方法
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error(ex.getMessage());
        if(ex.getMessage().contains("Duplicate entry")){
            String[] s = ex.getMessage().split(" ");
            return R.error("已经存在"+s[2]);
        }
        return R.error("未知异常");
    }

    @ExceptionHandler(CustomException.class)
    public R<String> excepetionHandler(CustomException ex){
        log.error(ex.getMessage());
        return R.error(ex.getMessage());
    }
}
