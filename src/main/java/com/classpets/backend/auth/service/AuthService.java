package com.classpets.backend.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classpets.backend.auth.dto.LoginRequestDTO;
import com.classpets.backend.auth.dto.RegisterRequestDTO;
import com.classpets.backend.auth.dto.ScreenLockPasswordRequestDTO;
import com.classpets.backend.auth.dto.ScreenLockVerifyRequestDTO;
import com.classpets.backend.auth.security.JwtUtil;
import com.classpets.backend.auth.security.TeacherPrincipal;
import com.classpets.backend.auth.vo.LoginVO;
import com.classpets.backend.auth.vo.TeacherVO;
import com.classpets.backend.common.BizException;
import com.classpets.backend.entity.ActivationCode;
import com.classpets.backend.entity.Teacher;
import com.classpets.backend.mapper.ActivationCodeMapper;
import com.classpets.backend.mapper.TeacherMapper;
import com.classpets.backend.auth.util.DeviceUtil;
import com.classpets.backend.entity.TeacherDeviceToken;
import com.classpets.backend.mapper.TeacherDeviceTokenMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    private static final int LOGIN_FAIL_WINDOW_MINUTES = 10;
    private static final int LOGIN_MAX_FAIL_TIMES = 5;
    private static final int LOGIN_LOCK_MINUTES = 15;

    private final TeacherMapper teacherMapper;
    private final ActivationCodeMapper activationCodeMapper;
    private final TeacherDeviceTokenMapper teacherDeviceTokenMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    public AuthService(TeacherMapper teacherMapper,
            ActivationCodeMapper activationCodeMapper,
            TeacherDeviceTokenMapper teacherDeviceTokenMapper,
            JwtUtil jwtUtil,
            PasswordEncoder passwordEncoder,
            StringRedisTemplate redisTemplate) {
        this.teacherMapper = teacherMapper;
        this.activationCodeMapper = activationCodeMapper;
        this.teacherDeviceTokenMapper = teacherDeviceTokenMapper;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
    }

    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(RegisterRequestDTO request, String userAgent) {
        String username = safeTrim(request.getUsername());
        String password = safeTrim(request.getPassword());
        String activation = safeTrim(request.getActivationCode());

        validateUsername(username);
        if (password.length() < 6) {
            throw new BizException(40001, "密码至少 6 位");
        }

        Teacher existed = teacherMapper.selectOne(new LambdaQueryWrapper<Teacher>()
                .eq(Teacher::getUsername, username)
                .last("limit 1"));
        if (existed != null) {
            throw new BizException(40901, "账号已存在");
        }

        ActivationCode code = activationCodeMapper.selectOne(new LambdaQueryWrapper<ActivationCode>()
                .eq(ActivationCode::getCode, activation)
                .last("limit 1"));
        if (code == null) {
            throw new BizException(41001, "激活码错误");
        }
        if (code.getUsed() != null && code.getUsed() == 1) {
            throw new BizException(41001, "激活码已被使用");
        }

        Teacher teacher = new Teacher();
        teacher.setUsername(username);
        // Use Spring Security's BCryptPasswordEncoder
        teacher.setPasswordHash(passwordEncoder.encode(password));
        teacher.setNickname(emptyToDefault(request.getNickname(), username));
        teacher.setStatus(1);
        teacher.setLicenseExpiresAt(resolveLicenseExpiresAt(code));
        teacherMapper.insert(teacher);

        int affected = activationCodeMapper.markUsedIfUnused(activation, teacher.getId());
        if (affected == 0) {
            ActivationCode latest = activationCodeMapper.selectOne(new LambdaQueryWrapper<ActivationCode>()
                    .eq(ActivationCode::getCode, activation)
                    .last("limit 1"));
            if (latest == null) {
                throw new BizException(41001, "激活码错误");
            }
            if (latest.getUsed() != null && latest.getUsed() == 1) {
                throw new BizException(41001, "激活码已被使用");
            }
            throw new BizException(41001, "激活码验证失败，请稍后重试");
        }

        return buildLoginVO(teacher, userAgent);
    }

    public LoginVO login(LoginRequestDTO request, String userAgent, String clientIp) {
        String username = safeTrim(request.getUsername());
        String password = safeTrim(request.getPassword());

        ensureLoginAllowed(username, clientIp);

        try {
            Teacher teacher = teacherMapper.selectOne(new LambdaQueryWrapper<Teacher>()
                    .eq(Teacher::getUsername, username)
                    .last("limit 1"));

            if (teacher == null || teacher.getPasswordHash() == null) {
                throw new BizException(40101, "账号或密码错误");
            }

            // Use Spring Security's PasswordEncoder for verification
            if (!passwordEncoder.matches(password, teacher.getPasswordHash())) {
                throw new BizException(40101, "账号或密码错误");
            }

            if (teacher.getStatus() != null && teacher.getStatus() == 0) {
                throw new BizException(40301, "账号已禁用");
            }

            if (isLicenseExpired(teacher)) {
                throw new BizException(40301, "有效期已到，请联系管理员续期");
            }

            clearLoginFailures(username, clientIp);
            return buildLoginVO(teacher, userAgent);
        } catch (BizException ex) {
            if (ex.getCode() != 42901) {
                recordLoginFailure(username, clientIp);
            }
            throw ex;
        }
    }

    /**
     * Get current logged-in teacher from SecurityContext
     */
    public TeacherVO me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof TeacherPrincipal)) {
            throw new BizException(40101, "未登录或登录已过期");
        }

        TeacherPrincipal principal = (TeacherPrincipal) authentication.getPrincipal();
        Long teacherId = principal.getTeacherId();

        Teacher teacher = teacherMapper.selectById(teacherId);
        if (teacher == null) {
            throw new BizException(40101, "用户不存在");
        }
        if (isLicenseExpired(teacher)) {
            throw new BizException(40301, "有效期已到，请联系管理员续期");
        }

        return toTeacherVO(teacher);
    }

    /**
     * Utility: Get current teacher ID from SecurityContext (for use in other
     * services)
     */
    public Long getCurrentTeacherId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof TeacherPrincipal) {
            return ((TeacherPrincipal) authentication.getPrincipal()).getTeacherId();
        }
        return null;
    }

    public TeacherVO updateProfile(String nickname, String password, String activationCode) {
        Long teacherId = getCurrentTeacherId();
        if (teacherId == null) {
            throw new BizException(40101, "未登录或登录已过期");
        }

        Teacher teacher = teacherMapper.selectById(teacherId);
        if (teacher == null) {
            throw new BizException(40401, "用户不存在");
        }

        if (nickname != null && !nickname.trim().isEmpty()) {
            teacher.setNickname(nickname.trim());
        }

        if (password != null && !password.trim().isEmpty()) {
            if (password.length() < 6) {
                throw new BizException(40001, "密码至少 6 位");
            }

            // Verify activation code
            if (activationCode == null || activationCode.trim().isEmpty()) {
                throw new BizException(40001, "修改密码需要验证注册码");
            }
            String codeStr = activationCode.trim();
            ActivationCode codeRecord = activationCodeMapper.selectOne(new LambdaQueryWrapper<ActivationCode>()
                    .eq(ActivationCode::getCode, codeStr)
                    .eq(ActivationCode::getUsedBy, teacherId)
                    .last("limit 1"));

            if (codeRecord == null) {
                // Try to find if the code exists but maybe not marked as used by this user
                // (edge case, but strictly should match)
                // Or just say invalid.
                // Let's also check if the code simply exists and was used by this teacher.
                // Using the exact logic: The code MUST exist, be 'used', and 'usedBy' MUST be
                // this teacherId.
                throw new BizException(40301, "注册码验证失败，请确认您使用的是注册时填写的激活码");
            }

            teacher.setPasswordHash(passwordEncoder.encode(password));
        }

        teacherMapper.updateById(teacher);

        return toTeacherVO(teacher);
    }

    public void setScreenLockPassword(ScreenLockPasswordRequestDTO request) {
        Long teacherId = getCurrentTeacherId();
        if (teacherId == null) {
            throw new BizException(40101, "未登录或登录已过期");
        }
        Teacher teacher = teacherMapper.selectById(teacherId);
        if (teacher == null) {
            throw new BizException(40401, "用户不存在");
        }
        String password = safeTrim(request.getPassword());
        if (!password.matches("^\\d{6}$")) {
            throw new BizException(40001, "锁屏密码必须为6位数字");
        }
        teacher.setScreenLockHash(passwordEncoder.encode(password));
        teacher.setScreenLockEnabled(1);
        teacherMapper.updateById(teacher);
    }

    public void verifyScreenLockPassword(ScreenLockVerifyRequestDTO request) {
        Long teacherId = getCurrentTeacherId();
        if (teacherId == null) {
            throw new BizException(40101, "未登录或登录已过期");
        }
        Teacher teacher = teacherMapper.selectById(teacherId);
        if (teacher == null) {
            throw new BizException(40401, "用户不存在");
        }
        if (!Boolean.TRUE.equals(toScreenLockEnabled(teacher.getScreenLockEnabled())) || teacher.getScreenLockHash() == null
                || teacher.getScreenLockHash().trim().isEmpty()) {
            throw new BizException(40001, "请先设置锁屏密码");
        }
        String password = safeTrim(request.getPassword());
        if (!passwordEncoder.matches(password, teacher.getScreenLockHash())) {
            throw new BizException(40001, "锁屏密码错误");
        }
    }

    public void logoutByToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return;
        }
        invalidateToken(token.trim());
    }

    private LoginVO buildLoginVO(Teacher teacher, String userAgent) {
        TeacherVO teacherVO = toTeacherVO(teacher);

        // Generate real JWT token
        String token = jwtUtil.generateToken(teacher.getId(), teacher.getUsername());

        // 1. Redis: Store for inactivity timeout (Sliding Window)
        try {
            String redisKey = "login:token:" + token;
            redisTemplate.opsForValue().set(redisKey, String.valueOf(teacher.getId()), 4, TimeUnit.HOURS);
        } catch (Exception e) {
            System.err.println("Redis Unavailable: Skipping session inactivity setup (" + e.getMessage() + ")");
        }

        // 2. DB: Store for concurrency control (2 PC limit)
        saveDeviceToken(teacher.getId(), token, userAgent);

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setExpiresIn(jwtUtil.getExpirationMs() / 1000); // Convert to seconds for frontend
        vo.setTeacher(teacherVO);
        return vo;
    }

    private void saveDeviceToken(Long teacherId, String token, String userAgent) {
        String deviceType = DeviceUtil.getDeviceType(userAgent);

        List<TeacherDeviceToken> existingTokens = teacherDeviceTokenMapper
                .selectList(new LambdaQueryWrapper<TeacherDeviceToken>()
                        .eq(TeacherDeviceToken::getTeacherId, teacherId)
                        .eq(TeacherDeviceToken::getDeviceType, deviceType));

        int limit = DeviceUtil.DEVICE_PC.equals(deviceType) ? 2 : 1;

        if (existingTokens.size() >= limit) {
            // Sort by last login time ASC (oldest first)
            existingTokens.sort(Comparator.comparing(TeacherDeviceToken::getLastLoginTime));

            // Delete oldest until count is limit - 1
            int toDelete = existingTokens.size() - limit + 1;
            for (int i = 0; i < toDelete; i++) {
                TeacherDeviceToken old = existingTokens.get(i);
                teacherDeviceTokenMapper.deleteById(old.getId());
                deleteRedisSession(old.getToken());
            }
        }

        TeacherDeviceToken newToken = new TeacherDeviceToken();
        newToken.setTeacherId(teacherId);
        newToken.setDeviceType(deviceType);
        newToken.setToken(token);
        newToken.setLastLoginTime(LocalDateTime.now());
        teacherDeviceTokenMapper.insert(newToken);
    }

    private void invalidateToken(String token) {
        teacherDeviceTokenMapper.delete(new LambdaQueryWrapper<TeacherDeviceToken>()
                .eq(TeacherDeviceToken::getToken, token));
        deleteRedisSession(token);
    }

    private void ensureLoginAllowed(String username, String clientIp) {
        String lockKey = buildLoginLockKey(username, clientIp);
        try {
            String lockedValue = redisTemplate.opsForValue().get(lockKey);
            if (lockedValue == null) {
                return;
            }

            Long ttlSeconds = redisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
            if (ttlSeconds == null || ttlSeconds <= 0) {
                throw new BizException(42901, "登录失败次数过多，请稍后再试");
            }
            long remainMinutes = Math.max(1, (long) Math.ceil(ttlSeconds / 60.0));
            throw new BizException(42901, "登录失败次数过多，请" + remainMinutes + "分钟后再试");
        } catch (BizException ex) {
            throw ex;
        } catch (Exception e) {
            System.err.println("Redis Unavailable: Skipping login lock check (" + e.getMessage() + ")");
        }
    }

    private void recordLoginFailure(String username, String clientIp) {
        String failKey = buildLoginFailKey(username, clientIp);
        String lockKey = buildLoginLockKey(username, clientIp);
        try {
            Long count = redisTemplate.opsForValue().increment(failKey);
            redisTemplate.expire(failKey, LOGIN_FAIL_WINDOW_MINUTES, TimeUnit.MINUTES);
            if (count != null && count >= LOGIN_MAX_FAIL_TIMES) {
                redisTemplate.opsForValue().set(lockKey, "1", LOGIN_LOCK_MINUTES, TimeUnit.MINUTES);
                redisTemplate.delete(failKey);
            }
        } catch (Exception e) {
            System.err.println("Redis Unavailable: Skipping login failure recording (" + e.getMessage() + ")");
        }
    }

    private void clearLoginFailures(String username, String clientIp) {
        try {
            redisTemplate.delete(buildLoginFailKey(username, clientIp));
            redisTemplate.delete(buildLoginLockKey(username, clientIp));
        } catch (Exception e) {
            System.err.println("Redis Unavailable: Skipping login failure cleanup (" + e.getMessage() + ")");
        }
    }

    private String buildLoginFailKey(String username, String clientIp) {
        return "login:fail:" + safeKeyPart(username) + ":" + safeKeyPart(clientIp);
    }

    private String buildLoginLockKey(String username, String clientIp) {
        return "login:lock:" + safeKeyPart(username) + ":" + safeKeyPart(clientIp);
    }

    private String safeKeyPart(String value) {
        String source = safeTrim(value);
        if (source.isEmpty()) {
            return "unknown";
        }
        return source.replaceAll("[^A-Za-z0-9._:-]", "_").toLowerCase();
    }

    private void deleteRedisSession(String token) {
        try {
            redisTemplate.delete("login:token:" + token);
        } catch (Exception e) {
            System.err.println("Redis Unavailable: Skipping logout session cleanup (" + e.getMessage() + ")");
        }
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private String emptyToDefault(String value, String defaultValue) {
        String trimmed = safeTrim(value);
        return trimmed.isEmpty() ? defaultValue : trimmed;
    }

    private void validateUsername(String username) {
        if (username.isEmpty()) {
            throw new BizException(40001, "账号不能为空");
        }
        if (!username.matches("^[A-Za-z0-9]{4,32}$")) {
            throw new BizException(40001, "账号需为 4-32 位字母或数字");
        }
    }

    private LocalDateTime resolveLicenseExpiresAt(ActivationCode code) {
        if (code == null || code.getValidDays() == null) {
            return null;
        }
        int validDays = code.getValidDays();
        if (validDays <= 0) {
            return null;
        }
        return LocalDateTime.now().plusDays(validDays);
    }

    private boolean isLicenseExpired(Teacher teacher) {
        if (teacher == null || teacher.getLicenseExpiresAt() == null) {
            return false;
        }
        return !LocalDateTime.now().isBefore(teacher.getLicenseExpiresAt());
    }

    private TeacherVO toTeacherVO(Teacher teacher) {
        TeacherVO vo = new TeacherVO();
        vo.setId(teacher.getId());
        vo.setUsername(teacher.getUsername());
        vo.setNickname(teacher.getNickname());
        vo.setScreenLockEnabled(toScreenLockEnabled(teacher.getScreenLockEnabled()));
        vo.setLicenseExpiresAt(teacher.getLicenseExpiresAt());
        return vo;
    }

    private Boolean toScreenLockEnabled(Integer value) {
        return value != null && value == 1;
    }
}
