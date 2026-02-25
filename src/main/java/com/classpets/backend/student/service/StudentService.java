package com.classpets.backend.student.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classpets.backend.classinfo.service.ClassInfoService;
import com.classpets.backend.common.BizException;
import com.classpets.backend.entity.RedemptionRecord;
import com.classpets.backend.entity.Student;
import com.classpets.backend.entity.StudentEvent;
import com.classpets.backend.growth.service.GrowthConfigService;
import com.classpets.backend.mapper.RedemptionRecordMapper;
import com.classpets.backend.mapper.StudentEventMapper;
import com.classpets.backend.mapper.StudentMapper;
import com.classpets.backend.sync.SyncEventService;
import com.classpets.backend.student.dto.StudentScoreRequest;
import com.classpets.backend.student.dto.StudentUpsertRequest;
import com.classpets.backend.student.dto.BatchScoreRequest;
import com.classpets.backend.student.vo.BatchScoreResultVO;
import com.classpets.backend.student.vo.StudentVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {

    private static final Logger log = LoggerFactory.getLogger(StudentService.class);

    private final StudentMapper studentMapper;
    private final ClassInfoService classInfoService;
    private final StudentEventMapper studentEventMapper;
    private final RedemptionRecordMapper redemptionRecordMapper;
    private final GrowthConfigService growthConfigService;
    private final SyncEventService syncEventService;

    public StudentService(StudentMapper studentMapper,
            ClassInfoService classInfoService,
            StudentEventMapper studentEventMapper,
            RedemptionRecordMapper redemptionRecordMapper,
            GrowthConfigService growthConfigService,
            SyncEventService syncEventService) {
        this.studentMapper = studentMapper;
        this.classInfoService = classInfoService;
        this.studentEventMapper = studentEventMapper;
        this.redemptionRecordMapper = redemptionRecordMapper;
        this.growthConfigService = growthConfigService;
        this.syncEventService = syncEventService;
    }

    public List<StudentVO> listByClass(Long classId) {
        ensureClassOwnership(classId);
        List<Student> students = studentMapper.selectList(new LambdaQueryWrapper<Student>()
                .eq(Student::getClassId, classId)
                .orderByDesc(Student::getUpdateTime));
        return students.stream().map(this::toVO).collect(Collectors.toList());
    }

    public StudentVO create(Long classId, StudentUpsertRequest req) {
        ensureClassOwnership(classId);
        validateRequest(req);

        if (hasDuplicateNo(classId, req.getNo(), null)) {
            throw new BizException(40901, "学号已存在");
        }

        Student student = new Student();
        student.setClassId(classId);
        apply(req, student);
        student.setUpdateTime(System.currentTimeMillis());
        studentMapper.insert(student);
        syncEventService.publishClassChange(classId, "student_created", student.getId());
        return toVO(student);
    }

    public StudentVO update(Long studentId, StudentUpsertRequest req) {
        Student student = mustGet(studentId);
        ensureClassOwnership(student.getClassId());
        validateRequest(req);

        if (hasDuplicateNo(student.getClassId(), req.getNo(), studentId)) {
            throw new BizException(40901, "学号已存在");
        }

        apply(req, student);
        student.setUpdateTime(System.currentTimeMillis());
        studentMapper.updateById(student);
        syncEventService.publishClassChange(student.getClassId(), "student_updated", student.getId());
        return toVO(student);
    }

    @Transactional
    public void delete(Long studentId) {
        Student student = mustGet(studentId);
        ensureClassOwnership(student.getClassId());

        studentEventMapper.delete(new LambdaQueryWrapper<StudentEvent>()
                .eq(StudentEvent::getClassId, student.getClassId())
                .eq(StudentEvent::getStudentId, studentId));

        redemptionRecordMapper.delete(new LambdaQueryWrapper<RedemptionRecord>()
                .eq(RedemptionRecord::getClassId, student.getClassId())
                .eq(RedemptionRecord::getStudentId, studentId));

        studentMapper.deleteById(studentId);
        syncEventService.publishClassChange(student.getClassId(), "student_deleted", studentId);
    }

    public StudentVO score(Long studentId, StudentScoreRequest request) {
        Student student = mustGet(studentId);
        ensureClassOwnership(student.getClassId());

        String mode = trim(request.getMode()).toLowerCase();
        if (!"add".equals(mode) && !"deduct".equals(mode)) {
            throw new BizException(40001, "mode 必须为 add 或 deduct");
        }

        int points = request.getPoints() == null ? 0 : request.getPoints();
        if (points < 0) {
            throw new BizException(40001, "分值必须大于等于 0");
        }

        int delta = "deduct".equals(mode) ? -points : points;
        int total = safe(student.getTotalPoints()) + delta;
        int redeem = Math.max(0, safe(student.getRedeemPoints()) + delta);
        int exp = Math.max(0, safe(student.getExp()));
        double expGainRatio = resolveExpGainRatio(student.getClassId());
        exp = Math.max(0, exp + calcExpDeltaFromChange(delta, expGainRatio));
        LevelSnapshot snapshot = snapshotByExp(student.getClassId(), exp);

        student.setTotalPoints(total);
        student.setRedeemPoints(redeem);
        student.setExp(exp);
        student.setLevel(snapshot.level);
        student.setUpdateTime(System.currentTimeMillis());
        studentMapper.updateById(student);

        // Log event
        StudentEvent event = new StudentEvent();
        event.setClassId(student.getClassId());
        event.setStudentId(studentId);
        event.setChangeValue(delta);
        event.setRedeemChange(delta); // Assuming redeem points change same as total for now
        event.setReason(request.getReason()); // Need to add reason to request DTO if not present, or use default
        event.setTimestamp(System.currentTimeMillis());
        studentEventMapper.insert(event);

        syncEventService.publishClassChange(student.getClassId(), "student_score_changed", studentId);

        return toVO(student);
    }

    @Transactional
    public BatchScoreResultVO batchScore(Long classId, BatchScoreRequest request) {
        ensureClassOwnership(classId);
        if (request.getStudentIds() == null || request.getStudentIds().isEmpty()) {
            throw new BizException(40001, "studentIds 不能为空");
        }

        String mode = trim(request.getMode()).toLowerCase();
        if (!"add".equals(mode) && !"deduct".equals(mode)) {
            throw new BizException(40001, "mode 必须为 add 或 deduct");
        }

        int points = request.getPoints() == null ? 0 : request.getPoints();
        if (points <= 0) {
            throw new BizException(40001, "points 必须大于 0");
        }

        Set<Long> ids = new LinkedHashSet<>(request.getStudentIds());
        if (ids.contains(null)) {
            throw new BizException(40001, "studentId 不能为空");
        }

        List<Long> idList = new ArrayList<>(ids);
        List<Student> students = studentMapper.selectBatchIds(idList);
        if (students.size() != idList.size()) {
            Set<Long> foundIds = students.stream().map(Student::getId).collect(Collectors.toCollection(HashSet::new));
            List<Long> missingIds = idList.stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toList());
            throw new BizException(40001, "存在无效学生ID: " + missingIds);
        }

        List<Long> foreignIds = students.stream()
                .filter(s -> !classId.equals(s.getClassId()))
                .map(Student::getId)
                .collect(Collectors.toList());
        if (!foreignIds.isEmpty()) {
            throw new BizException(40001, "存在不属于当前班级的学生ID: " + foreignIds);
        }

        String finalReason = trim(request.getReason()).isEmpty() ? "批量操作" : request.getReason();
        log.info("batch score start classId={}, count={}, mode={}, points={}", classId, ids.size(), mode, points);
        StudentScoreRequest scoreRequest = new StudentScoreRequest();
        scoreRequest.setMode(mode);
        scoreRequest.setPoints(points);
        scoreRequest.setReason(finalReason);

        for (Long studentId : idList) {
            score(studentId, scoreRequest);
        }

        BatchScoreResultVO result = new BatchScoreResultVO();
        result.setTotal(idList.size());
        result.setSuccessIds(idList);
        result.setSuccessCount(idList.size());
        result.setFailedCount(0);
        log.info("batch score success classId={}, count={}, mode={}, points={}", classId, idList.size(), mode, points);
        syncEventService.publishClassChange(classId, "student_batch_score_changed", null);
        return result;
    }

    public void batchUpdateGroup(Long classId, com.classpets.backend.student.dto.BatchGroupRequest request) {
        ensureClassOwnership(classId);
        if (request.getStudentIds() == null || request.getStudentIds().isEmpty()) {
            return;
        }

        Student update = new Student();
        update.setGroupId(request.getGroupId());
        update.setUpdateTime(System.currentTimeMillis());

        studentMapper.update(update, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Student>()
                .eq(Student::getClassId, classId)
                .in(Student::getId, request.getStudentIds()));
        syncEventService.publishClassChange(classId, "student_batch_group_changed", null);
    }

    @Transactional
    public int clearByClass(Long classId) {
        ensureClassOwnership(classId);

        Long count = studentMapper.selectCount(new LambdaQueryWrapper<Student>()
                .eq(Student::getClassId, classId));
        int deletedCount = count == null ? 0 : count.intValue();

        studentEventMapper.delete(new LambdaQueryWrapper<StudentEvent>()
                .eq(StudentEvent::getClassId, classId));

        redemptionRecordMapper.delete(new LambdaQueryWrapper<RedemptionRecord>()
                .eq(RedemptionRecord::getClassId, classId));

        studentMapper.delete(new LambdaQueryWrapper<Student>()
                .eq(Student::getClassId, classId));

        syncEventService.publishClassChange(classId, "students_cleared", classId);
        return deletedCount;
    }

    /**
     * Ensure the class belongs to the current logged-in teacher
     */
    private void ensureClassOwnership(Long classId) {
        if (!classInfoService.isOwnedByCurrentTeacher(classId)) {
            throw new BizException(40301, "无权访问此班级");
        }
    }

    private Student mustGet(Long id) {
        Student student = studentMapper.selectById(id);
        if (student == null) {
            throw new BizException(40401, "学生不存在");
        }
        return student;
    }

    private boolean hasDuplicateNo(Long classId, String no, Long selfId) {
        String value = trim(no);
        if (value.isEmpty())
            return false;
        List<Student> list = studentMapper.selectList(new LambdaQueryWrapper<Student>()
                .eq(Student::getClassId, classId)
                .eq(Student::getStudentNo, value));
        return list.stream().anyMatch(s -> selfId == null || !selfId.equals(s.getId()));
    }

    private void validateRequest(StudentUpsertRequest req) {
        String name = trim(req.getName());
        String gender = trim(req.getGender());
        if (name.isEmpty())
            throw new BizException(40001, "姓名不能为空");
        if (!("男".equals(gender) || "女".equals(gender))) {
            throw new BizException(40001, "性别必须为 男/女");
        }
        if (req.getLevel() == null || req.getLevel() < 1)
            throw new BizException(40001, "等级必须大于等于 1");
        if (req.getRedeem() == null || req.getRedeem() < 0)
            throw new BizException(40001, "可用积分不能小于 0");
        if (req.getTotal() == null)
            throw new BizException(40001, "总积分不能为空");
    }

    private void apply(StudentUpsertRequest req, Student student) {
        student.setName(trim(req.getName()));
        student.setStudentNo(trim(req.getNo()));
        student.setGender(trim(req.getGender()));
        student.setTitle(trim(req.getGroup()));
        student.setAvatarImage(trim(req.getAvatarImage()));
        student.setTotalPoints(req.getTotal());
        student.setRedeemPoints(Math.max(0, req.getRedeem()));
        double expGainRatio = resolveExpGainRatio(student.getClassId());
        int exp = calcExpGainFromPoints(Math.max(0, safe(req.getTotal())), expGainRatio);
        LevelSnapshot snapshot = snapshotByExp(student.getClassId(), exp);
        student.setExp(exp);
        student.setLevel(snapshot.level);
        student.setGroupId(req.getGroupId()); // Assign to group
        student.setPetId(trim(req.getPetId()));

    }

    private double resolveExpGainRatio(Long classId) {
        GrowthConfigService.ResolvedGrowthConfig config = growthConfigService.resolveForClass(classId);
        double ratio = config.getExpGainRatio() == null ? 1.0 : config.getExpGainRatio();
        return ratio <= 0 ? 1.0 : ratio;
    }

    private int calcExpGainFromPoints(int points, double ratio) {
        if (points <= 0) {
            return 0;
        }
        int gain = (int) Math.round(points * ratio);
        return Math.max(1, gain);
    }

    private int calcExpDeltaFromChange(int changeValue, double ratio) {
        if (changeValue == 0) {
            return 0;
        }
        int abs = Math.abs(changeValue);
        int gain = (int) Math.round(abs * ratio);
        gain = Math.max(1, gain);
        return changeValue > 0 ? gain : -gain;
    }

    private StudentVO toVO(Student student) {
        StudentVO vo = new StudentVO();
        vo.setId(student.getId());
        vo.setName(student.getName());
        vo.setNo(empty(student.getStudentNo()));
        vo.setGroup(empty(student.getTitle(), "未分组"));
        vo.setGender(empty(student.getGender()));
        vo.setLevel(safe(student.getLevel()));
        int exp = Math.max(0, safe(student.getExp()));
        LevelSnapshot snapshot = snapshotByExp(student.getClassId(), exp);
        vo.setExp(exp);
        vo.setLevel(snapshot.level);
        vo.setLevelStartExp(snapshot.currentBase);
        vo.setNextLevelExp(snapshot.nextBase);
        vo.setExpToNext(Math.max(0, snapshot.nextBase - exp));
        vo.setTotal(safe(student.getTotalPoints()));
        vo.setRedeem(Math.max(0, safe(student.getRedeemPoints())));
        vo.setUpdated(toUpdatedText(student.getUpdateTime()));
        vo.setUpdateTime(student.getUpdateTime());
        vo.setGroupId(student.getGroupId());
        vo.setAvatarImage(student.getAvatarImage());
        vo.setPetId(student.getPetId());
        return vo;
    }

    private String toUpdatedText(Long updateTime) {
        if (updateTime == null || updateTime <= 0)
            return "今天";
        long now = System.currentTimeMillis();
        long diff = now - updateTime;
        long oneDay = 24L * 60L * 60L * 1000L;
        if (diff < oneDay)
            return "今天";
        if (diff < 2 * oneDay)
            return "昨天";
        return (diff / oneDay) + "天前";
    }

    private int safe(Integer value) {
        return value == null ? 0 : value;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String empty(String value) {
        return empty(value, "");
    }

    private String empty(String value, String fallback) {
        String v = trim(value);
        return v.isEmpty() ? fallback : v;
    }

    @Transactional
    public void recalculateGrowthByClass(Long classId) {
        ensureClassOwnership(classId);
        List<Student> students = studentMapper.selectList(new LambdaQueryWrapper<Student>()
                .eq(Student::getClassId, classId));
        List<StudentEvent> events = studentEventMapper.selectList(new LambdaQueryWrapper<StudentEvent>()
                .eq(StudentEvent::getClassId, classId)
                .eq(StudentEvent::getRevoked, 0)
                .orderByAsc(StudentEvent::getTimestamp)
                .orderByAsc(StudentEvent::getId));
        Map<Long, List<StudentEvent>> eventsByStudent = events.stream()
                .filter(e -> e.getStudentId() != null)
                .collect(Collectors.groupingBy(StudentEvent::getStudentId));
        long now = System.currentTimeMillis();
        for (Student student : students) {
            double expGainRatio = resolveExpGainRatio(student.getClassId());
            List<StudentEvent> studentEvents = eventsByStudent.get(student.getId());
            int exp = (studentEvents == null || studentEvents.isEmpty())
                    ? calcExpFromTotalForFallback(student, expGainRatio)
                    : recalculateExpFromEvents(studentEvents, expGainRatio);
            LevelSnapshot snapshot = snapshotByExp(classId, exp);
            student.setExp(exp);
            student.setLevel(snapshot.level);
            student.setUpdateTime(now);
            studentMapper.updateById(student);
        }
    }

    private int recalculateExpFromEvents(List<StudentEvent> events, double ratio) {
        if (events == null || events.isEmpty()) {
            return 0;
        }
        List<StudentEvent> sorted = events.stream()
                .sorted(Comparator.comparing(StudentEvent::getTimestamp, Comparator.nullsLast(Long::compareTo))
                        .thenComparing(StudentEvent::getId, Comparator.nullsLast(Long::compareTo)))
                .collect(Collectors.toList());

        int exp = 0;
        for (StudentEvent event : sorted) {
            int delta = event.getChangeValue() == null ? 0 : event.getChangeValue();
            if (delta == 0) {
                continue;
            }
            exp = Math.max(0, exp + calcExpDeltaFromChange(delta, ratio));
        }
        return exp;
    }

    private int calcExpFromTotalForFallback(Student student, double ratio) {
        return calcExpGainFromPoints(Math.max(0, safe(student.getTotalPoints())), ratio);
    }

    private LevelSnapshot snapshotByExp(Long classId, int exp) {
        int safeExp = Math.max(0, exp);
        GrowthConfigService.ResolvedGrowthConfig config = growthConfigService.resolveForClass(classId);
        java.util.List<Integer> levels = config.getLevelThresholds();

        int level = 1;
        for (int i = 0; i < levels.size(); i++) {
            if (safeExp >= levels.get(i)) {
                level = i + 1;
            } else {
                break;
            }
        }

        int currentBase;
        int nextBase;
        if (level < levels.size()) {
            currentBase = levels.get(level - 1);
            nextBase = levels.get(level);
        } else {
            currentBase = levels.get(levels.size() - 1);
            nextBase = currentBase;
            level = levels.size();
        }

        return new LevelSnapshot(Math.max(1, level), currentBase, nextBase);
    }

    private static class LevelSnapshot {
        private final int level;
        private final int currentBase;
        private final int nextBase;

        private LevelSnapshot(int level, int currentBase, int nextBase) {
            this.level = level;
            this.currentBase = currentBase;
            this.nextBase = nextBase;
        }
    }
}
