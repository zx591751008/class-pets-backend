package com.classpets.backend.data.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classpets.backend.classinfo.service.ClassInfoService;
import com.classpets.backend.common.BizException;
import com.classpets.backend.entity.GroupInfo;
import com.classpets.backend.entity.RedemptionRecord;
import com.classpets.backend.entity.RuleInfo;
import com.classpets.backend.entity.Student;
import com.classpets.backend.entity.StudentEvent;
import com.classpets.backend.mapper.GroupInfoMapper;
import com.classpets.backend.mapper.RedemptionRecordMapper;
import com.classpets.backend.mapper.RuleInfoMapper;
import com.classpets.backend.mapper.StudentEventMapper;
import com.classpets.backend.mapper.StudentMapper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataExportService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final ClassInfoService classInfoService;
    private final StudentMapper studentMapper;
    private final RuleInfoMapper ruleInfoMapper;
    private final StudentEventMapper studentEventMapper;
    private final GroupInfoMapper groupInfoMapper;
    private final RedemptionRecordMapper redemptionRecordMapper;

    public DataExportService(ClassInfoService classInfoService,
            StudentMapper studentMapper,
            RuleInfoMapper ruleInfoMapper,
            StudentEventMapper studentEventMapper,
            GroupInfoMapper groupInfoMapper,
            RedemptionRecordMapper redemptionRecordMapper) {
        this.classInfoService = classInfoService;
        this.studentMapper = studentMapper;
        this.ruleInfoMapper = ruleInfoMapper;
        this.studentEventMapper = studentEventMapper;
        this.groupInfoMapper = groupInfoMapper;
        this.redemptionRecordMapper = redemptionRecordMapper;
    }

    public byte[] exportStudentsCsv(Long classId) {
        ensureClassOwnership(classId);
        List<Student> students = studentMapper.selectList(new LambdaQueryWrapper<Student>()
                .eq(Student::getClassId, classId)
                .orderByAsc(Student::getId));

        StringBuilder csv = new StringBuilder();
        csv.append("id,姓名,学号,性别,小组ID,总积分,可用积分,经验值,等级,宠物路线,更新时间\n");
        for (Student s : students) {
            csv.append(safe(s.getId())).append(',')
                    .append(escape(s.getName())).append(',')
                    .append(escape(s.getStudentNo())).append(',')
                    .append(escape(s.getGender())).append(',')
                    .append(safe(s.getGroupId())).append(',')
                    .append(safe(s.getTotalPoints())).append(',')
                    .append(safe(s.getRedeemPoints())).append(',')
                    .append(safe(s.getExp())).append(',')
                    .append(safe(s.getLevel())).append(',')
                    .append(escape(s.getPetId())).append(',')
                    .append(escape(formatTs(s.getUpdateTime())))
                    .append('\n');
        }
        return withBom(csv.toString());
    }

    public byte[] exportRulesCsv(Long classId) {
        ensureClassOwnership(classId);
        List<RuleInfo> rules = ruleInfoMapper.selectList(new LambdaQueryWrapper<RuleInfo>()
                .eq(RuleInfo::getClassId, classId)
                .orderByAsc(RuleInfo::getTargetType)
                .orderByAsc(RuleInfo::getCategory)
                .orderByAsc(RuleInfo::getId));

        StringBuilder csv = new StringBuilder();
        csv.append("id,规则内容,分值,类型,目标类型,分类,是否启用,冷却小时,可叠加,创建时间\n");
        for (RuleInfo r : rules) {
            csv.append(safe(r.getId())).append(',')
                    .append(escape(r.getContent())).append(',')
                    .append(safe(r.getPoints())).append(',')
                    .append(escape(r.getType())).append(',')
                    .append(escape(targetTypeName(r.getTargetType()))).append(',')
                    .append(escape(r.getCategory())).append(',')
                    .append(safe(r.getEnabled())).append(',')
                    .append(r.getCooldownHours() == null ? "" : r.getCooldownHours()).append(',')
                    .append(safe(r.getStackable())).append(',')
                    .append(escape(r.getCreatedAt() == null ? "" : r.getCreatedAt().toString()))
                    .append('\n');
        }
        return withBom(csv.toString());
    }

    public byte[] exportStudentEventsCsv(Long classId, Long from, Long to) {
        ensureClassOwnership(classId);
        LambdaQueryWrapper<StudentEvent> wrapper = new LambdaQueryWrapper<StudentEvent>()
                .eq(StudentEvent::getClassId, classId)
                .orderByDesc(StudentEvent::getTimestamp)
                .orderByDesc(StudentEvent::getId);
        if (from != null) {
            wrapper.ge(StudentEvent::getTimestamp, from);
        }
        if (to != null) {
            wrapper.le(StudentEvent::getTimestamp, to);
        }
        List<StudentEvent> events = studentEventMapper.selectList(wrapper);

        List<Student> students = studentMapper.selectList(new LambdaQueryWrapper<Student>()
                .eq(Student::getClassId, classId));
        Map<Long, String> studentNameMap = new HashMap<>();
        for (Student s : students) {
            studentNameMap.put(s.getId(), s.getName());
        }

        StringBuilder csv = new StringBuilder();
        csv.append("id,时间,学生ID,学生姓名,原因,积分变化,余额变化,是否撤销\n");
        for (StudentEvent e : events) {
            csv.append(safe(e.getId())).append(',')
                    .append(escape(formatTs(e.getTimestamp()))).append(',')
                    .append(safe(e.getStudentId())).append(',')
                    .append(escape(studentNameMap.get(e.getStudentId()))).append(',')
                    .append(escape(e.getReason())).append(',')
                    .append(safe(e.getChangeValue())).append(',')
                    .append(safe(e.getRedeemChange())).append(',')
                    .append(e.getRevoked() != null && e.getRevoked() == 1 ? "是" : "否")
                    .append('\n');
        }
        return withBom(csv.toString());
    }

    private void ensureClassOwnership(Long classId) {
        if (!classInfoService.isOwnedByCurrentTeacher(classId)) {
            throw new BizException(40301, "无权访问此班级");
        }
    }

    public byte[] exportGroupsCsv(Long classId) {
        ensureClassOwnership(classId);
        List<GroupInfo> groups = groupInfoMapper.selectList(new LambdaQueryWrapper<GroupInfo>()
                .eq(GroupInfo::getClassId, classId)
                .orderByAsc(GroupInfo::getId));
        List<Student> students = studentMapper.selectList(new LambdaQueryWrapper<Student>()
                .eq(Student::getClassId, classId));

        // Build group-id -> member names map
        Map<Long, StringBuilder> memberMap = new HashMap<>();
        for (Student s : students) {
            if (s.getGroupId() != null) {
                memberMap.computeIfAbsent(s.getGroupId(), k -> new StringBuilder());
                if (memberMap.get(s.getGroupId()).length() > 0)
                    memberMap.get(s.getGroupId()).append("、");
                memberMap.get(s.getGroupId()).append(s.getName());
            }
        }

        StringBuilder csv = new StringBuilder();
        csv.append("小组ID,小组名称,图标,小组积分,成员列表\n");
        for (GroupInfo g : groups) {
            csv.append(safe(g.getId())).append(',')
                    .append(escape(g.getName())).append(',')
                    .append(escape(g.getIcon())).append(',')
                    .append(safe(g.getPoints())).append(',')
                    .append(escape(memberMap.containsKey(g.getId()) ? memberMap.get(g.getId()).toString() : ""))
                    .append('\n');
        }
        return withBom(csv.toString());
    }

    public byte[] exportLeaderboardCsv(Long classId) {
        ensureClassOwnership(classId);
        List<Student> students = studentMapper.selectList(new LambdaQueryWrapper<Student>()
                .eq(Student::getClassId, classId)
                .orderByDesc(Student::getTotalPoints));

        StringBuilder csv = new StringBuilder();
        csv.append("排名,姓名,学号,性别,总积分,等级\n");
        int rank = 1;
        for (Student s : students) {
            csv.append(rank++).append(',')
                    .append(escape(s.getName())).append(',')
                    .append(escape(s.getStudentNo())).append(',')
                    .append(escape(s.getGender())).append(',')
                    .append(safe(s.getTotalPoints())).append(',')
                    .append(safe(s.getLevel()))
                    .append('\n');
        }
        return withBom(csv.toString());
    }

    public byte[] exportRedemptionsCsv(Long classId) {
        ensureClassOwnership(classId);
        List<RedemptionRecord> records = redemptionRecordMapper.selectList(new LambdaQueryWrapper<RedemptionRecord>()
                .eq(RedemptionRecord::getClassId, classId)
                .orderByDesc(RedemptionRecord::getCreateTime));

        List<Student> students = studentMapper.selectList(new LambdaQueryWrapper<Student>()
                .eq(Student::getClassId, classId));
        Map<Long, String> studentNameMap = new HashMap<>();
        for (Student s : students) {
            studentNameMap.put(s.getId(), s.getName());
        }

        StringBuilder csv = new StringBuilder();
        csv.append("兑换时间,学生姓名,商品名称,消耗积分,状态\n");
        for (RedemptionRecord r : records) {
            csv.append(escape(formatTs(r.getCreateTime()))).append(',')
                    .append(escape(studentNameMap.getOrDefault(r.getStudentId(), "未知"))).append(',')
                    .append(escape(r.getItemName())).append(',')
                    .append(safe(r.getCost())).append(',')
                    .append(escape(statusName(r.getStatus())))
                    .append('\n');
        }
        return withBom(csv.toString());
    }

    private String statusName(String status) {
        if (status == null)
            return "未知";
        switch (status) {
            case "PENDING":
                return "待发放";
            case "COMPLETED":
                return "已完成";
            case "REFUNDED":
                return "已退款";
            default:
                return status;
        }
    }

    private String targetTypeName(Integer value) {
        if (value == null || value == 0) {
            return "学生";
        }
        return "小组";
    }

    private String formatTs(Long ts) {
        if (ts == null || ts <= 0) {
            return "";
        }
        return TIME_FMT.format(Instant.ofEpochMilli(ts));
    }

    private String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private byte[] withBom(String csv) {
        byte[] body = csv.getBytes(StandardCharsets.UTF_8);
        byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
        byte[] out = new byte[bom.length + body.length];
        System.arraycopy(bom, 0, out, 0, bom.length);
        System.arraycopy(body, 0, out, bom.length, body.length);
        return out;
    }
}
