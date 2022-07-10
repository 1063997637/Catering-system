package com.jason.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jason.reggie.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

}
