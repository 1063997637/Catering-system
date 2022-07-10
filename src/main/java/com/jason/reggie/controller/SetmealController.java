package com.jason.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jason.reggie.common.R;
import com.jason.reggie.dto.SetmealDto;
import com.jason.reggie.entity.Dish;
import com.jason.reggie.entity.Setmeal;
import com.jason.reggie.entity.SetmealDish;
import com.jason.reggie.service.SetmealDishService;
import com.jason.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        Page<SetmealDto> mypage = setmealService.mypage(page, pageSize, name);
        return R.success(mypage);
    }

    /**
     * 添加套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("保存套餐ing...");
        setmealService.savewithDishes(setmealDto);
        return R.success("添加成功!");
    }

    /**
     * 修改时获取套餐信息
     * @param ids
     * @return
     */
    @GetMapping("/{ids}")
    public R<SetmealDto> update(@PathVariable Long ids){
        log.info("获取套餐修改信息ing");
        SetmealDto updatewithdishes = setmealService.updatewithdishes(ids);
        return R.success(updatewithdishes);
    }
    /**
     * 修改售卖状态为启售
     * @param ids
     * @return
     */
    @PostMapping("/status/0")
    public R<String> status0(String ids){
        log.info("修改售卖状态ing");
        setmealService.statusupdatebatch(ids,0);
        return R.success("修改成功!");
    }

    /**
     * 修改售卖状态为启售
     * @param ids
     * @return
     */
    @PostMapping("/status/1")
    public R<String> status1(String ids){
        log.info("修改售卖状态ing");
        setmealService.statusupdatebatch(ids,1);
        return R.success("修改成功!");
    }

    /**
     * 删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(String ids){
        log.info("删除dish ing");
        setmealService.setmealdeletebatch(ids);
        return R.success("删除成功!");
    }

    /**
     * 修改套餐提交
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> updatecommit(@RequestBody SetmealDto setmealDto){
        log.info("修改提交ing...");
        setmealService.updatecommit(setmealDto);
        return R.success("修改成功!");
    }

    /**
     * 请求菜品列表,套餐使用该接口
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(String categoryId){
        log.info("获取套餐列表ing");
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Setmeal::getCategoryId,categoryId);
        //只查询状态值为1的
        queryWrapper.eq(Setmeal::getStatus,1);
        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }

    /**
     * 请求菜品列表,套餐使用该接口
     * @param ids
     * @return
     */
    @GetMapping("/dish/{ids}")
    public R<List<SetmealDish>> list(Long ids){
        log.info("获取dish列表ing");
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,ids);
        List<SetmealDish> list = setmealDishService.list(queryWrapper);
        return R.success(list);
    }
}
