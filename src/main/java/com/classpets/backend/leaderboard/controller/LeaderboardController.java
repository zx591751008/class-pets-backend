package com.classpets.backend.leaderboard.controller;

import com.classpets.backend.common.ApiResponse;
import com.classpets.backend.leaderboard.service.LeaderboardService;
import com.classpets.backend.leaderboard.vo.RankVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/classes/{classId}/leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping
    public ApiResponse<List<RankVO>> getRank(
            @PathVariable Long classId,
            @RequestParam(defaultValue = "TOTAL") String type,
            @RequestParam(required = false) String category) {
        return ApiResponse.ok(leaderboardService.getRank(classId, type, category));
    }

    @GetMapping("/groups")
    public ApiResponse<List<RankVO>> getGroupRank(
            @PathVariable Long classId,
            @RequestParam(defaultValue = "TOTAL") String type) {
        return ApiResponse.ok(leaderboardService.getGroupRank(classId, type));
    }
}
