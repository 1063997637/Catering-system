package com.jason.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jason.reggie.common.BaseContext;
import com.jason.reggie.common.R;
import com.jason.reggie.entity.ShoppingCart;
import com.jason.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 获取列表
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        log.info("获取购物车列表ing");
        List<ShoppingCart> list = shoppingCartService.list();
        return R.success(list);
    }

    /**
     * 购物车添加
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<String> add(@RequestBody ShoppingCart shoppingCart){
        log.info("购物车添加ing");
        shoppingCart.setUserId(BaseContext.getCurrentId());
        shoppingCartService.save(shoppingCart);
        return R.success("添加成功");
    }

    /**
     * 购物车删除单个
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<String> sub(@RequestBody ShoppingCart shoppingCart){
        log.info("删除购物车单个菜品ing");
        if(shoppingCart.getDishId()!=null){
            LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
            shoppingCartService.remove(queryWrapper);
        }
        if(shoppingCart.getSetmealId()!=null){
            LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
            shoppingCartService.remove(queryWrapper);
        }
        return R.success("删除成功");
    }

    /**
     * 购物车清除
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> delete(){
        log.info("清除购物车ing");
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);
        return R.success("删除成功");
    }
}
