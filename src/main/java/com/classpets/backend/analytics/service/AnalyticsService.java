package com.classpets.backend.analytics.service;

import com.classpets.backend.classinfo.service.ClassInfoService;
import com.classpets.backend.common.BizException;
import com.classpets.backend.entity.Student;
import com.classpets.backend.mapper.GroupEventMapper;
import com.classpets.backend.mapper.StudentEventMapper;
import com.classpets.backend.mapper.StudentMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final StudentEventMapper studentEventMapper;
    private final GroupEventMapper groupEventMapper;
    private final StudentMapper studentMapper;
    private final ClassInfoService classInfoService;

    public AnalyticsService(StudentEventMapper studentEventMapper,
            GroupEventMapper groupEventMapper,
            StudentMapper studentMapper,
            ClassInfoService classInfoService) {
        this.studentEventMapper = studentEventMapper;
        this.groupEventMapper = groupEventMapper;
        this.studentMapper = studentMapper;
        this.classInfoService = classInfoService;
    }

    public Map<String, Object> getClassTrend(Long classId) {
        if (!classInfoService.isOwnedByCurrentTeacher(classId)) {
            throw new BizException(40301, "无权访问此班级");
        }

        // Last 30 days
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(30);
        long startTime = start.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
        long endTime = end.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();

        List<Map<String, Object>> trends = studentEventMapper.sumScoreByDate(classId, startTime, endTime);
        List<Map<String, Object>> distribution = studentEventMapper.sumScoreGroupByReason(classId);
        List<Map<String, Object>> categoryRadar = studentEventMapper.sumScoreByCategoryGroup(classId);
        Map<String, Object> scoreBalance = studentEventMapper.sumPositiveAndNegativeBetween(classId, startTime, endTime);
        List<Map<String, Object>> groupPerformance = groupEventMapper.sumScoreByGroupBetween(classId, startTime, endTime);

        // All students in class (used for current total and top10)
        List<Student> allStudents = studentMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Student>()
                .eq(Student::getClassId, classId)
                .orderByDesc(Student::getTotalPoints));

        // Top students by total points (bar chart)
        List<Student> topStudentsRaw = allStudents.stream().limit(10).collect(Collectors.toList());
        List<Map<String, Object>> topStudents = topStudentsRaw.stream().map(s -> {
            Map<String, Object> row = new HashMap<>();
            row.put("id", s.getId());
            row.put("name", s.getName());
            row.put("total", safeInt(s.getTotalPoints()));
            row.put("level", safeInt(s.getLevel()));
            return row;
        }).collect(Collectors.toList());

        // Alerts
        Map<String, Object> alerts = buildAlerts(classId);

        // Summary KPIs
        int totalStudents = allStudents.size();
        int totalScore = allStudents.stream().mapToInt(s -> safeInt(s.getTotalPoints())).sum();
        int positive = mapInt(scoreBalance, "positive");
        int negative = mapInt(scoreBalance, "negative");

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalStudents", totalStudents);
        summary.put("totalScore", totalScore);
        summary.put("positive", positive);
        summary.put("negative", negative);
        summary.put("net", positive - negative);

        Map<String, Object> result = new HashMap<>();
        result.put("summary", summary);
        result.put("trend", trends);
        result.put("distribution", distribution);
        result.put("radar", categoryRadar);
        result.put("scoreBalance", scoreBalance);
        result.put("groupPerformance", groupPerformance);
        result.put("topStudents", topStudents);
        result.put("alerts", alerts);

        return result;
    }

    private Map<String, Object> buildAlerts(Long classId) {
        Map<String, Object> alerts = new HashMap<>();
        long now = System.currentTimeMillis();
        long sevenDays = 7L * 24 * 60 * 60 * 1000;

        // Low activity: low number of score events in last 7 days
        Map<Long, Integer> activityCountMap = toStudentCountMap(
                studentEventMapper.countEventsByStudentBetween(classId, now - sevenDays, now));
        List<Student> lowActivityStudents = studentMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Student>()
                .eq(Student::getClassId, classId));
        List<Map<String, Object>> lowActivityList = lowActivityStudents.stream()
                .map(s -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", s.getId());
                    row.put("name", s.getName());
                    row.put("count", activityCountMap.getOrDefault(s.getId(), 0));
                    return row;
                })
                .filter(r -> ((Integer) r.get("count")) <= 1)
                .sorted(Comparator.comparingInt(r -> (Integer) r.get("count")))
                .limit(5)
                .collect(Collectors.toList());

        // Declining students: current 7d score < previous 7d score
        long currentStart = now - sevenDays;
        long prevStart = now - (sevenDays * 2);
        long prevEnd = currentStart - 1;

        Map<Long, Integer> currentMap = toStudentScoreMap(studentEventMapper.sumScoreByStudentBetween(classId, currentStart, now));
        Map<Long, Integer> prevMap = toStudentScoreMap(studentEventMapper.sumScoreByStudentBetween(classId, prevStart, prevEnd));

        List<Student> students = studentMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Student>()
                .eq(Student::getClassId, classId));

        List<Map<String, Object>> declining = new ArrayList<>();
        for (Student s : students) {
            int cur = currentMap.getOrDefault(s.getId(), 0);
            int prev = prevMap.getOrDefault(s.getId(), 0);
            int delta = cur - prev;
            if (delta < 0) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", s.getId());
                row.put("name", s.getName());
                row.put("delta", delta);
                row.put("current", cur);
                row.put("previous", prev);
                declining.add(row);
            }
        }
        declining.sort(Comparator.comparingInt(o -> (Integer) o.get("delta")));
        if (declining.size() > 5) {
            declining = declining.subList(0, 5);
        }
        if (declining.isEmpty()) {
            List<Map<String, Object>> fallback = new ArrayList<>();
            for (Student s : students) {
                int cur = currentMap.getOrDefault(s.getId(), 0);
                int prev = prevMap.getOrDefault(s.getId(), 0);
                Map<String, Object> row = new HashMap<>();
                row.put("id", s.getId());
                row.put("name", s.getName());
                row.put("delta", cur - prev);
                row.put("current", cur);
                row.put("previous", prev);
                fallback.add(row);
            }
            fallback.sort(Comparator.comparingInt(o -> (Integer) o.get("current")));
            declining = fallback.stream().limit(5).collect(Collectors.toList());
        }

        alerts.put("lowActivity", lowActivityList);
        alerts.put("declining", declining);
        return alerts;
    }

    private Map<Long, Integer> toStudentScoreMap(List<Map<String, Object>> rows) {
        Map<Long, Integer> map = new HashMap<>();
        if (rows == null) {
            return map;
        }
        for (Map<String, Object> row : rows) {
            Object sidObj = row.get("studentId");
            if (!(sidObj instanceof Number)) {
                continue;
            }
            long sid = ((Number) sidObj).longValue();
            int total = mapInt(row, "total");
            map.put(sid, total);
        }
        return map;
    }

    private Map<Long, Integer> toStudentCountMap(List<Map<String, Object>> rows) {
        Map<Long, Integer> map = new HashMap<>();
        if (rows == null) {
            return map;
        }
        for (Map<String, Object> row : rows) {
            Object sidObj = row.get("studentId");
            if (!(sidObj instanceof Number)) {
                continue;
            }
            long sid = ((Number) sidObj).longValue();
            int count = mapInt(row, "count");
            map.put(sid, count);
        }
        return map;
    }

    private int mapInt(Map<String, Object> row, String key) {
        if (row == null) {
            return 0;
        }
        Object val = row.get(key);
        return val instanceof Number ? ((Number) val).intValue() : 0;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
