package com.jason.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jason.reggie.common.R;
import com.jason.reggie.dto.DishDto;
import com.jason.reggie.entity.Category;
import com.jason.reggie.entity.Dish;
import com.jason.reggie.entity.Setmeal;
import com.jason.reggie.service.CategoryService;
import com.jason.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        log.info("菜品分页查询ing");

        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();

        //添加过滤条件
        dishLambdaQueryWrapper.like(StringUtils.isNotEmpty(name), Dish::getName,name);

        //查看是否标记为删除(暂不实现)

        dishLambdaQueryWrapper.orderByAsc(Dish::getPrice);

        dishService.page(pageInfo,dishLambdaQueryWrapper);

        //拷贝对象
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");//因为两者里面的T不同，所以不能原样copy

        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map((i)->{
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(i,dishDto);

            Category categoryServiceById = categoryService.getById(i.getCategoryId());
            dishDto.setCategoryName(categoryServiceById.getName());

            return dishDto;
        }).collect(Collectors.toList());
        dishDtoPage.setRecords(list);


        return R.success(dishDtoPage);
    }

    /**
     * 新增菜品
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info("新增菜品ing");
        dishService.saveWithFlavor(dishDto);
        return R.success("新增成功!");
    }

    /**
     * 修改菜品信息
     * @return
     */
    @GetMapping("/{ids}")
    public R<DishDto> updateget(@PathVariable Long ids){
        log.info("修改菜品ing");
        DishDto dishDto = dishService.getByidsWithFlavor(ids);
        return R.success(dishDto);
    }

    /**
     * 修改菜品信息提交
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> updatecommit(@RequestBody DishDto dishDto){
        log.info("修改菜品提交ing");
        dishService.updateWithFlavor(dishDto);
        return R.success("修改成功!");
    }

    /**
     * 修改售卖状态为停止
     * @param ids
     * @return
     */
    @PostMapping("/status/0")
    public R<String> status0(String ids){
        log.info("修改售卖状态ing");
        dishService.statusupdatebatch(ids,0);
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
        dishService.statusupdatebatch(ids,1);
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
        dishService.dishdeletebatch(ids);
        return R.success("删除成功!");
    }

//    /**
//     * 请求菜品列表,套餐使用该接口
//     * @param categoryId
//     * @return
//     */
//    @GetMapping("/list")
//    public R<List<Dish>> list(String categoryId){
//        log.info("获取dish列表ing");
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(Dish::getCategoryId,categoryId);
//        //只查询状态值为1的
//        queryWrapper.eq(Dish::getStatus,1);
//        List<Dish> list = dishService.list(queryWrapper);
//        return R.success(list);
//    }
    /**
     * 请求菜品列表,套餐使用该接口
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Long categoryId){
        log.info("获取dish列表ing");
        List<DishDto> alllist = dishService.alllist(categoryId);
        return R.success(alllist);
    }
}
