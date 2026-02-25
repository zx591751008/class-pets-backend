package com.classpets.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.classpets.backend.entity.ActivationCode;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface ActivationCodeMapper extends BaseMapper<ActivationCode> {
    @Update("UPDATE activation_code SET used = 1, used_by = #{teacherId}, used_at = NOW() WHERE code = #{code} AND used = 0")
    int markUsedIfUnused(@Param("code") String code, @Param("teacherId") Long teacherId);
}
