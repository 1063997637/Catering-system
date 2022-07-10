package com.jason.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jason.reggie.common.BaseContext;
import com.jason.reggie.common.R;
import com.jason.reggie.dto.OrdersDto;
import com.jason.reggie.entity.Orders;
import com.jason.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService ordersService;

    @GetMapping("/userPage")
    public R<Page> page(int page,int pageSize){
        log.info("获取当前用户订单列表");
        Page<OrdersDto> mypage = ordersService.mypage(page, pageSize);
        return R.success(mypage);
    }

    /**
     * 购物车提交
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("购物车提交ing");
        ordersService.submit(orders);
        return R.success("支付成功");
    }
}
