package com.classpets.backend.rule.controller;

import com.classpets.backend.common.ApiResponse;
import com.classpets.backend.rule.dto.RuleCategoryCreateRequest;
import com.classpets.backend.rule.service.RuleCategoryService;
import com.classpets.backend.rule.vo.RuleCategoryVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/classes/{classId}/rule-categories")
public class RuleCategoryController {

    private final RuleCategoryService ruleCategoryService;

    public RuleCategoryController(RuleCategoryService ruleCategoryService) {
        this.ruleCategoryService = ruleCategoryService;
    }

    @GetMapping
    public ApiResponse<List<RuleCategoryVO>> list(@PathVariable Long classId,
            @RequestParam(required = false) Integer targetType) {
        return ApiResponse.ok(ruleCategoryService.listByClass(classId, targetType));
    }

    @PostMapping
    public ApiResponse<RuleCategoryVO> create(@PathVariable Long classId,
            @RequestBody RuleCategoryCreateRequest request) {
        return ApiResponse.ok(ruleCategoryService.create(classId, request));
    }

    @DeleteMapping("/{categoryId}")
    public ApiResponse<Void> delete(@PathVariable Long classId, @PathVariable Long categoryId) {
        ruleCategoryService.delete(classId, categoryId);
        return ApiResponse.ok(null);
    }
}
