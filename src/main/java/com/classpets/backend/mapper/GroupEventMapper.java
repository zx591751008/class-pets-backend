package com.classpets.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.classpets.backend.entity.GroupEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface GroupEventMapper extends BaseMapper<GroupEvent> {

        @Select("SELECT group_id, SUM(change_value) as total_score FROM group_event " +
                        "WHERE class_id = #{classId} " +
                        "AND timestamp >= #{startTime} " +
                        "AND timestamp <= #{endTime} " +
                        "AND revoked = 0 " +
                        "GROUP BY group_id ORDER BY total_score DESC")
        List<Map<String, Object>> sumScoreByTimeRange(@Param("classId") Long classId,
                        @Param("startTime") Long startTime,
                        @Param("endTime") Long endTime);

        @Select("SELECT gi.id as groupId, gi.name as name, COALESCE(SUM(ge.change_value), 0) as total " +
                        "FROM group_info gi " +
                        "LEFT JOIN group_event ge ON ge.group_id = gi.id " +
                        "AND ge.timestamp >= #{startTime} " +
                        "AND ge.timestamp <= #{endTime} " +
                        "AND ge.revoked = 0 " +
                        "WHERE gi.class_id = #{classId} " +
                        "GROUP BY gi.id, gi.name " +
                        "ORDER BY total DESC")
        List<Map<String, Object>> sumScoreByGroupBetween(@Param("classId") Long classId,
                        @Param("startTime") Long startTime,
                        @Param("endTime") Long endTime);
}
