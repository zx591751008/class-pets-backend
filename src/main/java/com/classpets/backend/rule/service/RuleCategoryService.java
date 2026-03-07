package com.classpets.backend.rule.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classpets.backend.classinfo.service.ClassInfoService;
import com.classpets.backend.common.BizException;
import com.classpets.backend.entity.RuleCategory;
import com.classpets.backend.entity.RuleInfo;
import com.classpets.backend.mapper.RuleCategoryMapper;
import com.classpets.backend.mapper.RuleInfoMapper;
import com.classpets.backend.rule.dto.RuleCategoryCreateRequest;
import com.classpets.backend.rule.vo.RuleCategoryVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RuleCategoryService {

    private final RuleCategoryMapper ruleCategoryMapper;
    private final RuleInfoMapper ruleInfoMapper;
    private final ClassInfoService classInfoService;

    public RuleCategoryService(RuleCategoryMapper ruleCategoryMapper, RuleInfoMapper ruleInfoMapper, ClassInfoService classInfoService) {
        this.ruleCategoryMapper = ruleCategoryMapper;
        this.ruleInfoMapper = ruleInfoMapper;
        this.classInfoService = classInfoService;
    }

    public List<RuleCategoryVO> listByClass(Long classId, Integer targetType) {
        ensureClassOwnership(classId);
        LambdaQueryWrapper<RuleCategory> wrapper = new LambdaQueryWrapper<RuleCategory>()
                .eq(RuleCategory::getClassId, classId)
                .eq(RuleCategory::getEnabled, 1)
                .orderByAsc(RuleCategory::getSort)
                .orderByAsc(RuleCategory::getId);
        if (targetType != null) {
            wrapper.eq(RuleCategory::getTargetType, targetType);
        }
        return ruleCategoryMapper.selectList(wrapper).stream().map(this::toVO).collect(Collectors.toList());
    }

    public RuleCategoryVO create(Long classId, RuleCategoryCreateRequest req) {
        ensureClassOwnership(classId);
        if (req.getName() == null || req.getName().trim().isEmpty()) {
            throw new BizException(40001, "分类名称不能为空");
        }
        Integer targetType = req.getTargetType() != null ? req.getTargetType() : 0;
        if (targetType != 0 && targetType != 1) {
            throw new BizException(40001, "targetType 必须为 0 或 1");
        }
        String name = req.getName().trim();

        RuleCategory exists = ruleCategoryMapper.selectOne(new LambdaQueryWrapper<RuleCategory>()
                .eq(RuleCategory::getClassId, classId)
                .eq(RuleCategory::getTargetType, targetType)
                .eq(RuleCategory::getName, name)
                .last("LIMIT 1"));
        if (exists != null) {
            if (exists.getEnabled() != null && exists.getEnabled() == 0) {
                exists.setEnabled(1);
                ruleCategoryMapper.updateById(exists);
            }
            return toVO(exists);
        }

        RuleCategory category = new RuleCategory();
        category.setClassId(classId);
        category.setName(name);
        category.setTargetType(targetType);
        category.setSort(0);
        category.setEnabled(1);
        ruleCategoryMapper.insert(category);
        return toVO(category);
    }

    public void delete(Long classId, Long categoryId) {
        ensureClassOwnership(classId);
        RuleCategory category = ruleCategoryMapper.selectById(categoryId);
        if (category == null || !classId.equals(category.getClassId())) {
            throw new BizException(40401, "分类不存在");
        }

        long relatedRuleCount = countRelatedRules(classId, category);
        if (relatedRuleCount > 0) {
            throw new BizException(40001, "该分类下还有 " + relatedRuleCount + " 条规则，请先删除规则后再删除分类");
        }

        category.setEnabled(0);
        ruleCategoryMapper.updateById(category);
    }

    private long countRelatedRules(Long classId, RuleCategory category) {
        Long count = ruleInfoMapper.selectCount(new LambdaQueryWrapper<RuleInfo>()
                .eq(RuleInfo::getClassId, classId)
                .and(w -> w.eq(RuleInfo::getCategoryId, category.getId())
                        .or(v -> v.eq(RuleInfo::getTargetType, category.getTargetType())
                                .eq(RuleInfo::getCategory, category.getName()))));
        return count == null ? 0 : count;
    }

    private void ensureClassOwnership(Long classId) {
        if (!classInfoService.isOwnedByCurrentTeacher(classId)) {
            throw new BizException(40301, "无权访问此班级");
        }
    }

    private RuleCategoryVO toVO(RuleCategory category) {
        RuleCategoryVO vo = new RuleCategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setTargetType(category.getTargetType());
        vo.setSort(category.getSort());
        vo.setEnabled(category.getEnabled());
        return vo;
    }
}
