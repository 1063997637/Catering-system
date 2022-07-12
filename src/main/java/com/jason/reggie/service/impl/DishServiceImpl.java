package com.jason.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.jason.reggie.common.R;
import com.jason.reggie.dto.DishDto;
import com.jason.reggie.entity.Dish;
import com.jason.reggie.entity.DishFlavor;
import com.jason.reggie.mapper.DishMapper;
import com.jason.reggie.service.DishFlavorService;
import com.jason.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 新增菜品同时保存口味数据
     * @param dishDto
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
//        删除原有缓存
//        String key =  "dish_"+dishDto.getCategoryId();
//        redisTemplate.delete(key);
        this.save(dishDto);

        Long id = dishDto.getId();

        //因为传过来的flavors没有所属菜品的标记,所以遍历list为每个赋值菜品id
        List<DishFlavor> flavors = dishDto.getFlavors();

        for(DishFlavor i:flavors){
            i.setDishId(id);
        }

        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public DishDto getByidsWithFlavor(Long ids) {
        Dish dish = this.getById(ids);

        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(DishFlavor::getDishId,dish.getId());

        List<DishFlavor> list = dishFlavorService.list(queryWrapper);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);
        dishDto.setFlavors(list);


        return dishDto;
    }

    @Override
    public void updateWithFlavor(DishDto dishDto) {
        this.updateById(dishDto);

        //口味关系可能存在删除，新增等操作，不能简单的update
        //方法:先删除再添加
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        List<DishFlavor> flavors = dishDto.getFlavors();

        for(DishFlavor i:flavors){
            i.setDishId(dishDto.getId());
        }

        dishFlavorService.saveBatch(flavors);
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
            LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Dish::getId,i);
            Dish one = this.getOne(queryWrapper);
            one.setStatus(status);
            this.update(one,queryWrapper);
        }
    }

    /**
     * 菜品批量删除
     * @param ids
     */
    @Override
    public void dishdeletebatch(String ids) {
        String[] split = ids.split(",");
        this.removeByIds(Arrays.asList(split));
    }

    /**
     * 菜品信息和口味信息列表,使用redis
     * @param categoryId
     * @return
     */
    @Override
    public List<DishDto> alllist(Long categoryId) {

        //构造key
        String key = "dish_"+categoryId;

        //查询redis
        List<DishDto> redisdish = (List<DishDto>) redisTemplate.opsForValue().get(key);

        if(redisdish != null){
            log.info("正在使用redis的数据");
            return redisdish;
        }

        //查询数据库
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getCategoryId,categoryId);
        List<Dish> list = this.list(queryWrapper);
        List<DishDto> collect = list.stream().map((i) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(i, dishDto);
            LambdaQueryWrapper<DishFlavor> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(DishFlavor::getDishId, i.getId());
            dishDto.setFlavors(dishFlavorService.list(queryWrapper1));
            return dishDto;
        }).collect(Collectors.toList());

        //缓存到redis中
        redisTemplate.opsForValue().set(key,collect,60, TimeUnit.MINUTES);

        return collect;
    }
}
