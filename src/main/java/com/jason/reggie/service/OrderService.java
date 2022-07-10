package com.jason.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jason.reggie.dto.OrdersDto;
import com.jason.reggie.entity.Orders;

public interface OrderService extends IService<Orders> {
    public void submit(Orders orders);
    public Page<OrdersDto> mypage(int page,int pageSize);
}
