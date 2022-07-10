package com.jason.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jason.reggie.dto.DishDto;
import com.jason.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {
    public void saveWithFlavor(DishDto dishDto);
    public DishDto getByidsWithFlavor(Long ids);
    public void updateWithFlavor(DishDto dishDto);
    public void statusupdatebatch(String ids,int status);
    public void dishdeletebatch(String ids);
    public List<DishDto> alllist(Long categoryId);
}
