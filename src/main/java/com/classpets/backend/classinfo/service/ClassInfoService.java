package com.classpets.backend.classinfo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classpets.backend.auth.service.AuthService;
import com.classpets.backend.classinfo.dto.ClassCreateRequest;
import com.classpets.backend.classinfo.vo.ClassVO;
import com.classpets.backend.common.BizException;
import com.classpets.backend.entity.ClassInfo;
import com.classpets.backend.entity.GroupEvent;
import com.classpets.backend.entity.GroupInfo;
import com.classpets.backend.entity.RedemptionRecord;
import com.classpets.backend.entity.RuleCategory;
import com.classpets.backend.entity.RuleInfo;
import com.classpets.backend.entity.StoreItem;
import com.classpets.backend.entity.Student;
import com.classpets.backend.entity.StudentEvent;
import com.classpets.backend.entity.ClassConfig;
import com.classpets.backend.mapper.ClassInfoMapper;
import com.classpets.backend.mapper.ClassConfigMapper;
import com.classpets.backend.mapper.GroupEventMapper;
import com.classpets.backend.mapper.GroupInfoMapper;
import com.classpets.backend.mapper.RedemptionRecordMapper;
import com.classpets.backend.mapper.RuleCategoryMapper;
import com.classpets.backend.mapper.RuleInfoMapper;
import com.classpets.backend.mapper.StoreItemMapper;
import com.classpets.backend.mapper.StudentEventMapper;
import com.classpets.backend.mapper.StudentMapper;
import com.classpets.backend.sync.SyncEventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClassInfoService {

    private final ClassInfoMapper classInfoMapper;
    private final StudentMapper studentMapper;
    private final GroupInfoMapper groupInfoMapper;
    private final StudentEventMapper studentEventMapper;
    private final GroupEventMapper groupEventMapper;
    private final RuleCategoryMapper ruleCategoryMapper;
    private final RuleInfoMapper ruleInfoMapper;
    private final StoreItemMapper storeItemMapper;
    private final RedemptionRecordMapper redemptionRecordMapper;
    private final ClassConfigMapper classConfigMapper;
    private final AuthService authService;
    private final SyncEventService syncEventService;

    public ClassInfoService(ClassInfoMapper classInfoMapper,
            StudentMapper studentMapper,
            GroupInfoMapper groupInfoMapper,
            StudentEventMapper studentEventMapper,
            GroupEventMapper groupEventMapper,
            RuleCategoryMapper ruleCategoryMapper,
            RuleInfoMapper ruleInfoMapper,
            StoreItemMapper storeItemMapper,
            RedemptionRecordMapper redemptionRecordMapper,
            ClassConfigMapper classConfigMapper,
            AuthService authService,
            SyncEventService syncEventService) {
        this.classInfoMapper = classInfoMapper;
        this.studentMapper = studentMapper;
        this.groupInfoMapper = groupInfoMapper;
        this.studentEventMapper = studentEventMapper;
        this.groupEventMapper = groupEventMapper;
        this.ruleCategoryMapper = ruleCategoryMapper;
        this.ruleInfoMapper = ruleInfoMapper;
        this.storeItemMapper = storeItemMapper;
        this.redemptionRecordMapper = redemptionRecordMapper;
        this.classConfigMapper = classConfigMapper;
        this.authService = authService;
        this.syncEventService = syncEventService;
    }

    public List<ClassVO> list() {
        Long teacherId = requireTeacherId();
        List<ClassInfo> list = classInfoMapper.selectList(new LambdaQueryWrapper<ClassInfo>()
                .eq(ClassInfo::getTeacherId, teacherId)
                .orderByDesc(ClassInfo::getId));
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public ClassVO create(ClassCreateRequest request) {
        Long teacherId = requireTeacherId();
        ClassInfo classInfo = new ClassInfo();
        classInfo.setTeacherId(teacherId);
        classInfo.setName(trim(request.getName()));
        classInfo.setTeacherName(trim(request.getTeacherName()).isEmpty() ? "老师" : trim(request.getTeacherName()));
        classInfoMapper.insert(classInfo);
        seedDefaultRulesAndStoreItems(classInfo.getId());
        return toVO(classInfo);
    }

    public ClassVO update(Long classId, ClassCreateRequest request) {
        if (!isOwnedByCurrentTeacher(classId)) {
            throw new BizException(40301, "无权访问此班级");
        }
        ClassInfo classInfo = classInfoMapper.selectById(classId);
        if (classInfo == null) {
            throw new BizException(40401, "班级不存在");
        }
        String name = trim(request.getName());
        if (name.isEmpty()) {
            throw new BizException(40001, "班级名称不能为空");
        }
        classInfo.setName(name);
        String teacherName = trim(request.getTeacherName());
        if (!teacherName.isEmpty()) {
            classInfo.setTeacherName(teacherName);
        }
        classInfoMapper.updateById(classInfo);
        return toVO(classInfo);
    }

    /**
     * Check if a class belongs to the current teacher
     */
    public boolean isOwnedByCurrentTeacher(Long classId) {
        Long teacherId = authService.getCurrentTeacherId();
        if (teacherId == null)
            return false;
        ClassInfo classInfo = classInfoMapper.selectById(classId);
        return classInfo != null && teacherId.equals(classInfo.getTeacherId());
    }

    /**
     * Reset scores for a class:
     * - student total_points / redeem_points / exp = 0
     * - group points = 0
     */
    public void resetScores(Long classId) {
        if (!isOwnedByCurrentTeacher(classId)) {
            throw new BizException(40301, "无权访问此班级");
        }

        long now = System.currentTimeMillis();

        studentMapper.update(null, new LambdaUpdateWrapper<Student>()
                .eq(Student::getClassId, classId)
                .set(Student::getTotalPoints, 0)
                .set(Student::getRedeemPoints, 0)
                .set(Student::getExp, 0)
                .set(Student::getUpdateTime, now));

        groupInfoMapper.update(null, new LambdaUpdateWrapper<GroupInfo>()
                .eq(GroupInfo::getClassId, classId)
                .set(GroupInfo::getPoints, 0));

        // Clear score events so analytics/rankings for recent periods are reset too
        studentEventMapper.delete(new LambdaQueryWrapper<StudentEvent>()
                .eq(StudentEvent::getClassId, classId));

        groupEventMapper.delete(new LambdaQueryWrapper<GroupEvent>()
                .eq(GroupEvent::getClassId, classId));

        syncEventService.publishClassChange(classId, "class_scores_reset", classId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long classId) {
        if (!isOwnedByCurrentTeacher(classId)) {
            throw new BizException(40301, "无权访问此班级");
        }

        ClassInfo classInfo = classInfoMapper.selectById(classId);
        if (classInfo == null) {
            throw new BizException(40401, "班级不存在");
        }

        // Delete child data first to satisfy foreign key constraints
        studentEventMapper.delete(new LambdaQueryWrapper<StudentEvent>()
                .eq(StudentEvent::getClassId, classId));

        groupEventMapper.delete(new LambdaQueryWrapper<GroupEvent>()
                .eq(GroupEvent::getClassId, classId));

        redemptionRecordMapper.delete(new LambdaQueryWrapper<RedemptionRecord>()
                .eq(RedemptionRecord::getClassId, classId));

        storeItemMapper.delete(new LambdaQueryWrapper<StoreItem>()
                .eq(StoreItem::getClassId, classId));

        ruleInfoMapper.delete(new LambdaQueryWrapper<RuleInfo>()
                .eq(RuleInfo::getClassId, classId));

        studentMapper.delete(new LambdaQueryWrapper<Student>()
                .eq(Student::getClassId, classId));

        groupInfoMapper.delete(new LambdaQueryWrapper<GroupInfo>()
                .eq(GroupInfo::getClassId, classId));

        classConfigMapper.delete(new LambdaQueryWrapper<ClassConfig>()
                .eq(ClassConfig::getClassId, classId));

        classInfoMapper.deleteById(classId);
    }

    private Long requireTeacherId() {
        Long teacherId = authService.getCurrentTeacherId();
        if (teacherId == null) {
            throw new BizException(40101, "未登录或登录已过期");
        }
        return teacherId;
    }

    private ClassVO toVO(ClassInfo classInfo) {
        ClassVO vo = new ClassVO();
        vo.setId(classInfo.getId());
        vo.setName(classInfo.getName());
        vo.setTeacherName(classInfo.getTeacherName());
        return vo;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private void seedDefaultRulesAndStoreItems(Long classId) {
        if (classId == null) {
            return;
        }
        seedDefaultRules(classId);
        seedDefaultStoreItems(classId);
    }

    private void seedDefaultRules(Long classId) {
        Long existingCount = ruleInfoMapper.selectCount(new LambdaQueryWrapper<RuleInfo>()
                .eq(RuleInfo::getClassId, classId));
        if (existingCount != null && existingCount > 0) {
            return;
        }

        Long classroomCategoryId = ensureCategory(classId, "课堂表现", 0, 10);
        Long homeworkCategoryId = ensureCategory(classId, "作业学习", 0, 20);
        Long disciplineCategoryId = ensureCategory(classId, "纪律规范", 0, 30);
        Long teamworkCategoryId = ensureCategory(classId, "团队协作", 1, 10);

        List<RuleInfo> defaults = new ArrayList<>();
        defaults.add(buildRule(classId, "主动回答问题", 2, "add", 0, "课堂表现", classroomCategoryId));
        defaults.add(buildRule(classId, "帮助同学", 3, "add", 0, "课堂表现", classroomCategoryId));
        defaults.add(buildRule(classId, "上课走神", 2, "deduct", 0, "课堂表现", classroomCategoryId));
        defaults.add(buildRule(classId, "作业按时完成", 2, "add", 0, "作业学习", homeworkCategoryId));
        defaults.add(buildRule(classId, "作业优秀", 3, "add", 0, "作业学习", homeworkCategoryId));
        defaults.add(buildRule(classId, "作业未完成", 3, "deduct", 0, "作业学习", homeworkCategoryId));
        defaults.add(buildRule(classId, "课堂纪律良好", 2, "add", 0, "纪律规范", disciplineCategoryId));
        defaults.add(buildRule(classId, "课堂喧哗", 2, "deduct", 0, "纪律规范", disciplineCategoryId));
        defaults.add(buildRule(classId, "小组合作优秀", 5, "add", 1, "团队协作", teamworkCategoryId));
        defaults.add(buildRule(classId, "小组任务未完成", 4, "deduct", 1, "团队协作", teamworkCategoryId));

        for (RuleInfo rule : defaults) {
            ruleInfoMapper.insert(rule);
        }
    }

    private void seedDefaultStoreItems(Long classId) {
        Long existingCount = storeItemMapper.selectCount(new LambdaQueryWrapper<StoreItem>()
                .eq(StoreItem::getClassId, classId));
        if (existingCount != null && existingCount > 0) {
            return;
        }

        long now = System.currentTimeMillis();
        List<StoreItem> defaults = new ArrayList<>();
        defaults.add(buildStoreItem(classId, "文具盲盒", "可兑换一份学习文具盲盒", 30, 20, "📦", now));
        defaults.add(buildStoreItem(classId, "免作业券", "可抵扣一次当日作业", 50, 10, "🎫", now));
        defaults.add(buildStoreItem(classId, "课间优先券", "下课可优先排队领取奖励", 20, -1, "⚡", now));
        defaults.add(buildStoreItem(classId, "班级荣誉贴纸", "兑换一张荣誉贴纸", 15, -1, "⭐", now));
        defaults.add(buildStoreItem(classId, "神秘礼物", "班主任准备的神秘奖励", 80, 5, "🎁", now));

        for (StoreItem item : defaults) {
            storeItemMapper.insert(item);
        }
    }

    private Long ensureCategory(Long classId, String name, Integer targetType, Integer sort) {
        RuleCategory existing = ruleCategoryMapper.selectOne(new LambdaQueryWrapper<RuleCategory>()
                .eq(RuleCategory::getClassId, classId)
                .eq(RuleCategory::getTargetType, targetType)
                .eq(RuleCategory::getName, name)
                .last("LIMIT 1"));
        if (existing != null) {
            if (existing.getEnabled() == null || existing.getEnabled() == 0) {
                existing.setEnabled(1);
                ruleCategoryMapper.updateById(existing);
            }
            return existing.getId();
        }

        RuleCategory category = new RuleCategory();
        category.setClassId(classId);
        category.setName(name);
        category.setTargetType(targetType);
        category.setSort(sort);
        category.setEnabled(1);
        ruleCategoryMapper.insert(category);
        return category.getId();
    }

    private RuleInfo buildRule(Long classId, String content, Integer points, String type, Integer targetType, String categoryName,
            Long categoryId) {
        RuleInfo rule = new RuleInfo();
        rule.setClassId(classId);
        rule.setContent(content);
        rule.setPoints(points);
        rule.setType(type);
        rule.setTargetType(targetType);
        rule.setCategory(categoryName);
        rule.setCategoryId(categoryId);
        rule.setEnabled(1);
        rule.setCooldownHours(BigDecimal.ZERO);
        rule.setStackable(1);
        return rule;
    }

    private StoreItem buildStoreItem(Long classId, String name, String description, Integer cost, Integer stock, String icon,
            long now) {
        StoreItem item = new StoreItem();
        item.setClassId(classId);
        item.setName(name);
        item.setDescription(description);
        item.setCost(cost);
        item.setStock(stock);
        item.setIcon(icon);
        item.setIsActive(true);
        item.setIsDeleted(false);
        item.setCreateTime(now);
        item.setUpdateTime(now);
        return item;
    }
}
