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
                        "WHERE se.class_id = #{classId} " +
                        "AND se.revoked = 0 " +
                        "AND ((se.rule_id IS NOT NULL AND EXISTS (" +
                        "  SELECT 1 FROM rule_info ri " +
                        "  WHERE ri.id = se.rule_id AND ri.class_id = se.class_id " +
                        "    AND ri.target_type = 0 AND ri.category = #{category}" +
                        ")) OR (se.rule_id IS NULL AND EXISTS (" +
                        "  SELECT 1 FROM rule_info ri2 " +
                        "  WHERE ri2.class_id = se.class_id AND ri2.content = se.reason " +
                        "    AND ri2.target_type = 0 AND ri2.category = #{category}" +
                        "))) " +
                        "GROUP BY se.student_id " +
                        "ORDER BY total DESC")
        List<Map<String, Object>> sumScoreByCategory(@Param("classId") Long classId,
                        @Param("category") String category);

        @Select("SELECT se.student_id as studentId, SUM(se.change_value) as total " +
                        "FROM student_event se " +
                        "WHERE se.class_id = #{classId} " +
                        "AND se.timestamp >= #{startTime} " +
                        "AND se.timestamp <= #{endTime} " +
                        "AND se.revoked = 0 " +
                        "AND ((se.rule_id IS NOT NULL AND EXISTS (" +
                        "  SELECT 1 FROM rule_info ri " +
                        "  WHERE ri.id = se.rule_id AND ri.class_id = se.class_id " +
                        "    AND ri.target_type = 0 AND ri.category = #{category}" +
                        ")) OR (se.rule_id IS NULL AND EXISTS (" +
                        "  SELECT 1 FROM rule_info ri2 " +
                        "  WHERE ri2.class_id = se.class_id AND ri2.content = se.reason " +
                        "    AND ri2.target_type = 0 AND ri2.category = #{category}" +
                        "))) " +
                        "GROUP BY se.student_id " +
                        "ORDER BY total DESC")
        List<Map<String, Object>> sumScoreByCategoryAndTimeRange(@Param("classId") Long classId,
                        @Param("category") String category,
                        @Param("startTime") Long startTime,
                        @Param("endTime") Long endTime);

        @Select("SELECT x.category as name, SUM(x.change_value) as total " +
                        "FROM (" +
                        "  SELECT se.change_value, CASE " +
                        "    WHEN se.rule_id IS NOT NULL THEN (" +
                        "      SELECT ri.category FROM rule_info ri " +
                        "      WHERE ri.id = se.rule_id AND ri.class_id = se.class_id AND ri.target_type = 0 LIMIT 1" +
                        "    ) ELSE (" +
                        "      SELECT ri2.category FROM rule_info ri2 " +
                        "      WHERE ri2.class_id = se.class_id AND ri2.content = se.reason AND ri2.target_type = 0 " +
                        "      ORDER BY ri2.id ASC LIMIT 1" +
                        "    ) END AS category " +
                        "  FROM student_event se " +
                        "  WHERE se.class_id = #{classId} AND se.revoked = 0" +
                        ") x " +
                        "WHERE x.category IS NOT NULL " +
                        "AND x.category <> '' " +
                        "AND x.category <> '通用类型' " +
                        "GROUP BY x.category " +
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
