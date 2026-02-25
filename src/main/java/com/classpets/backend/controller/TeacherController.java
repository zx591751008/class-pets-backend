package com.classpets.backend.controller;

import com.classpets.backend.auth.service.AuthService;
import com.classpets.backend.auth.vo.TeacherVO;
import com.classpets.backend.common.ApiResponse;
import com.classpets.backend.teacher.dto.TeacherProfileUpdateRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/teacher")
public class TeacherController {

    private final AuthService authService;

    public TeacherController(AuthService authService) {
        this.authService = authService;
    }

    @PutMapping("/profile")
    public ApiResponse<TeacherVO> updateProfile(@Validated @RequestBody TeacherProfileUpdateRequest request) {
        return ApiResponse.ok(
                authService.updateProfile(request.getNickname(), request.getPassword(), request.getActivationCode()));
    }
}
