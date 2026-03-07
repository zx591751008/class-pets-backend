package com.classpets.backend.data.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classpets.backend.classinfo.service.ClassInfoService;
import com.classpets.backend.common.BizException;
import com.classpets.backend.entity.GroupInfo;
import com.classpets.backend.entity.RedemptionRecord;
import com.classpets.backend.entity.RuleInfo;
import com.classpets.backend.entity.Student;
import com.classpets.backend.entity.StudentEvent;
import com.classpets.backend.leaderboard.service.LeaderboardService;
import com.classpets.backend.leaderboard.vo.RankVO;
import com.classpets.backend.mapper.GroupInfoMapper;
import com.classpets.backend.mapper.RedemptionRecordMapper;
import com.classpets.backend.mapper.RuleInfoMapper;
import com.classpets.backend.mapper.StudentEventMapper;
import com.classpets.backend.mapper.StudentMapper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final LeaderboardService leaderboardService;

    public DataExportService(ClassInfoService classInfoService,
            StudentMapper studentMapper,
            RuleInfoMapper ruleInfoMapper,
            StudentEventMapper studentEventMapper,
            GroupInfoMapper groupInfoMapper,
            RedemptionRecordMapper redemptionRecordMapper,
            LeaderboardService leaderboardService) {
        this.classInfoService = classInfoService;
        this.studentMapper = studentMapper;
        this.ruleInfoMapper = ruleInfoMapper;
        this.studentEventMapper = studentEventMapper;
        this.groupInfoMapper = groupInfoMapper;
        this.redemptionRecordMapper = redemptionRecordMapper;
        this.leaderboardService = leaderboardService;
    }

    public byte[] exportStudentsCsv(Long classId) {
        ensureClassOwnership(classId);
        List<Student> students = studentMapper.selectList(new LambdaQueryWrapper<Student>()
                .eq(Student::getClassId, classId)
                .orderByAsc(Student::getId));
        Map<Long, String> groupNameMap = groupInfoMapper.selectList(new LambdaQueryWrapper<GroupInfo>()
                .eq(GroupInfo::getClassId, classId))
                .stream()
                .collect(HashMap::new, (m, g) -> m.put(g.getId(), g.getName()), HashMap::putAll);

        StringBuilder csv = new StringBuilder();
        csv.append("姓名,学号,性别,分组,总积分,可用积分\n");
        for (Student s : students) {
            csv.append(escape(s.getName())).append(',')
                    .append(escape(s.getStudentNo())).append(',')
                    .append(escape(s.getGender())).append(',')
                    .append(escape(s.getGroupId() == null ? "" : groupNameMap.getOrDefault(s.getGroupId(), ""))).append(',')
                    .append(safe(s.getTotalPoints())).append(',')
                    .append(safe(s.getRedeemPoints()))
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
        csv.append("规则内容,分值,类型,目标类型,分类,是否启用,冷却小时,可叠加,创建时间\n");
        for (RuleInfo r : rules) {
            csv.append(escape(r.getContent())).append(',')
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
        csv.append("时间,学生姓名,原因,积分变化,是否撤销\n");
        for (StudentEvent e : events) {
            csv.append(escape(formatTs(e.getTimestamp()))).append(',')
                    .append(escape(studentNameMap.get(e.getStudentId()))).append(',')
                    .append(escape(e.getReason())).append(',')
                    .append(safe(e.getChangeValue())).append(',')
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
        csv.append("小组名称,小组积分,成员列表\n");
        for (GroupInfo g : groups) {
            csv.append(escape(g.getName())).append(',')
                    .append(safe(g.getPoints())).append(',')
                    .append(escape(memberMap.containsKey(g.getId()) ? memberMap.get(g.getId()).toString() : ""))
                    .append('\n');
        }
        return withBom(csv.toString());
    }

    public byte[] exportLeaderboardCsv(Long classId, String type, String category) {
        ensureClassOwnership(classId);
        List<RankVO> ranks = leaderboardService.getRank(classId, type, category);
        Map<Long, Student> studentMap = studentMapper.selectList(new LambdaQueryWrapper<Student>()
                .eq(Student::getClassId, classId))
                .stream()
                .collect(HashMap::new, (m, s) -> m.put(s.getId(), s), HashMap::putAll);

        StringBuilder csv = new StringBuilder();
        csv.append("排名,姓名,学号,性别,分值,小组\n");
        for (RankVO rank : ranks) {
            Student student = rank.getStudentId() == null ? null : studentMap.get(rank.getStudentId());
            csv.append(safe(rank.getRank())).append(',')
                    .append(escape(rank.getName())).append(',')
                    .append(escape(student == null ? "" : student.getStudentNo())).append(',')
                    .append(escape(student == null ? "" : student.getGender())).append(',')
                    .append(safe(rank.getScore())).append(',')
                    .append(escape(rank.getGroupName()))
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

    public byte[] exportLeaderboardXlsx(Long classId, String type, String category) {
        ensureClassOwnership(classId);
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Map<Long, Student> studentMap = studentMapper.selectList(new LambdaQueryWrapper<Student>()
                    .eq(Student::getClassId, classId))
                    .stream()
                    .collect(HashMap::new, (m, s) -> m.put(s.getId(), s), HashMap::putAll);

            List<RankVO> mainRanks = leaderboardService.getRank(classId, type, category);
            String typeLabel = rankTypeLabel(type);
            appendLeaderboardSheet(workbook, safeSheetName("排行榜_" + typeLabel), mainRanks, studentMap);

            String normalizedCategory = category == null ? "" : category.trim();
            if (normalizedCategory.isEmpty()) {
                for (String categoryName : collectLeaderboardCategories(classId)) {
                    List<RankVO> categoryRanks = leaderboardService.getRank(classId, type, categoryName);
                    appendLeaderboardSheet(workbook, safeSheetName(typeLabel + "_" + categoryName), categoryRanks, studentMap);
                }
            }

            workbook.write(output);
            return output.toByteArray();
        } catch (Exception ex) {
            throw new BizException(50001, "导出排行榜失败");
        }
    }

    public byte[] exportBundleXlsx(Long classId, List<String> types, Long from, Long to) {
        ensureClassOwnership(classId);
        Set<String> normalizedTypes = normalizeBundleTypes(types);
        if (normalizedTypes.isEmpty()) {
            throw new BizException(40001, "请选择导出项");
        }
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (normalizedTypes.contains("students")) {
                Sheet sheet = workbook.createSheet("学生花名册");
                writeHeader(sheet, "姓名", "学号", "性别", "分组", "总积分", "可用积分");
                List<Student> students = studentMapper.selectList(new LambdaQueryWrapper<Student>()
                        .eq(Student::getClassId, classId)
                        .orderByAsc(Student::getId));
                Map<Long, String> groupNameMap = groupInfoMapper.selectList(new LambdaQueryWrapper<GroupInfo>()
                        .eq(GroupInfo::getClassId, classId))
                        .stream()
                        .collect(HashMap::new, (m, g) -> m.put(g.getId(), g.getName()), HashMap::putAll);
                int rowIndex = 1;
                for (Student s : students) {
                    writeRow(sheet, rowIndex++,
                            safe(s.getName()), safe(s.getStudentNo()), safe(s.getGender()),
                            safe(s.getGroupId() == null ? "" : groupNameMap.getOrDefault(s.getGroupId(), "")),
                            safe(s.getTotalPoints()), safe(s.getRedeemPoints()));
                }
                if (rowIndex == 1) {
                    writeRow(sheet, rowIndex, "当前筛选条件下无数据");
                }
                autosizeColumns(sheet, 6);
            }

            if (normalizedTypes.contains("rules")) {
                Sheet sheet = workbook.createSheet("计分规则");
                writeHeader(sheet, "规则内容", "分值", "类型", "目标类型", "分类", "是否启用", "冷却小时", "可叠加", "创建时间");
                List<RuleInfo> rules = ruleInfoMapper.selectList(new LambdaQueryWrapper<RuleInfo>()
                        .eq(RuleInfo::getClassId, classId)
                        .orderByAsc(RuleInfo::getTargetType)
                        .orderByAsc(RuleInfo::getCategory)
                        .orderByAsc(RuleInfo::getId));
                int rowIndex = 1;
                for (RuleInfo r : rules) {
                    writeRow(sheet, rowIndex++,
                            safe(r.getContent()), safe(r.getPoints()), safe(r.getType()),
                            targetTypeName(r.getTargetType()), safe(r.getCategory()), safe(r.getEnabled()),
                            r.getCooldownHours() == null ? "" : r.getCooldownHours().toString(),
                            safe(r.getStackable()), r.getCreatedAt() == null ? "" : r.getCreatedAt().toString());
                }
                if (rowIndex == 1) {
                    writeRow(sheet, rowIndex, "当前筛选条件下无数据");
                }
                autosizeColumns(sheet, 9);
            }

            if (normalizedTypes.contains("events")) {
                Sheet sheet = workbook.createSheet("积分流水");
                writeHeader(sheet, "时间", "学生姓名", "原因", "积分变化", "是否撤销");
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
                Map<Long, String> studentNameMap = new HashMap<>();
                for (Student s : studentMapper.selectList(new LambdaQueryWrapper<Student>().eq(Student::getClassId, classId))) {
                    studentNameMap.put(s.getId(), s.getName());
                }
                int rowIndex = 1;
                for (StudentEvent e : events) {
                    writeRow(sheet, rowIndex++,
                            safe(formatTs(e.getTimestamp())), safe(studentNameMap.get(e.getStudentId())),
                            safe(e.getReason()), safe(e.getChangeValue()),
                            e.getRevoked() != null && e.getRevoked() == 1 ? "是" : "否");
                }
                if (rowIndex == 1) {
                    writeRow(sheet, rowIndex, "当前筛选条件下无数据");
                }
                autosizeColumns(sheet, 5);
            }

            if (normalizedTypes.contains("groups")) {
                Sheet sheet = workbook.createSheet("小组数据");
                writeHeader(sheet, "小组名称", "小组积分", "成员列表");
                List<GroupInfo> groups = groupInfoMapper.selectList(new LambdaQueryWrapper<GroupInfo>()
                        .eq(GroupInfo::getClassId, classId)
                        .orderByAsc(GroupInfo::getId));
                List<Student> students = studentMapper.selectList(new LambdaQueryWrapper<Student>()
                        .eq(Student::getClassId, classId));
                Map<Long, StringBuilder> memberMap = new HashMap<>();
                for (Student s : students) {
                    if (s.getGroupId() == null) {
                        continue;
                    }
                    memberMap.computeIfAbsent(s.getGroupId(), key -> new StringBuilder());
                    if (memberMap.get(s.getGroupId()).length() > 0) {
                        memberMap.get(s.getGroupId()).append("、");
                    }
                    memberMap.get(s.getGroupId()).append(s.getName());
                }
                int rowIndex = 1;
                for (GroupInfo g : groups) {
                    writeRow(sheet, rowIndex++, safe(g.getName()), safe(g.getPoints()),
                            memberMap.containsKey(g.getId()) ? memberMap.get(g.getId()).toString() : "");
                }
                if (rowIndex == 1) {
                    writeRow(sheet, rowIndex, "当前筛选条件下无数据");
                }
                autosizeColumns(sheet, 3);
            }

            if (normalizedTypes.contains("redemptions")) {
                Sheet sheet = workbook.createSheet("兑换记录");
                writeHeader(sheet, "兑换时间", "学生姓名", "商品名称", "消耗积分", "状态");
                List<RedemptionRecord> records = redemptionRecordMapper.selectList(new LambdaQueryWrapper<RedemptionRecord>()
                        .eq(RedemptionRecord::getClassId, classId)
                        .orderByDesc(RedemptionRecord::getCreateTime));
                Map<Long, String> studentNameMap = new HashMap<>();
                for (Student s : studentMapper.selectList(new LambdaQueryWrapper<Student>().eq(Student::getClassId, classId))) {
                    studentNameMap.put(s.getId(), s.getName());
                }
                int rowIndex = 1;
                for (RedemptionRecord r : records) {
                    writeRow(sheet, rowIndex++, safe(formatTs(r.getCreateTime())),
                            safe(studentNameMap.getOrDefault(r.getStudentId(), "未知")),
                            safe(r.getItemName()), safe(r.getCost()), statusName(r.getStatus()));
                }
                if (rowIndex == 1) {
                    writeRow(sheet, rowIndex, "当前筛选条件下无数据");
                }
                autosizeColumns(sheet, 5);
            }

            workbook.write(output);
            return output.toByteArray();
        } catch (Exception ex) {
            throw new BizException(50001, "导出数据失败");
        }
    }

    private Set<String> normalizeBundleTypes(List<String> types) {
        Set<String> normalized = new LinkedHashSet<>();
        if (types == null) {
            return normalized;
        }
        Set<String> allowed = new LinkedHashSet<>(Arrays.asList("students", "rules", "events", "groups", "redemptions"));
        for (String type : types) {
            if (type == null) {
                continue;
            }
            String value = type.trim().toLowerCase(Locale.ROOT);
            if (allowed.contains(value)) {
                normalized.add(value);
            }
        }
        return normalized;
    }

    private void writeHeader(Sheet sheet, String... headers) {
        writeRow(sheet, 0, headers);
    }

    private void writeRow(Sheet sheet, int rowIndex, String... cells) {
        Row row = sheet.createRow(rowIndex);
        for (int i = 0; i < cells.length; i++) {
            row.createCell(i).setCellValue(cells[i] == null ? "" : cells[i]);
        }
    }

    private void autosizeColumns(Sheet sheet, int count) {
        for (int i = 0; i < count; i++) {
            sheet.autoSizeColumn(i);
            int width = sheet.getColumnWidth(i);
            int maxWidth = 12000;
            if (width > maxWidth) {
                sheet.setColumnWidth(i, maxWidth);
            }
        }
    }

    private void appendLeaderboardSheet(Workbook workbook, String sheetName, List<RankVO> ranks, Map<Long, Student> studentMap) {
        Sheet sheet = workbook.createSheet(sheetName);
        writeHeader(sheet, "排名", "姓名", "学号", "性别", "分值", "小组");
        int rowIndex = 1;
        for (RankVO rank : ranks) {
            Student student = rank.getStudentId() == null ? null : studentMap.get(rank.getStudentId());
            writeRow(sheet, rowIndex++,
                    safe(rank.getRank()),
                    safe(rank.getName()),
                    safe(student == null ? "" : student.getStudentNo()),
                    safe(student == null ? "" : student.getGender()),
                    safe(rank.getScore()),
                    safe(rank.getGroupName()));
        }
        if (rowIndex == 1) {
            writeRow(sheet, rowIndex, "当前筛选条件下无数据");
        }
        autosizeColumns(sheet, 6);
    }

    private List<String> collectLeaderboardCategories(Long classId) {
        return ruleInfoMapper.selectList(new LambdaQueryWrapper<RuleInfo>()
                .eq(RuleInfo::getClassId, classId)
                .eq(RuleInfo::getTargetType, 0)
                .eq(RuleInfo::getEnabled, 1)
                .orderByAsc(RuleInfo::getCategory))
                .stream()
                .map(RuleInfo::getCategory)
                .filter(value -> value != null && !value.trim().isEmpty())
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }

    private String rankTypeLabel(String type) {
        String normalized = type == null ? "TOTAL" : type.trim().toUpperCase(Locale.ROOT);
        switch (normalized) {
            case "WEEKLY":
                return "周榜";
            case "MONTHLY":
                return "月榜";
            default:
                return "总榜";
        }
    }

    private String safeSheetName(String value) {
        String name = value == null ? "Sheet" : value.trim();
        if (name.isEmpty()) {
            name = "Sheet";
        }
        name = name.replaceAll("[\\\\/?*\\[\\]:]", "_");
        if (name.length() > 31) {
            return name.substring(0, 31);
        }
        return name;
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
        String escaped = sanitizeCsvFormula(value).replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private String sanitizeCsvFormula(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        int index = 0;
        while (index < value.length() && Character.isWhitespace(value.charAt(index))) {
            index++;
        }
        if (index >= value.length()) {
            return value;
        }
        char first = value.charAt(index);
        if (first == '=' || first == '+' || first == '-' || first == '@') {
            return "'" + value;
        }
        return value;
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
