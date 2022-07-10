package com.jason.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.jason.reggie.common.BaseContext;
import com.jason.reggie.common.R;
import com.jason.reggie.entity.AddressBook;
import com.jason.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 当前用户添加收货地址
     * @param addressBook
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody AddressBook addressBook){
        log.info("添加收货地址ing");
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookService.save(addressBook);
        return R.success("添加成功");
    }

    /**
     * 个人地址中当前用户获取列表
     * @return
     */
    @GetMapping("/list")
    public R<List<AddressBook>> list(){
        log.info("获取地址列表ing");
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId,BaseContext.getCurrentId());
        List<AddressBook> list = addressBookService.list(queryWrapper);
        return R.success(list);
    }

    /**
     * 修改地址信息时回调原有信息
     * @return
     */
    @GetMapping("/{ids}")
    public R<AddressBook> updatecallback(@PathVariable Long ids){
        log.info("回调地址信息ing");
        AddressBook byId = addressBookService.getById(ids);
        return R.success(byId);
    }

    /**
     * 地址修改信息的提交
     * @param addressBook
     * @return
     */
    @PutMapping
    public R<String> updatecommit(@RequestBody AddressBook addressBook){
        log.info("地址修改ing");
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookService.updateById(addressBook);
        return R.success("地址修改成功!");
    }

    /**
     * 删除收货地址
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long ids){
        log.info("删除地址ing");
        addressBookService.removeById(ids);
        return R.success("删除成功");
    }

    /**
     * 设置为默认地址
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    public R<String> setdefault(@RequestBody AddressBook addressBook){
        log.info("设置为默认地址ing");
        Long id = addressBook.getId();

        //将其他地址修改为非默认地址
        LambdaUpdateWrapper<AddressBook> updateWrapper0 = new LambdaUpdateWrapper<>();
        updateWrapper0.eq(AddressBook::getIsDefault,1);
        updateWrapper0.set(AddressBook::getIsDefault,0);
        addressBookService.update(updateWrapper0);
        log.info("修改为非默认完成");

        //修改为默认地址
        LambdaUpdateWrapper<AddressBook> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AddressBook::getId,id);
        updateWrapper.set(AddressBook::getIsDefault,1);
        addressBookService.update(updateWrapper);

        return R.success("设置成功");
    }

    /**
     * 查询默认地址
     * @return
     */
    @GetMapping("/default")
    public R<AddressBook> getDeafault(){
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getIsDefault,1);
        AddressBook one = addressBookService.getOne(queryWrapper);
        if(one == null)return R.error("没有找到该对象");
        else return R.success(one);
    }
}
