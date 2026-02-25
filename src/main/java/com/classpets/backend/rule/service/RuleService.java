package com.classpets.backend.rule.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classpets.backend.classinfo.service.ClassInfoService;
import com.classpets.backend.common.BizException;
import com.classpets.backend.entity.RuleCategory;
import com.classpets.backend.entity.RuleInfo;
import com.classpets.backend.mapper.RuleCategoryMapper;
import com.classpets.backend.mapper.RuleInfoMapper;
import com.classpets.backend.rule.dto.RuleUpsertRequest;
import com.classpets.backend.rule.vo.RuleVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RuleService {

    private final RuleInfoMapper ruleInfoMapper;
    private final RuleCategoryMapper ruleCategoryMapper;
    private final ClassInfoService classInfoService;

    public RuleService(RuleInfoMapper ruleInfoMapper, RuleCategoryMapper ruleCategoryMapper, ClassInfoService classInfoService) {
        this.ruleInfoMapper = ruleInfoMapper;
        this.ruleCategoryMapper = ruleCategoryMapper;
        this.classInfoService = classInfoService;
    }

    public List<RuleVO> listByClass(Long classId) {
        ensureClassOwnership(classId);
        List<RuleInfo> rules = ruleInfoMapper.selectList(new LambdaQueryWrapper<RuleInfo>()
                .eq(RuleInfo::getClassId, classId)
                .orderByDesc(RuleInfo::getId));
        return rules.stream().map(this::toVO).collect(Collectors.toList());
    }

    public RuleVO create(Long classId, RuleUpsertRequest req) {
        ensureClassOwnership(classId);
        validate(req);

        RuleInfo rule = new RuleInfo();
        rule.setClassId(classId);
        apply(req, rule);
        rule.setEnabled(1);
        ruleInfoMapper.insert(rule);
        return toVO(rule);
    }

    public RuleVO update(Long ruleId, RuleUpsertRequest req) {
        RuleInfo rule = mustGet(ruleId);
        ensureClassOwnership(rule.getClassId());
        validate(req);

        apply(req, rule);
        ruleInfoMapper.updateById(rule);
        return toVO(rule);
    }

    public void delete(Long ruleId) {
        RuleInfo rule = mustGet(ruleId);
        ensureClassOwnership(rule.getClassId());
        ruleInfoMapper.deleteById(ruleId);
    }

    public RuleVO toggleEnabled(Long ruleId) {
        RuleInfo rule = mustGet(ruleId);
        ensureClassOwnership(rule.getClassId());
        rule.setEnabled(rule.getEnabled() == 1 ? 0 : 1);
        ruleInfoMapper.updateById(rule);
        return toVO(rule);
    }

    private void ensureClassOwnership(Long classId) {
        if (!classInfoService.isOwnedByCurrentTeacher(classId)) {
            throw new BizException(40301, "无权访问此班级");
        }
    }

    private RuleInfo mustGet(Long id) {
        RuleInfo rule = ruleInfoMapper.selectById(id);
        if (rule == null) {
            throw new BizException(40401, "规则不存在");
        }
        return rule;
    }

    private void validate(RuleUpsertRequest req) {
        if (req.getContent() == null || req.getContent().trim().isEmpty()) {
            throw new BizException(40001, "规则内容不能为空");
        }
        if (req.getPoints() == null || req.getPoints() <= 0) {
            throw new BizException(40001, "分值必须大于 0");
        }
        String type = req.getType();
        if (type == null || (!type.equals("add") && !type.equals("deduct"))) {
            throw new BizException(40001, "类型必须为 add 或 deduct");
        }
    }

    private void apply(RuleUpsertRequest req, RuleInfo rule) {
        Integer safeTargetType = req.getTargetType() != null ? req.getTargetType() : 0;
        RuleCategory category = resolveCategory(rule.getClassId(), req, safeTargetType);

        rule.setContent(req.getContent().trim());
        rule.setPoints(req.getPoints());
        rule.setType(req.getType());
        rule.setCategory(category.getName());
        rule.setCategoryId(category.getId());
        rule.setTargetType(safeTargetType); // Default to Student (0)
        rule.setCooldownHours(req.getCooldownHours() != null ? req.getCooldownHours() : BigDecimal.ZERO);
        rule.setStackable(req.getStackable() != null ? req.getStackable() : 1);
    }

    private RuleCategory resolveCategory(Long classId, RuleUpsertRequest req, Integer targetType) {
        if (targetType != 0 && targetType != 1) {
            throw new BizException(40001, "targetType 必须为 0 或 1");
        }

        if (req.getCategoryId() != null) {
            RuleCategory category = ruleCategoryMapper.selectById(req.getCategoryId());
            if (category == null) {
                throw new BizException(40401, "分类不存在");
            }
            if (!classId.equals(category.getClassId())) {
                throw new BizException(40301, "无权访问此分类");
            }
            if (!targetType.equals(category.getTargetType())) {
                throw new BizException(40001, "分类与适用对象不匹配");
            }
            return category;
        }

        String categoryName = req.getCategory() != null ? req.getCategory().trim() : "";
        if (categoryName.isEmpty()) {
            categoryName = "其他";
        }

        RuleCategory existing = ruleCategoryMapper.selectOne(new LambdaQueryWrapper<RuleCategory>()
                .eq(RuleCategory::getClassId, classId)
                .eq(RuleCategory::getTargetType, targetType)
                .eq(RuleCategory::getName, categoryName)
                .last("LIMIT 1"));
        if (existing != null) {
            if (existing.getEnabled() != null && existing.getEnabled() == 0) {
                existing.setEnabled(1);
                ruleCategoryMapper.updateById(existing);
            }
            return existing;
        }

        RuleCategory created = new RuleCategory();
        created.setClassId(classId);
        created.setTargetType(targetType);
        created.setName(categoryName);
        created.setSort(0);
        created.setEnabled(1);
        ruleCategoryMapper.insert(created);
        return created;
    }

    private RuleVO toVO(RuleInfo rule) {
        RuleVO vo = new RuleVO();
        vo.setId(rule.getId());
        vo.setContent(rule.getContent());
        vo.setPoints(rule.getPoints());
        vo.setType(rule.getType());
        vo.setTargetType(rule.getTargetType());
        vo.setCategory(rule.getCategory());
        vo.setCategoryId(rule.getCategoryId());
        vo.setEnabled(rule.getEnabled());
        vo.setCooldownHours(rule.getCooldownHours());
        vo.setStackable(rule.getStackable());
        return vo;
    }
}
