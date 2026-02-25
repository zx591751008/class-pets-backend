package com.classpets.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.classpets.backend.entity.GroupInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GroupMapper extends BaseMapper<GroupInfo> {
}
