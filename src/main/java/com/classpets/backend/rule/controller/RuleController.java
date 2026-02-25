package com.classpets.backend.rule.controller;

import com.classpets.backend.common.ApiResponse;
import com.classpets.backend.rule.dto.RuleUpsertRequest;
import com.classpets.backend.rule.service.RuleService;
import com.classpets.backend.rule.vo.RuleVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/classes/{classId}/rules")
public class RuleController {

    private final RuleService ruleService;

    public RuleController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    @GetMapping
    public ApiResponse<List<RuleVO>> list(@PathVariable Long classId) {
        return ApiResponse.ok(ruleService.listByClass(classId));
    }

    @PostMapping
    public ApiResponse<RuleVO> create(@PathVariable Long classId, @RequestBody RuleUpsertRequest request) {
        return ApiResponse.ok(ruleService.create(classId, request));
    }

    @PutMapping("/{ruleId}")
    public ApiResponse<RuleVO> update(@PathVariable Long classId, @PathVariable Long ruleId,
            @RequestBody RuleUpsertRequest request) {
        return ApiResponse.ok(ruleService.update(ruleId, request));
    }

    @DeleteMapping("/{ruleId}")
    public ApiResponse<Void> delete(@PathVariable Long classId, @PathVariable Long ruleId) {
        ruleService.delete(ruleId);
        return ApiResponse.ok();
    }

    @PostMapping("/{ruleId}/toggle")
    public ApiResponse<RuleVO> toggle(@PathVariable Long classId, @PathVariable Long ruleId) {
        return ApiResponse.ok(ruleService.toggleEnabled(ruleId));
    }
}
