package com.classpets.backend.student.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classpets.backend.classinfo.service.ClassInfoService;
import com.classpets.backend.common.BizException;
import com.classpets.backend.entity.RedemptionRecord;
import com.classpets.backend.entity.Student;
import com.classpets.backend.entity.StudentEvent;
import com.classpets.backend.growth.service.GrowthConfigService;
import com.classpets.backend.mapper.RedemptionRecordMapper;
import com.classpets.backend.mapper.RuleInfoMapper;
import com.classpets.backend.mapper.StudentEventMapper;
import com.classpets.backend.mapper.StudentMapper;
import com.classpets.backend.mapper.StudentPetGalleryMapper;
import com.classpets.backend.sync.SyncEventService;
import com.classpets.backend.entity.StudentPetGallery;
import com.classpets.backend.entity.RuleInfo;
import com.classpets.backend.student.dto.StudentScoreRequest;
import com.classpets.backend.student.dto.StudentSpendRedeemRequest;
import com.classpets.backend.student.dto.StudentUpsertRequest;
import com.classpets.backend.student.dto.BatchScoreRequest;
import com.classpets.backend.student.dto.BatchAdoptRequest;
import com.classpets.backend.student.vo.BatchScoreResultVO;
import com.classpets.backend.student.vo.BatchAdoptResultVO;
import com.classpets.backend.student.vo.StudentVO;
import com.classpets.backend.student.vo.StudentPetGalleryVO;
import com.classpets.backend.student.dto.FeedPetRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {

    private static final Logger log = LoggerFactory.getLogger(StudentService.class);
    private static final int MAX_STUDENTS_PER_CLASS = 80;

    private final StudentMapper studentMapper;
    private final ClassInfoService classInfoService;
    private final StudentEventMapper studentEventMapper;
    private final RedemptionRecordMapper redemptionRecordMapper;
    private final GrowthConfigService growthConfigService;
    private final SyncEventService syncEventService;
    private final StudentPetGalleryMapper studentPetGalleryMapper;
    private final AbandonedPetService abandonPetService;
    private final RuleInfoMapper ruleInfoMapper;

    public StudentService(StudentMapper studentMapper,
            ClassInfoService classInfoService,
            StudentEventMapper studentEventMapper,
            RedemptionRecordMapper redemptionRecordMapper,
            GrowthConfigService growthConfigService,
            SyncEventService syncEventService,
            StudentPetGalleryMapper studentPetGalleryMapper,
            AbandonedPetService abandonPetService,
            RuleInfoMapper ruleInfoMapper) {
        this.studentMapper = studentMapper;
        this.classInfoService = classInfoService;
        this.studentEventMapper = studentEventMapper;
        this.redemptionRecordMapper = redemptionRecordMapper;
        this.growthConfigService = growthConfigService;
        this.syncEventService = syncEventService;
        this.studentPetGalleryMapper = studentPetGalleryMapper;
        this.abandonPetService = abandonPetService;
        this.ruleInfoMapper = ruleInfoMapper;
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

        Long studentCount = studentMapper.selectCount(new LambdaQueryWrapper<Student>()
                .eq(Student::getClassId, classId));
        if (studentCount != null && studentCount >= MAX_STUDENTS_PER_CLASS) {
            throw new BizException(40001, "班级人数已达上限（最多80人）");
        }

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

        studentPetGalleryMapper.delete(new LambdaQueryWrapper<StudentPetGallery>()
                .eq(StudentPetGallery::getStudentId, studentId));

        abandonPetService.deleteByStudentId(studentId);

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

        // 没有选择宠物的学生不能加减分
        String petId = trim(student.getPetId());
        if (petId.isEmpty()) {
            throw new BizException(40002, "该学生还没有领养宠物，请先选择宠物后再进行加减分操作");
        }

        int delta = "deduct".equals(mode) ? -points : points;
        Long validRuleId = resolveValidRuleId(request.getRuleId(), student.getClassId(), mode);
        int beforeLevel = safe(student.getLevel());
        String beforePetId = petId;

        int beforeTotal = safe(student.getTotalPoints());
        int beforeRedeem = Math.max(0, safe(student.getRedeemPoints()));
        int total;
        int redeem;
        int redeemDelta;

        if (delta >= 0) {
            total = beforeTotal + delta;
            int repay = beforeTotal < 0 ? Math.min(delta, -beforeTotal) : 0;
            int redeemInc = delta - repay;
            redeem = beforeRedeem + redeemInc;
            redeemDelta = redeemInc;
        } else {
            int deduct = -delta;
            total = beforeTotal - deduct;
            int redeemDeduct = Math.min(beforeRedeem, deduct);
            redeem = beforeRedeem - redeemDeduct;
            redeemDelta = -redeemDeduct;
        }

        int beforeExp = Math.max(0, safe(student.getExp()));
        int exp = beforeExp;
        double expGainRatio = resolveExpGainRatio(student.getClassId());
        int expChangeValueForGrowth = resolveExpChangeValueForGrowth(beforeTotal, delta);
        int expectedExpDelta = calcExpDeltaFromChange(expChangeValueForGrowth, expGainRatio);
        exp = Math.max(0, exp + expectedExpDelta);
        int actualExpDelta = exp - beforeExp;
        LevelSnapshot snapshot = snapshotByExp(student.getClassId(), exp);

        student.setTotalPoints(total);
        student.setRedeemPoints(redeem);
        student.setExp(exp);
        student.setLevel(snapshot.level);
        student.setTitle(generateTitle(snapshot.level, student.getClassId()));

        UnlockResult unlockResult = handleAutoUnlockAndReset(student, beforeLevel, beforePetId);

        student.setUpdateTime(System.currentTimeMillis());
        studentMapper.updateById(student);

        // Log event
        StudentEvent event = new StudentEvent();
        event.setClassId(student.getClassId());
        event.setStudentId(studentId);
        event.setChangeValue(delta);
        event.setRedeemChange(redeemDelta);
        event.setExpChange(actualExpDelta);
        event.setNote("expExact=1");
        event.setReason(request.getReason()); // Need to add reason to request DTO if not present, or use default
        event.setRuleId(validRuleId);
        event.setTimestamp(System.currentTimeMillis());
        studentEventMapper.insert(event);

        syncEventService.publishClassChange(student.getClassId(), "student_score_changed", studentId);

        StudentVO vo = toVO(student);
        vo.setGalleryUnlocked(unlockResult.unlocked);
        vo.setUnlockedPetId(unlockResult.unlockedPetId);
        return vo;
    }

    @Transactional
    public StudentVO spendRedeem(Long studentId, StudentSpendRedeemRequest request) {
        Student student = mustGet(studentId);
        ensureClassOwnership(student.getClassId());

        int points = request.getPoints() == null ? 0 : request.getPoints();
        if (points <= 0) {
            throw new BizException(40001, "扣除积分必须大于 0");
        }

        int redeem = safe(student.getRedeemPoints());
        if (redeem < points) {
            throw new BizException(40002, "余额不足，需要 " + points + " 积分");
        }

        student.setRedeemPoints(redeem - points);
        student.setUpdateTime(System.currentTimeMillis());
        studentMapper.updateById(student);

        StudentEvent event = new StudentEvent();
        event.setClassId(student.getClassId());
        event.setStudentId(studentId);
        event.setChangeValue(0);
        event.setRedeemChange(-points);
        event.setExpChange(0);
        String reason = trim(request.getReason());
        event.setReason(reason.isEmpty() ? "课堂抽奖消耗" : reason);
        event.setTimestamp(System.currentTimeMillis());
        studentEventMapper.insert(event);

        syncEventService.publishClassChange(student.getClassId(), "student_redeem_spent", studentId);
        return toVO(student);
    }

    @Transactional
    public Map<String, Object> feedPet(Long studentId, FeedPetRequest request) {
        Student student = mustGet(studentId);
        ensureClassOwnership(student.getClassId());

        int beforeLevel = safe(student.getLevel());
        String beforePetId = trim(student.getPetId());
        UnlockResult unlockResult = UnlockResult.none();

        String itemType = trim(request.getItemType()).toUpperCase();
        int cost = 0;
        int expChange = 0;
        String itemName = "";
        boolean isAmnesia = false;

        if ("CANDY".equals(itemType)) {
            cost = 20;
            expChange = 25;
            itemName = "魔法糖果";
        } else if ("MEAT".equals(itemType)) {
            cost = 50;
            expChange = 70;
            itemName = "超级烤肉";
        } else if ("GACHA".equals(itemType)) {
            cost = 10;
            itemName = "神秘扭蛋";
            double rand = Math.random();
            if (rand < 0.10) {
                // 10% 概率：坏肚子，扣除 10~20 经验
                expChange = -(10 + (int) (Math.random() * 11));
            } else if (rand < 0.30) {
                // 20% 概率：大爆，增加 30~50 经验
                expChange = 30 + (int) (Math.random() * 21);
            } else {
                // 70% 概率：小赚，增加 10~20 经验
                expChange = 10 + (int) (Math.random() * 11);
            }
        } else if ("AMNESIA".equals(itemType)) {
            cost = 100;
            expChange = 0;
            itemName = "遗忘果实";
            isAmnesia = true;
        } else {
            throw new BizException(40001, "未知的物品类型");
        }

        int redeem = safe(student.getRedeemPoints());
        if (redeem < cost) {
            throw new BizException(40002, "余额不足，需要 " + cost + " 积分");
        }

        // 扣除余额
        student.setRedeemPoints(redeem - cost);

        // 如果是遗忘果实，清除宠物 ID，经验归零（重新养育）
        if (isAmnesia) {
            recordGalleryUnlock(student.getId(), student.getClassId(), student.getPetId(), safe(student.getLevel()));
            student.setPetId("");
            student.setExp(0);
            student.setLevel(1);
            student.setTitle(generateTitle(1, student.getClassId()));
        } else {
            // 计算经验
            int exp = Math.max(0, safe(student.getExp()) + expChange);
            LevelSnapshot snapshot = snapshotByExp(student.getClassId(), exp);
            student.setExp(exp);
            student.setLevel(snapshot.level);
            student.setTitle(generateTitle(snapshot.level, student.getClassId()));

            unlockResult = handleAutoUnlockAndReset(student, beforeLevel, beforePetId);
        }

        student.setUpdateTime(System.currentTimeMillis());
        studentMapper.updateById(student);

        // 写入特殊流水
        StudentEvent event = new StudentEvent();
        event.setClassId(student.getClassId());
        event.setStudentId(studentId);
        event.setChangeValue(0);
        event.setRedeemChange(-cost);
        event.setExpChange(isAmnesia ? 0 : expChange);
        if (isAmnesia) {
            event.setNote("noUndo=1");
        }
        event.setReason("喂小宠物 " + itemName
                + (expChange != 0 ? (expChange > 0 ? (" (EXP+" + expChange + ")") : (" (EXP" + expChange + ")")) : ""));
        event.setTimestamp(System.currentTimeMillis());
        studentEventMapper.insert(event);

        syncEventService.publishClassChange(student.getClassId(), "student_score_changed", studentId);

        Map<String, Object> result = new HashMap<>();
        StudentVO vo = toVO(student);
        vo.setGalleryUnlocked(unlockResult.unlocked);
        vo.setUnlockedPetId(unlockResult.unlockedPetId);
        result.put("student", vo);
        result.put("expChange", expChange);
        result.put("cost", cost);
        result.put("itemType", itemType);
        result.put("itemName", itemName);
        result.put("galleryUnlocked", unlockResult.unlocked);
        result.put("unlockedPetId", unlockResult.unlockedPetId);
        return result;
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
        scoreRequest.setRuleId(request.getRuleId());

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

    @Transactional
    public BatchAdoptResultVO batchAdopt(Long classId, BatchAdoptRequest request) {
        ensureClassOwnership(classId);

        boolean dryRun = request == null || request.getDryRun() == null || request.getDryRun();
        List<Student> students = studentMapper.selectList(new LambdaQueryWrapper<Student>()
                .eq(Student::getClassId, classId)
                .orderByAsc(Student::getId));
        students.sort(this::compareStudentByNoThenId);

        BatchAdoptResultVO result = new BatchAdoptResultVO();
        result.setDryRun(dryRun);
        result.setTotalStudents(students.size());

        com.classpets.backend.growth.vo.GrowthConfigVO config = growthConfigService.getForClass(classId);
        Map<String, String> petNameMap = new LinkedHashMap<>();
        List<String> routeIds = new ArrayList<>();
        if (config != null && config.getPetRoutes() != null) {
            for (com.classpets.backend.growth.vo.GrowthConfigVO.PetRouteVO route : config.getPetRoutes()) {
                if (route == null) {
                    continue;
                }
                String routeId = trim(route.getId());
                if (routeId.isEmpty()) {
                    continue;
                }
                if (Boolean.FALSE.equals(route.getEnabled())) {
                    continue;
                }
                if (!petNameMap.containsKey(routeId)) {
                    routeIds.add(routeId);
                }
                petNameMap.put(routeId, empty(route.getName(), routeId));
            }
        }

        int cursor = 0;
        int assignedCount = 0;
        int assignableCount = 0;
        List<BatchAdoptResultVO.ItemVO> items = new ArrayList<>();

        for (Student student : students) {
            BatchAdoptResultVO.ItemVO item = new BatchAdoptResultVO.ItemVO();
            item.setStudentId(student.getId());
            item.setStudentName(empty(student.getName(), "学生"));
            item.setStudentNo(empty(student.getStudentNo()));

            String currentPetId = trim(student.getPetId());
            if (!currentPetId.isEmpty()) {
                item.setStatus("skip");
                item.setReason("已有宠物");
                item.setPetId(currentPetId);
                item.setPetName(empty(petNameMap.get(currentPetId), currentPetId));
                items.add(item);
                continue;
            }

            if (routeIds.isEmpty()) {
                item.setStatus("skip");
                item.setReason("未配置可用宠物路线");
                items.add(item);
                continue;
            }

            Set<String> abandonedSet = new HashSet<>(abandonPetService.getAbandonedPetIds(student.getId()));
            int selectedIndex = findAssignableRouteIndex(routeIds, abandonedSet, cursor);
            if (selectedIndex < 0) {
                item.setStatus("skip");
                item.setReason("该生可选路线已全部被抛弃");
                items.add(item);
                continue;
            }

            String selectedRouteId = routeIds.get(selectedIndex);
            item.setStatus("assign");
            item.setReason(dryRun ? "预览：可分配" : "分配成功");
            item.setPetId(selectedRouteId);
            item.setPetName(empty(petNameMap.get(selectedRouteId), selectedRouteId));
            assignableCount += 1;

            if (!dryRun) {
                assignPetToStudent(student, selectedRouteId);
                assignedCount += 1;
            }
            cursor = (selectedIndex + 1) % routeIds.size();
            items.add(item);
        }

        if (!dryRun && assignedCount > 0) {
            syncEventService.publishClassChange(classId, "student_batch_adopted", null);
        }

        result.setAssignableCount(assignableCount);
        result.setAssignedCount(dryRun ? 0 : assignedCount);
        result.setSkippedCount(Math.max(0, students.size() - (dryRun ? assignableCount : assignedCount)));
        result.setItems(items);
        return result;
    }

    public void batchUpdateGroup(Long classId, com.classpets.backend.student.dto.BatchGroupRequest request) {
        ensureClassOwnership(classId);
        if (request.getStudentIds() == null || request.getStudentIds().isEmpty()) {
            return;
        }

        studentMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Student>()
                .eq(Student::getClassId, classId)
                .in(Student::getId, request.getStudentIds())
                .set(Student::getGroupId, request.getGroupId())
                .set(Student::getUpdateTime, System.currentTimeMillis()));
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

        String oldPetId = trim(student.getPetId());
        String newPetId = trim(req.getPetId());
        boolean hadPet = !oldPetId.isEmpty();
        boolean hasNewPet = !newPetId.isEmpty();
        boolean petChanged = hadPet && hasNewPet && !oldPetId.equals(newPetId);

        if (student.getId() == null) {
            // 新建学生：exp=0
            student.setExp(0);
            student.setLevel(1);
            student.setTitle(generateTitle(1, student.getClassId()));
            student.setPetId(newPetId);
        } else if (!hadPet && hasNewPet) {
            // 首次领养：保留原有成长数据，仅绑定宠物
            int exp = Math.max(0, safe(student.getExp()));
            LevelSnapshot snapshot = snapshotByExp(student.getClassId(), exp);
            student.setLevel(snapshot.level);
            student.setTitle(generateTitle(snapshot.level, student.getClassId()));
            student.setPetId(newPetId);
        } else if (petChanged) {
            // 更换宠物
            int currentLevel = safe(student.getLevel());
            int maxLevel = getMaxLevel(student.getClassId());

            if (currentLevel >= maxLevel) {
                // 满级换宠：记录旧宠物图鉴
                recordGalleryUnlock(student.getId(), student.getClassId(), oldPetId, currentLevel);
            } else {
                // 未满级换宠：记录抛弃
                abandonPetService.abandonPet(student.getClassId(), student.getId(), oldPetId);
            }

            // 切换到新宠物并重新开始成长
            student.setPetId(newPetId);
            student.setExp(0);
            student.setLevel(1);
            student.setTitle(generateTitle(1, student.getClassId()));
        } else {
            // 普通编辑：保留 exp
            int exp = Math.max(0, safe(student.getExp()));
            LevelSnapshot snapshot = snapshotByExp(student.getClassId(), exp);
            student.setLevel(snapshot.level);
            student.setTitle(generateTitle(snapshot.level, student.getClassId()));
            // 普通编辑时保留原有宠物
            student.setPetId(oldPetId);
        }
        student.setGroupId(req.getGroupId());
    }

    private void recordGalleryUnlock(Long studentId, Long classId, String petId, int currentLevel) {
        if (studentId == null || petId == null || petId.trim().isEmpty()) {
            return;
        }
        int maxLevel = getMaxLevel(classId);
        if (currentLevel < maxLevel) {
            return; // 未满级不收录
        }

        // 检查是否已收录
        boolean exists = studentPetGalleryMapper.selectCount(new LambdaQueryWrapper<StudentPetGallery>()
                .eq(StudentPetGallery::getStudentId, studentId)
                .eq(StudentPetGallery::getPetRouteId, petId)) > 0;
        if (exists) {
            return;
        }

        // 获取宠物名称
        String petName = "未知宠物";
        com.classpets.backend.growth.vo.GrowthConfigVO configVO = growthConfigService.getForClass(classId);
        if (configVO != null && configVO.getPetRoutes() != null) {
            for (com.classpets.backend.growth.vo.GrowthConfigVO.PetRouteVO route : configVO.getPetRoutes()) {
                if (petId.equals(route.getId())) {
                    petName = route.getName();
                    break;
                }
            }
        }

        StudentPetGallery gallery = new StudentPetGallery();
        gallery.setClassId(classId);
        gallery.setStudentId(studentId);
        gallery.setPetRouteId(petId);
        gallery.setPetName(petName);
        gallery.setUnlockTime(System.currentTimeMillis());
        studentPetGalleryMapper.insert(gallery);
    }

    public List<StudentPetGalleryVO> getStudentGallery(Long classId, Long studentId) {
        ensureClassOwnership(classId);
        Student student = mustGet(studentId);
        if (!student.getClassId().equals(classId)) {
            throw new BizException(40401, "学生不在该班级中");
        }
        List<StudentPetGallery> list = studentPetGalleryMapper.selectList(
                new LambdaQueryWrapper<StudentPetGallery>()
                        .eq(StudentPetGallery::getStudentId, studentId)
                        .orderByDesc(StudentPetGallery::getUnlockTime));

        return list.stream().map(g -> {
            StudentPetGalleryVO vo = new StudentPetGalleryVO();
            vo.setPetRouteId(g.getPetRouteId());
            vo.setPetName(g.getPetName());
            vo.setUnlockTime(g.getUnlockTime());
            return vo;
        }).collect(Collectors.toList());
    }

    public List<String> getAbandonedPetIds(Long studentId) {
        Student student = mustGet(studentId);
        ensureClassOwnership(student.getClassId());
        return abandonPetService.getAbandonedPetIds(studentId);
    }

    public void abandonPet(Long studentId, Long classId, String petId) {
        Student student = mustGet(studentId);
        ensureClassOwnership(student.getClassId());
        if (!student.getClassId().equals(classId)) {
            throw new BizException(40001, "班级不匹配");
        }
        String normalizedPetId = trim(petId);
        if (normalizedPetId.isEmpty()) {
            throw new BizException(40001, "petId 不能为空");
        }
        abandonPetService.abandonPet(classId, studentId, normalizedPetId);
    }

    @Transactional
    public StudentVO adoptPet(Long studentId, String petId) {
        Student student = mustGet(studentId);
        ensureClassOwnership(student.getClassId());

        String normalizedPetId = trim(petId);
        if (normalizedPetId.isEmpty()) {
            throw new BizException(40001, "petId 不能为空");
        }
        if (!trim(student.getPetId()).isEmpty()) {
            throw new BizException(40001, "当前学生已有宠物，请先更换");
        }
        if (abandonPetService.isPetAbandoned(studentId, normalizedPetId)) {
            throw new BizException(40001, "该宠物已被抛弃，无法再次领养");
        }

        assignPetToStudent(student, normalizedPetId);
        return toVO(student);
    }

    private void assignPetToStudent(Student student, String petId) {
        int exp = Math.max(0, safe(student.getExp()));
        LevelSnapshot snapshot = snapshotByExp(student.getClassId(), exp);
        student.setLevel(snapshot.level);
        student.setTitle(generateTitle(snapshot.level, student.getClassId()));
        student.setPetId(trim(petId));
        student.setUpdateTime(System.currentTimeMillis());
        studentMapper.updateById(student);
    }

    private int findAssignableRouteIndex(List<String> routeIds, Set<String> abandonedSet, int startIndex) {
        if (routeIds == null || routeIds.isEmpty()) {
            return -1;
        }
        int size = routeIds.size();
        int safeStart = Math.max(0, startIndex) % size;
        for (int offset = 0; offset < size; offset++) {
            int idx = (safeStart + offset) % size;
            String routeId = routeIds.get(idx);
            if (routeId == null || routeId.trim().isEmpty()) {
                continue;
            }
            if (!abandonedSet.contains(routeId)) {
                return idx;
            }
        }
        return -1;
    }

    private int compareStudentByNoThenId(Student a, Student b) {
        String noA = trim(a == null ? null : a.getStudentNo());
        String noB = trim(b == null ? null : b.getStudentNo());
        boolean numA = noA.matches("^\\d+$");
        boolean numB = noB.matches("^\\d+$");

        if (numA && numB) {
            int cmpNum = Long.compare(Long.parseLong(noA), Long.parseLong(noB));
            if (cmpNum != 0) {
                return cmpNum;
            }
        } else if (numA != numB) {
            return numA ? -1 : 1;
        } else if (!noA.equals(noB)) {
            int cmpNo = noA.compareTo(noB);
            if (cmpNo != 0) {
                return cmpNo;
            }
        }

        long idA = a == null || a.getId() == null ? Long.MAX_VALUE : a.getId();
        long idB = b == null || b.getId() == null ? Long.MAX_VALUE : b.getId();
        return Long.compare(idA, idB);
    }

    private int getMaxLevel(Long classId) {
        GrowthConfigService.ResolvedGrowthConfig config = growthConfigService.resolveForClass(classId);
        java.util.List<Integer> levels = config.getLevelThresholds();
        return Math.min(levels.size(), 10);
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

    private int resolveExpChangeValueForGrowth(int beforeTotalPoints, int changeValue) {
        if (changeValue <= 0) {
            return changeValue;
        }
        int beforePositive = Math.max(0, beforeTotalPoints);
        int afterPositive = Math.max(0, beforeTotalPoints + changeValue);
        return Math.max(0, afterPositive - beforePositive);
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
        vo.setTitle(student.getTitle());
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

    private Long resolveValidRuleId(Long ruleId, Long classId, String mode) {
        if (ruleId == null) {
            return null;
        }
        RuleInfo rule = ruleInfoMapper.selectById(ruleId);
        if (rule == null || !classId.equals(rule.getClassId())) {
            throw new BizException(40001, "规则不存在或不属于当前班级");
        }
        Integer targetType = rule.getTargetType();
        if (targetType == null || targetType != 0) {
            throw new BizException(40001, "该规则不支持学生加减分");
        }
        String ruleType = trim(rule.getType()).toLowerCase();
        if (!ruleType.isEmpty() && !ruleType.equals(mode)) {
            throw new BizException(40001, "规则类型与本次操作不一致");
        }
        return ruleId;
    }

    private String empty(String value) {
        return empty(value, "");
    }

    private String empty(String value, String fallback) {
        String v = trim(value);
        return v.isEmpty() ? fallback : v;
    }

    private UnlockResult handleAutoUnlockAndReset(Student student, int beforeLevel, String beforePetId) {
        String petId = trim(beforePetId);
        if (petId.isEmpty()) {
            return UnlockResult.none();
        }

        int maxLevel = getMaxLevel(student.getClassId());
        int afterLevel = safe(student.getLevel());
        boolean reachMaxNow = beforeLevel < maxLevel && afterLevel >= maxLevel;
        if (!reachMaxNow) {
            return UnlockResult.none();
        }

        recordGalleryUnlock(student.getId(), student.getClassId(), petId, afterLevel);
        abandonPetService.abandonPet(student.getClassId(), student.getId(), petId);

        student.setPetId("");
        student.setExp(0);
        student.setLevel(1);
        student.setTitle(generateTitle(1, student.getClassId()));
        return UnlockResult.unlocked(petId);
    }

    private static class UnlockResult {
        private final boolean unlocked;
        private final String unlockedPetId;

        private UnlockResult(boolean unlocked, String unlockedPetId) {
            this.unlocked = unlocked;
            this.unlockedPetId = unlockedPetId;
        }

        private static UnlockResult none() {
            return new UnlockResult(false, "");
        }

        private static UnlockResult unlocked(String petId) {
            return new UnlockResult(true, petId == null ? "" : petId);
        }
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
                    ? 0
                    : recalculateExpFromEvents(studentEvents, expGainRatio);
            LevelSnapshot snapshot = snapshotByExp(classId, exp);
            student.setExp(exp);
            student.setLevel(snapshot.level);
            student.setTitle(generateTitle(snapshot.level, student.getClassId()));
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
        int totalPoints = 0;
        for (StudentEvent event : sorted) {
            int delta = event.getChangeValue() == null ? 0 : event.getChangeValue();
            if (delta == 0) {
                continue;
            }
            int expChangeValue = resolveExpChangeValueForGrowth(totalPoints, delta);
            exp = Math.max(0, exp + calcExpDeltaFromChange(expChangeValue, ratio));
            totalPoints += delta;
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

    private String generateTitle(int level, Long classId) {
        try {
            var config = growthConfigService.resolveForClass(classId);
            int stage1Max = config.getStage1MaxLevel();
            int stage2Max = config.getStage2MaxLevel();

            if (level <= stage1Max) {
                return "灵兽";
            } else if (level <= stage2Max) {
                return "神兽";
            } else {
                return "传说神兽";
            }
        } catch (Exception e) {
            log.warn("获取称号失败，使用默认称号", e);
            if (level <= 3) {
                return "灵兽";
            } else if (level <= 6) {
                return "神兽";
            } else {
                return "传说神兽";
            }
        }
    }
}
