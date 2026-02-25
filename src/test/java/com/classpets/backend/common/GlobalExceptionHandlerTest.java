package com.classpets.backend.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBizShouldPreserveBizCodeAndMessage() {
        ApiResponse<Void> response = handler.handleBiz(new BizException(40901, "账号已存在"));

        assertEquals(40901, response.getCode());
        assertEquals("账号已存在", response.getMessage());
    }

    @Test
    void handleSystemShouldReturnGenericMessage() {
        ApiResponse<Void> response = handler.handleSystem(new RuntimeException("db timeout"));

        assertEquals(50001, response.getCode());
        assertEquals("系统异常", response.getMessage());
    }
}
