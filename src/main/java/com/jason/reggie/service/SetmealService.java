package com.jason.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jason.reggie.dto.SetmealDto;
import com.jason.reggie.entity.Setmeal;


public interface SetmealService extends IService<Setmeal> {
    public Page<SetmealDto> mypage(int page, int pageSize, String name);
    public void savewithDishes(SetmealDto setmealDto);
    public SetmealDto updatewithdishes(Long ids);
    public void statusupdatebatch(String ids,int status);
    public void setmealdeletebatch(String ids);
    public void updatecommit(SetmealDto setmealDto);
}
