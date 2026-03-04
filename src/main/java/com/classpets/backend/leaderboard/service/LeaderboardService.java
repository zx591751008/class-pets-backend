package com.classpets.backend.leaderboard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classpets.backend.classinfo.service.ClassInfoService;
import com.classpets.backend.common.BizException;
import com.classpets.backend.entity.Student;
import com.classpets.backend.leaderboard.vo.RankVO;
import com.classpets.backend.mapper.StudentEventMapper;
import com.classpets.backend.mapper.StudentMapper;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {

    private static final Set<String> STUDENT_RANK_TYPES = Set.of("TOTAL", "WEEKLY", "MONTHLY");
    private static final Set<String> GROUP_RANK_TYPES = Set.of(
            "TOTAL", "WEEKLY", "MONTHLY", "MEMBER_TOTAL", "MEMBER_WEEKLY", "MEMBER_MONTHLY");

    private final StudentMapper studentMapper;
    private final StudentEventMapper studentEventMapper;
    private final ClassInfoService classInfoService;
    private final com.classpets.backend.mapper.GroupMapper groupMapper;
    private final com.classpets.backend.mapper.GroupEventMapper groupEventMapper;

    public LeaderboardService(StudentMapper studentMapper,
            StudentEventMapper studentEventMapper,
            ClassInfoService classInfoService,
            com.classpets.backend.mapper.GroupMapper groupMapper,
            com.classpets.backend.mapper.GroupEventMapper groupEventMapper) {
        this.studentMapper = studentMapper;
        this.studentEventMapper = studentEventMapper;
        this.classInfoService = classInfoService;
        this.groupMapper = groupMapper;
        this.groupEventMapper = groupEventMapper;
    }

    public List<RankVO> getRank(Long classId, String type, String category) {
        if (!classInfoService.isOwnedByCurrentTeacher(classId)) {
            throw new BizException(40301, "无权访问此班级");
        }

        List<RankVO> result = new ArrayList<>();
        boolean hasCategory = category != null && !category.isEmpty();
        String normalizedType = normalizeStudentRankType(type);

        // WEEKLY - students with scores this week (optionally filtered by category)
        if ("WEEKLY".equals(normalizedType)) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    .withHour(0).withMinute(0).withSecond(0).withNano(0);
            long startTime = start.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
            long endTime = System.currentTimeMillis();

            List<Map<String, Object>> stats = hasCategory
                    ? studentEventMapper.sumScoreByCategoryAndTimeRange(classId, category, startTime, endTime)
                    : studentEventMapper.sumScoreByTimeRange(classId, startTime, endTime);
            result = buildStudentRankList(classId, toStudentScoreMap(stats));
        }
        // MONTHLY - students with scores this month (optionally filtered by category)
        else if ("MONTHLY".equals(normalizedType)) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = now.with(TemporalAdjusters.firstDayOfMonth())
                    .withHour(0).withMinute(0).withSecond(0).withNano(0);
            long startTime = start.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
            long endTime = System.currentTimeMillis();

            List<Map<String, Object>> stats = hasCategory
                    ? studentEventMapper.sumScoreByCategoryAndTimeRange(classId, category, startTime, endTime)
                    : studentEventMapper.sumScoreByTimeRange(classId, startTime, endTime);
            result = buildStudentRankList(classId, toStudentScoreMap(stats));
        }
        // TOTAL + category
        else if (hasCategory) {
            List<Map<String, Object>> stats = studentEventMapper.sumScoreByCategory(classId, category);
            result = buildStudentRankList(classId, toStudentScoreMap(stats));
        }
        // Default: TOTAL - all students by total points
        else {
            List<Student> students = studentMapper.selectList(new LambdaQueryWrapper<Student>()
                    .eq(Student::getClassId, classId)
                    .orderByDesc(Student::getTotalPoints));

            // Get all groups for name lookup
            List<com.classpets.backend.entity.GroupInfo> groups = groupMapper
                    .selectList(new LambdaQueryWrapper<com.classpets.backend.entity.GroupInfo>()
                            .eq(com.classpets.backend.entity.GroupInfo::getClassId, classId));
            Map<Long, String> groupNameMap = groups.stream().collect(Collectors.toMap(
                    com.classpets.backend.entity.GroupInfo::getId, com.classpets.backend.entity.GroupInfo::getName));

            for (int i = 0; i < students.size(); i++) {
                Student s = students.get(i);
                RankVO vo = new RankVO();
                vo.setStudentId(s.getId());
                vo.setName(s.getName());
                vo.setAvatar(s.getPetId());
                vo.setAvatarImage(s.getAvatarImage());
                vo.setScore(s.getTotalPoints());
                vo.setRank(i + 1);
                vo.setLevel(s.getLevel());
                vo.setTitle(s.getTitle());
                if (s.getGroupId() != null) {
                    vo.setGroupName(groupNameMap.get(s.getGroupId()));
                }
                result.add(vo);
            }
        }

        return result;
    }

    private Map<Long, Integer> toStudentScoreMap(List<Map<String, Object>> stats) {
        Map<Long, Integer> scoreMap = new HashMap<>();
        if (stats == null || stats.isEmpty()) {
            return scoreMap;
        }
        for (Map<String, Object> stat : stats) {
            Number studentIdNum = (Number) stat.get("studentId");
            if (studentIdNum == null) {
                continue;
            }
            Long studentId = studentIdNum.longValue();
            Number totalObj = (Number) stat.get("total");
            int total = totalObj == null ? 0 : totalObj.intValue();
            scoreMap.put(studentId, total);
        }
        return scoreMap;
    }

    private List<RankVO> buildStudentRankList(Long classId, Map<Long, Integer> scoreMap) {
        List<RankVO> list = new ArrayList<>();

        List<Student> students = studentMapper.selectList(new LambdaQueryWrapper<Student>()
                .eq(Student::getClassId, classId));

        List<com.classpets.backend.entity.GroupInfo> groups = groupMapper
                .selectList(new LambdaQueryWrapper<com.classpets.backend.entity.GroupInfo>()
                        .eq(com.classpets.backend.entity.GroupInfo::getClassId, classId));
        Map<Long, String> groupNameMap = groups.stream().collect(Collectors.toMap(
                com.classpets.backend.entity.GroupInfo::getId, com.classpets.backend.entity.GroupInfo::getName));

        students.sort((a, b) -> {
            int scoreA = scoreMap.getOrDefault(a.getId(), 0);
            int scoreB = scoreMap.getOrDefault(b.getId(), 0);
            if (scoreA != scoreB) {
                return Integer.compare(scoreB, scoreA);
            }
            return Long.compare(a.getId(), b.getId());
        });

        for (int i = 0; i < students.size(); i++) {
            Student s = students.get(i);
            RankVO vo = new RankVO();
            vo.setStudentId(s.getId());
            vo.setName(s.getName());
            vo.setAvatar(s.getPetId());
            vo.setAvatarImage(s.getAvatarImage());
            vo.setScore(scoreMap.getOrDefault(s.getId(), 0));
            vo.setRank(i + 1);
            vo.setLevel(s.getLevel());
            vo.setTitle(s.getTitle());
            if (s.getGroupId() != null) {
                vo.setGroupName(groupNameMap.get(s.getGroupId()));
            }
            list.add(vo);
        }
        return list;
    }

    public List<RankVO> getGroupRank(Long classId, String type) {
        if (!classInfoService.isOwnedByCurrentTeacher(classId)) {
            throw new BizException(40301, "无权访问此班级");
        }

        List<RankVO> result = new ArrayList<>();
        String normalizedType = normalizeGroupRankType(type);
        List<com.classpets.backend.entity.GroupInfo> groups = groupMapper
                .selectList(new LambdaQueryWrapper<com.classpets.backend.entity.GroupInfo>()
                        .eq(com.classpets.backend.entity.GroupInfo::getClassId, classId));
        Map<Long, com.classpets.backend.entity.GroupInfo> groupMap = groups.stream()
                .collect(Collectors.toMap(com.classpets.backend.entity.GroupInfo::getId, g -> g));

        if (normalizedType.startsWith("MEMBER_")) {
            // Aggregate student points by group
            Map<Long, Integer> groupMemberScores = new HashMap<>();

            if ("MEMBER_TOTAL".equals(normalizedType)) {
                List<Student> students = studentMapper.selectList(new LambdaQueryWrapper<Student>()
                        .eq(Student::getClassId, classId)
                        .isNotNull(Student::getGroupId));
                for (Student s : students) {
                    groupMemberScores.merge(s.getGroupId(), s.getTotalPoints(), Integer::sum);
                }
            } else {
                // MEMBER_WEEKLY or MEMBER_MONTHLY
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime start;
                if ("MEMBER_WEEKLY".equals(normalizedType)) {
                    start = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                            .withHour(0).withMinute(0).withSecond(0).withNano(0);
                } else {
                    start = now.with(TemporalAdjusters.firstDayOfMonth())
                            .withHour(0).withMinute(0).withSecond(0).withNano(0);
                }
                long startTime = start.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
                long endTime = System.currentTimeMillis();

                List<Map<String, Object>> studentStats = studentEventMapper.sumScoreByTimeRange(classId, startTime,
                        endTime);
                // studentStats contains {studentId, total}
                List<Student> students = studentMapper.selectList(new LambdaQueryWrapper<Student>()
                        .eq(Student::getClassId, classId)
                        .isNotNull(Student::getGroupId));
                Map<Long, Long> studentToGroup = students.stream()
                        .collect(Collectors.toMap(Student::getId, Student::getGroupId));

                for (Map<String, Object> stat : studentStats) {
                    Long sId = ((Number) stat.get("studentId")).longValue();
                    Integer score = ((Number) stat.get("total")).intValue();
                    Long gId = studentToGroup.get(sId);
                    if (gId != null) {
                        groupMemberScores.merge(gId, score, Integer::sum);
                    }
                }
            }

            // Map and sort
            for (com.classpets.backend.entity.GroupInfo g : groups) {
                RankVO vo = new RankVO();
                vo.setStudentId(g.getId());
                vo.setName(g.getName());
                vo.setAvatar(g.getIcon());
                vo.setScore(groupMemberScores.getOrDefault(g.getId(), 0));
                result.add(vo);
            }
            result.sort((a, b) -> b.getScore() - a.getScore());
            for (int i = 0; i < result.size(); i++) {
                result.get(i).setRank(i + 1);
            }
        } else if ("WEEKLY".equals(normalizedType) || "MONTHLY".equals(normalizedType)) {
            // Existing independent group points logic...
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start;
            if ("WEEKLY".equals(normalizedType)) {
                start = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                        .withHour(0).withMinute(0).withSecond(0).withNano(0);
            } else {
                start = now.with(TemporalAdjusters.firstDayOfMonth())
                        .withHour(0).withMinute(0).withSecond(0).withNano(0);
            }
            long startTime = start.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
            long endTime = System.currentTimeMillis();

            List<Map<String, Object>> stats = groupEventMapper.sumScoreByTimeRange(classId, startTime, endTime);

            for (int i = 0; i < stats.size(); i++) {
                Map<String, Object> stat = stats.get(i);
                Long groupId = ((Number) stat.get("group_id")).longValue();
                Number scoreNum = (Number) stat.get("total_score");
                int score = scoreNum != null ? scoreNum.intValue() : 0;

                com.classpets.backend.entity.GroupInfo g = groupMap.get(groupId);
                if (g != null) {
                    RankVO vo = new RankVO();
                    vo.setStudentId(g.getId());
                    vo.setName(g.getName());
                    vo.setAvatar(g.getIcon());
                    vo.setScore(score);
                    vo.setRank(i + 1);
                    result.add(vo);
                }
            }
        } else {
            // TOTAL
            List<com.classpets.backend.entity.GroupInfo> sortedGroups = groupMapper
                    .selectList(new LambdaQueryWrapper<com.classpets.backend.entity.GroupInfo>()
                            .eq(com.classpets.backend.entity.GroupInfo::getClassId, classId)
                            .orderByDesc(com.classpets.backend.entity.GroupInfo::getPoints));

            for (int i = 0; i < sortedGroups.size(); i++) {
                com.classpets.backend.entity.GroupInfo g = sortedGroups.get(i);
                RankVO vo = new RankVO();
                vo.setStudentId(g.getId());
                vo.setName(g.getName());
                vo.setAvatar(g.getIcon());
                vo.setScore(g.getPoints());
                vo.setRank(i + 1);
                result.add(vo);
            }
        }
        return result;
    }

    private String normalizeStudentRankType(String type) {
        String normalized = type == null ? "TOTAL" : type.trim().toUpperCase(Locale.ROOT);
        return STUDENT_RANK_TYPES.contains(normalized) ? normalized : "TOTAL";
    }

    private String normalizeGroupRankType(String type) {
        String normalized = type == null ? "TOTAL" : type.trim().toUpperCase(Locale.ROOT);
        return GROUP_RANK_TYPES.contains(normalized) ? normalized : "TOTAL";
    }
}
