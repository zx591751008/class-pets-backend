package com.classpets.backend.auth.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.nio.charset.StandardCharsets;

import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.concurrent.TimeUnit;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classpets.backend.auth.util.DeviceUtil;
import com.classpets.backend.entity.Teacher;
import com.classpets.backend.entity.TeacherDeviceToken;
import com.classpets.backend.mapper.TeacherMapper;
import com.classpets.backend.mapper.TeacherDeviceTokenMapper;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TeacherDeviceTokenMapper teacherDeviceTokenMapper;
    private final TeacherMapper teacherMapper;
    private final StringRedisTemplate redisTemplate;

    public JwtAuthenticationFilter(JwtUtil jwtUtil,
            TeacherDeviceTokenMapper teacherDeviceTokenMapper,
            TeacherMapper teacherMapper,
            StringRedisTemplate redisTemplate) {
        this.jwtUtil = jwtUtil;
        this.teacherDeviceTokenMapper = teacherDeviceTokenMapper;
        this.teacherMapper = teacherMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            // 1. Redis Check (Inactivity Timeout)
            try {
                String redisKey = "login:token:" + token;
                Boolean hasKey = redisTemplate.hasKey(redisKey);

                if (Boolean.FALSE.equals(hasKey)) {
                    // Token expired in Redis (Inactivity)
                    writeUnauthorized(response, 40101, "登录已过期，请重新登录");
                    return;
                }

                // Renew Redis Expiration (Sliding Window)
                redisTemplate.expire(redisKey, 4, TimeUnit.HOURS);
            } catch (Exception e) {
                // Redis is optional for local dev / reliability
                System.err.println("Redis Unavailable: Skipping session check (" + e.getMessage() + ")");
            }

            String username = jwtUtil.getUsernameFromToken(token);
            Long teacherId = jwtUtil.getTeacherIdFromToken(token);

            // 2. DB Check (Concurrency Control)
            String userAgent = request.getHeader("User-Agent");
            String deviceType = DeviceUtil.getDeviceType(userAgent);

            Long count = teacherDeviceTokenMapper.selectCount(new LambdaQueryWrapper<TeacherDeviceToken>()
                    .eq(TeacherDeviceToken::getTeacherId, teacherId)
                    .eq(TeacherDeviceToken::getDeviceType, deviceType)
                    .eq(TeacherDeviceToken::getToken, token));

            if (count != null && count > 0) {
                Teacher teacher = teacherMapper.selectById(teacherId);
                if (teacher == null) {
                    writeUnauthorized(response, 40101, "用户不存在");
                    return;
                }
                if (teacher.getLicenseExpiresAt() != null && !java.time.LocalDateTime.now().isBefore(teacher.getLicenseExpiresAt())) {
                    writeUnauthorized(response, 40103, "有效期已到，请联系管理员续期");
                    return;
                }

                // Create authentication with teacherId as principal details
                TeacherPrincipal principal = new TeacherPrincipal(teacherId, username);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_TEACHER")));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                writeUnauthorized(response, 40102, "您的账号已在另一台同类型设备登录，当前设备已下线");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void writeUnauthorized(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + code + ",\"message\":\"" + message + "\"}");
    }
}
