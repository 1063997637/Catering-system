package com.jason.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jason.reggie.common.CustomException;
import com.jason.reggie.common.R;
import com.jason.reggie.entity.Category;
import com.jason.reggie.entity.Dish;
import com.jason.reggie.entity.Setmeal;
import com.jason.reggie.service.CategoryService;
import com.jason.reggie.service.DishService;
import com.jason.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize){
        log.info("page={},pageSize={},name={}",page,pageSize);
        //构造分页构造器
        Page pageInfo = new Page(page,pageSize);

        //构造条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();

        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort);

        //执行查询
        //返回值和pageinfo为同一个对象
        categoryService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 新增菜品套餐公用
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category){
        log.info("保存新栏目ing");
        boolean save = categoryService.save(category);
        return R.success("新增成功!");

    }

    /**
     * 删除某类菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long ids){
        //创建dish的query
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,ids);
        int count1 = dishService.count(dishLambdaQueryWrapper);

        //判断是否有关联,若有，抛出一个异常
        if(count1>0){
            //抛出业务异常
            throw new CustomException("当前分类下关联了菜品，无法删除");
        }

        //创建setmeal的query
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();

        //添加查询条件
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,ids);

        //判断
        int count2 = setmealService.count(setmealLambdaQueryWrapper);
        if(count2>0){
            //抛出异常
            throw new CustomException("当前分类下关联了套餐，无法删除");
        }

        //正常删除
        categoryService.removeById(ids);
        return R.success("删除成功!");

    }

    /**
     * 修改菜品信息
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category){
        log.info("修改菜品信息ing");
        categoryService.updateById(category);
        return R.success("修改成功");
    }

    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(category.getType()!=null,Category::getType,category.getType());
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }
}
