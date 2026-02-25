package com.classpets.backend.controller;

import com.classpets.backend.common.ApiResponse;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HealthControllerTest {

    @Test
    void healthShouldReturnUpStatus() {
        HealthController controller = new HealthController();

        ApiResponse<Map<String, String>> response = controller.health();

        assertEquals(0, response.getCode());
        assertEquals("UP", response.getData().get("status"));
    }
}
