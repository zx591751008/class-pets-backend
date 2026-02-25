package com.classpets.backend.analytics.controller;

import com.classpets.backend.analytics.service.AnalyticsService;
import com.classpets.backend.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/classes/{classId}/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/trend")
    public ApiResponse<Map<String, Object>> getTrend(@PathVariable Long classId) {
        return ApiResponse.ok(analyticsService.getClassTrend(classId));
    }
}
