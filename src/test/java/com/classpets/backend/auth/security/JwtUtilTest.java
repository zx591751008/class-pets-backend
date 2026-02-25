package com.classpets.backend.auth.security;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtUtilTest {

    @Test
    void generateAndParseTokenShouldWorkInNonProd() {
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[0]);

        JwtUtil jwtUtil = new JwtUtil(environment);
        ReflectionTestUtils.setField(jwtUtil, "secret", "01234567890123456789012345678901");
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 60000L);
        jwtUtil.init();

        String token = jwtUtil.generateToken(123L, "teacher_a");

        assertTrue(jwtUtil.validateToken(token));
        assertEquals("teacher_a", jwtUtil.getUsernameFromToken(token));
        assertEquals(123L, jwtUtil.getTeacherIdFromToken(token));
    }

    @Test
    void shouldFailInProdWhenSecretMissing() {
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[] { "prod" });

        JwtUtil jwtUtil = new JwtUtil(environment);
        ReflectionTestUtils.setField(jwtUtil, "secret", "");

        IllegalStateException ex = assertThrows(IllegalStateException.class, jwtUtil::init);
        assertTrue(ex.getMessage().contains("jwt.secret is required in production"));
    }
}
