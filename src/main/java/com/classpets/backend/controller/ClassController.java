package com.classpets.backend.controller;

import com.classpets.backend.classinfo.dto.ClassCreateRequest;
import com.classpets.backend.classinfo.service.ClassInfoService;
import com.classpets.backend.classinfo.vo.ClassVO;
import com.classpets.backend.common.ApiResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/classes")
public class ClassController {

    private final ClassInfoService classInfoService;

    public ClassController(ClassInfoService classInfoService) {
        this.classInfoService = classInfoService;
    }

    @GetMapping
    public ApiResponse<List<ClassVO>> list() {
        return ApiResponse.ok(classInfoService.list());
    }

    @PostMapping
    public ApiResponse<ClassVO> create(@Validated @RequestBody ClassCreateRequest request) {
        return ApiResponse.ok(classInfoService.create(request));
    }

    @PutMapping("/{classId}")
    public ApiResponse<ClassVO> update(@PathVariable Long classId, @Validated @RequestBody ClassCreateRequest request) {
        return ApiResponse.ok(classInfoService.update(classId, request));
    }

    @DeleteMapping("/{classId}")
    public ApiResponse<Void> delete(@PathVariable Long classId) {
        classInfoService.delete(classId);
        return ApiResponse.ok();
    }

    @PostMapping("/{classId}/scores/reset")
    public ApiResponse<Void> resetScores(@PathVariable Long classId) {
        classInfoService.resetScores(classId);
        return ApiResponse.ok();
    }
}
