package com.classpets.backend.controller;

import com.classpets.backend.auth.dto.LoginRequestDTO;
import com.classpets.backend.auth.dto.RegisterRequestDTO;
import com.classpets.backend.auth.dto.ScreenLockPasswordRequestDTO;
import com.classpets.backend.auth.dto.ScreenLockVerifyRequestDTO;
import com.classpets.backend.auth.service.AuthService;
import com.classpets.backend.auth.vo.LoginVO;
import com.classpets.backend.auth.vo.TeacherVO;
import com.classpets.backend.common.ApiResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<LoginVO> register(@Validated @RequestBody RegisterRequestDTO request,
            @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        return ApiResponse.ok(authService.register(request, userAgent));
    }

    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@Validated @RequestBody LoginRequestDTO request,
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            HttpServletRequest servletRequest) {
        return ApiResponse.ok(authService.login(request, userAgent, resolveClientIp(servletRequest)));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = extractBearerToken(authorization);
        authService.logoutByToken(token);
        return ApiResponse.ok();
    }

    @GetMapping("/me")
    public ApiResponse<TeacherVO> me() {
        return ApiResponse.ok(authService.me());
    }

    @PostMapping("/screen-lock/password")
    public ApiResponse<Void> setScreenLockPassword(@Validated @RequestBody ScreenLockPasswordRequestDTO request) {
        authService.setScreenLockPassword(request);
        return ApiResponse.ok();
    }

    @PostMapping("/screen-lock/verify")
    public ApiResponse<Void> verifyScreenLock(@Validated @RequestBody ScreenLockVerifyRequestDTO request) {
        authService.verifyScreenLockPassword(request);
        return ApiResponse.ok();
    }

    private String extractBearerToken(String authorization) {
        if (authorization == null) {
            return null;
        }
        String value = authorization.trim();
        if (value.startsWith("Bearer ") && value.length() > 7) {
            return value.substring(7).trim();
        }
        return null;
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.trim().isEmpty()) {
            String[] ips = forwardedFor.split(",");
            if (ips.length > 0) {
                String firstIp = ips[0].trim();
                if (!firstIp.isEmpty() && !"unknown".equalsIgnoreCase(firstIp)) {
                    return firstIp;
                }
            }
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.trim().isEmpty() && !"unknown".equalsIgnoreCase(realIp.trim())) {
            return realIp.trim();
        }

        String remoteAddr = request.getRemoteAddr();
        if (remoteAddr == null || remoteAddr.trim().isEmpty()) {
            return "unknown";
        }
        return remoteAddr.trim();
    }
}
