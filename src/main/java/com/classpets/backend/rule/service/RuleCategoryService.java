package com.classpets.backend.rule.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classpets.backend.classinfo.service.ClassInfoService;
import com.classpets.backend.common.BizException;
import com.classpets.backend.entity.RuleCategory;
import com.classpets.backend.mapper.RuleCategoryMapper;
import com.classpets.backend.rule.dto.RuleCategoryCreateRequest;
import com.classpets.backend.rule.vo.RuleCategoryVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RuleCategoryService {

    private final RuleCategoryMapper ruleCategoryMapper;
    private final ClassInfoService classInfoService;

    public RuleCategoryService(RuleCategoryMapper ruleCategoryMapper, ClassInfoService classInfoService) {
        this.ruleCategoryMapper = ruleCategoryMapper;
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
