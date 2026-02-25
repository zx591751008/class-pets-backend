package com.classpets.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.classpets.backend.entity.StudentEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

@Mapper
public interface StudentEventMapper extends BaseMapper<StudentEvent> {

        @Select("SELECT student_id as studentId, SUM(change_value) as total " +
                        "FROM student_event " +
                        "WHERE class_id = #{classId} " +
                        "AND timestamp >= #{startTime} " +
                        "AND timestamp <= #{endTime} " +
                        "AND revoked = 0 " +
                        "GROUP BY student_id " +
                        "ORDER BY total DESC")
        List<Map<String, Object>> sumScoreByTimeRange(@Param("classId") Long classId,
                        @Param("startTime") Long startTime, @Param("endTime") Long endTime);

        @Select("SELECT se.student_id as studentId, SUM(se.change_value) as total " +
                        "FROM student_event se " +
                        "INNER JOIN rule_info ri ON se.reason = ri.content AND se.class_id = ri.class_id " +
                        "WHERE se.class_id = #{classId} " +
                        "AND ri.category = #{category} " +
                        "AND se.revoked = 0 " +
                        "GROUP BY se.student_id " +
                        "ORDER BY total DESC")
        List<Map<String, Object>> sumScoreByCategory(@Param("classId") Long classId,
                        @Param("category") String category);

        @Select("SELECT se.student_id as studentId, SUM(se.change_value) as total " +
                        "FROM student_event se " +
                        "INNER JOIN rule_info ri ON se.reason = ri.content AND se.class_id = ri.class_id " +
                        "WHERE se.class_id = #{classId} " +
                        "AND ri.category = #{category} " +
                        "AND se.timestamp >= #{startTime} " +
                        "AND se.timestamp <= #{endTime} " +
                        "AND se.revoked = 0 " +
                        "GROUP BY se.student_id " +
                        "ORDER BY total DESC")
        List<Map<String, Object>> sumScoreByCategoryAndTimeRange(@Param("classId") Long classId,
                        @Param("category") String category,
                        @Param("startTime") Long startTime,
                        @Param("endTime") Long endTime);

        @Select("SELECT ri.category as name, SUM(se.change_value) as total " +
                        "FROM student_event se " +
                        "INNER JOIN rule_info ri ON se.reason = ri.content AND se.class_id = ri.class_id " +
                        "WHERE se.class_id = #{classId} " +
                        "AND se.revoked = 0 " +
                        "AND ri.category IS NOT NULL " +
                        "AND ri.category <> '' " +
                        "AND ri.category <> '通用类型' " +
                        "GROUP BY ri.category " +
                        "ORDER BY total DESC")
        List<Map<String, Object>> sumScoreByCategoryGroup(@Param("classId") Long classId);

        @Select("SELECT student_id as studentId, SUM(change_value) as total " +
                        "FROM student_event " +
                        "WHERE class_id = #{classId} " +
                        "AND timestamp >= #{startTime} " +
                        "AND timestamp <= #{endTime} " +
                        "AND revoked = 0 " +
                        "GROUP BY student_id")
        List<Map<String, Object>> sumScoreByStudentBetween(@Param("classId") Long classId,
                        @Param("startTime") Long startTime,
                        @Param("endTime") Long endTime);

        @Select("SELECT student_id as studentId, COUNT(1) as count " +
                        "FROM student_event " +
                        "WHERE class_id = #{classId} " +
                        "AND timestamp >= #{startTime} " +
                        "AND timestamp <= #{endTime} " +
                        "AND revoked = 0 " +
                        "GROUP BY student_id")
        List<Map<String, Object>> countEventsByStudentBetween(@Param("classId") Long classId,
                        @Param("startTime") Long startTime,
                        @Param("endTime") Long endTime);

        @Select("SELECT COALESCE(SUM(CASE WHEN change_value > 0 THEN change_value ELSE 0 END), 0) as positive, " +
                        "COALESCE(SUM(CASE WHEN change_value < 0 THEN -change_value ELSE 0 END), 0) as negative " +
                        "FROM student_event " +
                        "WHERE class_id = #{classId} AND revoked = 0")
        Map<String, Object> sumPositiveAndNegative(@Param("classId") Long classId);

        @Select("SELECT COALESCE(SUM(CASE WHEN change_value > 0 THEN change_value ELSE 0 END), 0) as positive, " +
                        "COALESCE(SUM(CASE WHEN change_value < 0 THEN -change_value ELSE 0 END), 0) as negative " +
                        "FROM student_event " +
                        "WHERE class_id = #{classId} " +
                        "AND timestamp >= #{startTime} " +
                        "AND timestamp <= #{endTime} " +
                        "AND revoked = 0")
        Map<String, Object> sumPositiveAndNegativeBetween(@Param("classId") Long classId,
                        @Param("startTime") Long startTime,
                        @Param("endTime") Long endTime);

        @Select("SELECT DATE_FORMAT(FROM_UNIXTIME(timestamp/1000), '%Y-%m-%d') as date, SUM(change_value) as total " +
                        "FROM student_event " +
                        "WHERE class_id = #{classId} " +
                        "AND timestamp >= #{startTime} " +
                        "AND timestamp <= #{endTime} " +
                        "AND revoked = 0 " +
                        "GROUP BY date " +
                        "ORDER BY date ASC")
        List<Map<String, Object>> sumScoreByDate(@Param("classId") Long classId, @Param("startTime") Long startTime,
                        @Param("endTime") Long endTime);

        @Select("SELECT reason as name, SUM(change_value) as total " +
                        "FROM student_event " +
                        "WHERE class_id = #{classId} " +
                        "AND revoked = 0 " +
                        "GROUP BY reason " +
                        "ORDER BY total DESC " +
                        "LIMIT 5")
        List<Map<String, Object>> sumScoreGroupByReason(@Param("classId") Long classId);
}
