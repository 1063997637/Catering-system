package com.jason.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jason.reggie.dto.SetmealDto;
import com.jason.reggie.entity.Category;
import com.jason.reggie.entity.Dish;
import com.jason.reggie.entity.Setmeal;
import com.jason.reggie.entity.SetmealDish;
import com.jason.reggie.mapper.SetmealMapper;
import com.jason.reggie.service.CategoryService;
import com.jason.reggie.service.SetmealDishService;
import com.jason.reggie.service.SetmealService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;
    /**
     * 套餐界面分页实现
     * @param page
     * @param pageSize
     * @param name
     */
    @Override
    public Page<SetmealDto> mypage(int page, int pageSize, String name) {
        //构造分页构造器
        Page<Setmeal> pageInfo = new Page(page,pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();

        //构造条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();

        //添加条件
        queryWrapper.like(StringUtils.isNotEmpty(name),Setmeal::getName,name);

        //添加排序条件
        queryWrapper.orderByAsc(Setmeal::getPrice);

        //执行查询
        //返回值和pageinfo为同一个对象
        this.page(pageInfo, queryWrapper);

        //拷贝除了泛型的其他信息
        BeanUtils.copyProperties(pageInfo,setmealDtoPage,"records");

        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> list = records.stream().map((i) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(i, setmealDto);
            Category categoryServiceById = categoryService.getById(i.getCategoryId());
            setmealDto.setCategoryName(categoryServiceById.getName());
            return setmealDto;
        }).collect(Collectors.toList());
        setmealDtoPage.setRecords(list);
        return setmealDtoPage;

    }

    /**
     * 保存新套餐,需要保存套餐信息和套餐内dish的信息
     */
    @Override
    public void savewithDishes(SetmealDto setmealDto) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDto,setmeal);
        this.save(setmeal);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        for(SetmealDish i:setmealDishes){
            i.setSetmealId(setmeal.getId());
        }
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 修改界面返回原本的信息
     * @param ids
     * @return
     */
    @Override
    public SetmealDto updatewithdishes(Long ids) {
        Setmeal byId = this.getById(ids);
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(byId,setmealDto);
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,ids);
        List<SetmealDish> list = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(list);
        return setmealDto;
    }
    /**
     * 批量启用或禁用dish
     * @param ids
     * @param status
     */
    @Override
    public void statusupdatebatch(String ids, int status) {
        String[] split = ids.split(",");
        for(String i:split){
            LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Setmeal::getId,i);
            Setmeal one = this.getOne(queryWrapper);
            one.setStatus(status);
            this.update(one,queryWrapper);
        }
    }

    /**
     * 菜品批量删除
     * @param ids
     */
    @Override
    public void setmealdeletebatch(String ids) {
        String[] split = ids.split(",");
        this.removeByIds(Arrays.asList(split));
        for(String s:split){
            LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SetmealDish::getSetmealId,s);
            setmealDishService.remove(queryWrapper);
        }
    }

    /**
     * 修改套餐信息的提交
     * @param setmealDto
     */
    @Override
    public void updatecommit(SetmealDto setmealDto) {
        this.updateById(setmealDto);

        //先删除套餐内原有的dish再添加
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();//这个list是新的dishes
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());
        setmealDishService.remove(queryWrapper);//删除旧的dishes

        for(SetmealDish i : setmealDishes){
            i.setSetmealId(setmealDto.getId());
        }
        setmealDishService.saveBatch(setmealDishes);
    }
}
