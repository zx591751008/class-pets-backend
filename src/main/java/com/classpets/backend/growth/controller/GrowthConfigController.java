package com.classpets.backend.growth.controller;

import com.classpets.backend.common.ApiResponse;
import com.classpets.backend.growth.dto.GrowthConfigUpdateRequest;
import com.classpets.backend.growth.service.GrowthConfigService;
import com.classpets.backend.growth.vo.GrowthConfigVO;
import com.classpets.backend.student.service.StudentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/classes/{classId}/growth")
public class GrowthConfigController {

    private final GrowthConfigService growthConfigService;
    private final StudentService studentService;

    public GrowthConfigController(GrowthConfigService growthConfigService, StudentService studentService) {
        this.growthConfigService = growthConfigService;
        this.studentService = studentService;
    }

    @GetMapping("/config")
    public ApiResponse<GrowthConfigVO> getConfig(@PathVariable Long classId) {
        return ApiResponse.ok(growthConfigService.getForClass(classId));
    }

    @PutMapping("/config")
    public ApiResponse<GrowthConfigVO> updateConfig(@PathVariable Long classId,
            @RequestBody GrowthConfigUpdateRequest request) {
        GrowthConfigVO config = growthConfigService.updateForClass(classId, request);
        if (request == null || request.getRecalculate() == null || request.getRecalculate()) {
            studentService.recalculateGrowthByClass(classId);
        }
        return ApiResponse.ok(config);
    }

    @PostMapping("/recalculate")
    public ApiResponse<Void> recalculate(@PathVariable Long classId) {
        studentService.recalculateGrowthByClass(classId);
        return ApiResponse.ok();
    }
}
