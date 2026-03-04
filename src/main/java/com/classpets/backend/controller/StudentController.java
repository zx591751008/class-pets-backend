package com.classpets.backend.controller;

import com.classpets.backend.common.ApiResponse;
import com.classpets.backend.student.dto.StudentScoreRequest;
import com.classpets.backend.student.dto.StudentSpendRedeemRequest;
import com.classpets.backend.student.dto.StudentUpsertRequest;
import com.classpets.backend.student.dto.BatchScoreRequest;
import com.classpets.backend.student.vo.BatchScoreResultVO;
import com.classpets.backend.student.service.StudentService;
import com.classpets.backend.student.vo.StudentVO;
import com.classpets.backend.student.vo.StudentPetGalleryVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.classpets.backend.student.dto.FeedPetRequest;

@RestController
@RequestMapping("/api/v1")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/classes/{classId}/students")
    public ApiResponse<List<StudentVO>> list(@PathVariable Long classId) {
        return ApiResponse.ok(studentService.listByClass(classId));
    }

    @PostMapping("/classes/{classId}/students")
    public ApiResponse<StudentVO> create(@PathVariable Long classId,
            @Validated @RequestBody StudentUpsertRequest request) {
        return ApiResponse.ok(studentService.create(classId, request));
    }

    @PutMapping("/students/{studentId}")
    public ApiResponse<StudentVO> update(@PathVariable Long studentId,
            @Validated @RequestBody StudentUpsertRequest request) {
        return ApiResponse.ok(studentService.update(studentId, request));
    }

    @DeleteMapping("/students/{studentId}")
    public ApiResponse<Void> delete(@PathVariable Long studentId) {
        studentService.delete(studentId);
        return ApiResponse.ok();
    }

    @PostMapping("/students/{studentId}/score")
    public ApiResponse<StudentVO> score(@PathVariable Long studentId,
            @Validated @RequestBody StudentScoreRequest request) {
        return ApiResponse.ok(studentService.score(studentId, request));
    }

    @PostMapping("/students/{studentId}/redeem/spend")
    public ApiResponse<StudentVO> spendRedeem(@PathVariable Long studentId,
            @Validated @RequestBody StudentSpendRedeemRequest request) {
        return ApiResponse.ok(studentService.spendRedeem(studentId, request));
    }

    @PostMapping("/classes/{classId}/students/batch-score")
    public ApiResponse<BatchScoreResultVO> batchScore(@PathVariable Long classId,
            @Validated @RequestBody BatchScoreRequest request) {
        return ApiResponse.ok(studentService.batchScore(classId, request));
    }

    @PutMapping("/classes/{classId}/students/batch-group")
    public ApiResponse<Void> batchGroup(@PathVariable Long classId,
            @Validated @RequestBody com.classpets.backend.student.dto.BatchGroupRequest request) {
        studentService.batchUpdateGroup(classId, request);
        return ApiResponse.ok();
    }

    @PostMapping("/classes/{classId}/students/reset")
    public ApiResponse<Map<String, Object>> resetStudents(@PathVariable Long classId) {
        int deletedCount = studentService.clearByClass(classId);
        Map<String, Object> data = new HashMap<>();
        data.put("deletedCount", deletedCount);
        return ApiResponse.ok(data);
    }

    @GetMapping("/students/{studentId}/abandoned-pets")
    public ApiResponse<List<String>> getAbandonedPets(@PathVariable Long studentId) {
        return ApiResponse.ok(studentService.getAbandonedPetIds(studentId));
    }

    @PostMapping("/students/{studentId}/abandon-pet")
    public ApiResponse<Void> abandonPet(@PathVariable Long studentId, @RequestBody Map<String, String> request) {
        Long classId = Long.parseLong(request.get("classId"));
        String petId = request.get("petId");
        studentService.abandonPet(studentId, classId, petId);
        return ApiResponse.ok();
    }

    @PostMapping("/students/{studentId}/adopt")
    public ApiResponse<StudentVO> adoptPet(@PathVariable Long studentId, @RequestBody Map<String, String> request) {
        return ApiResponse.ok(studentService.adoptPet(studentId, request.get("petId")));
    }

    @PostMapping("/students/{studentId}/feed")
    public ApiResponse<Map<String, Object>> feedPet(@PathVariable Long studentId,
            @Validated @RequestBody FeedPetRequest request) {
        return ApiResponse.ok(studentService.feedPet(studentId, request));
    }

    @GetMapping("/classes/{classId}/students/{studentId}/gallery")
    public ApiResponse<List<StudentPetGalleryVO>> getGallery(@PathVariable Long classId, @PathVariable Long studentId) {
        return ApiResponse.ok(studentService.getStudentGallery(classId, studentId));
    }
}
