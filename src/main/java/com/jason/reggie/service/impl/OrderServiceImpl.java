package com.jason.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jason.reggie.common.BaseContext;
import com.jason.reggie.dto.OrdersDto;
import com.jason.reggie.entity.AddressBook;
import com.jason.reggie.entity.OrderDetail;
import com.jason.reggie.entity.Orders;
import com.jason.reggie.entity.ShoppingCart;
import com.jason.reggie.mapper.OrdersMapper;
import com.jason.reggie.service.AddressBookService;
import com.jason.reggie.service.OrderDetailService;
import com.jason.reggie.service.OrderService;
import com.jason.reggie.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrderService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;
    /**
     * 集合所有信息到订单中
     * @param orders
     */
    @Override
    @Transactional
    public void submit(Orders orders) {
        Long currentId = BaseContext.getCurrentId();
        //获取用户地址
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId,currentId).eq(AddressBook::getIsDefault,1);
        AddressBook address = addressBookService.getOne(queryWrapper);

        //获取用户当前购物车列表
        LambdaQueryWrapper<ShoppingCart> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(ShoppingCart::getUserId,currentId);
        List<ShoppingCart> cartList = shoppingCartService.list(queryWrapper1);

        long orderId = IdWorker.getId();

        //原子int，多线程安全的变量,保证值正确
        AtomicInteger amount = new AtomicInteger(0);

        //计算价格同时保存菜品对象给详细订单表使用
        List<OrderDetail> detailList = cartList.stream().map((i) -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(i.getNumber());
            orderDetail.setDishFlavor(i.getDishFlavor());
            orderDetail.setDishId(i.getDishId());
            orderDetail.setSetmealId(i.getSetmealId());
            orderDetail.setName(i.getName());
            orderDetail.setImage(i.getImage());
            orderDetail.setAmount(i.getAmount());
            //金额乘以份数再转化为int
            amount.addAndGet(i.getAmount().multiply(new BigDecimal(i.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());

        //设置order各种信息
        orders.setId(orderId);
        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        orders.setAddress(address.getDetail());
        orders.setAddressBookId(address.getId());
        orders.setUserId(address.getUserId());
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        //设置收货人
        orders.setConsignee(address.getConsignee());
        orders.setPhone(address.getPhone());
        orders.setNumber(String.valueOf(orderId));
        orders.setAmount(new BigDecimal(amount.get()));
        orders.setStatus(2);

        this.save(orders);

        orderDetailService.saveBatch(detailList);

        //储存完成后清空购物车
        shoppingCartService.remove(queryWrapper1);

    }

    @Override
    public Page<OrdersDto> mypage(int page, int pageSize) {
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>();
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId, BaseContext.getCurrentId());
        this.page(pageInfo,queryWrapper);

        BeanUtils.copyProperties(pageInfo,ordersDtoPage,"records");

        List<Orders> records = pageInfo.getRecords();
        List<OrdersDto> collect = records.stream().map((i) -> {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(i, ordersDto);
            LambdaQueryWrapper<OrderDetail> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(OrderDetail::getOrderId, i.getId());
            List<OrderDetail> list = orderDetailService.list(queryWrapper1);
            ordersDto.setOrderDetails(list);
            return ordersDto;
        }).collect(Collectors.toList());
        ordersDtoPage.setRecords(collect);
        return ordersDtoPage;
    }
}
