package com.wang.back.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.back.entity.Todo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TodoMapper extends BaseMapper<Todo> {
}
