package com.classpets.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.classpets.backend.entity.AbandonedPet;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AbandonedPetMapper extends BaseMapper<AbandonedPet> {
}
