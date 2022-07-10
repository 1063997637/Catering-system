package com.jason.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jason.reggie.entity.OrderDetail;
import com.jason.reggie.mapper.OrderDetailMapper;
import com.jason.reggie.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderdetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
