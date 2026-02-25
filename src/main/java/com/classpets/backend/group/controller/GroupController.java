package com.classpets.backend.group.controller;

import com.classpets.backend.common.ApiResponse;
import com.classpets.backend.group.dto.GroupScoreRequest;
import com.classpets.backend.group.dto.GroupUpsertRequest;
import com.classpets.backend.group.service.GroupService;
import com.classpets.backend.group.vo.GroupVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping("/classes/{classId}/groups")
    public ApiResponse<List<GroupVO>> list(@PathVariable Long classId) {
        return ApiResponse.ok(groupService.listByClass(classId));
    }

    @PostMapping("/classes/{classId}/groups")
    public ApiResponse<GroupVO> create(@PathVariable Long classId, @Validated @RequestBody GroupUpsertRequest request) {
        return ApiResponse.ok(groupService.create(classId, request));
    }

    @PutMapping("/groups/{groupId}")
    public ApiResponse<GroupVO> update(@PathVariable Long groupId, @Validated @RequestBody GroupUpsertRequest request) {
        return ApiResponse.ok(groupService.update(groupId, request));
    }

    @DeleteMapping("/groups/{groupId}")
    public ApiResponse<Void> delete(@PathVariable Long groupId) {
        groupService.delete(groupId);
        return ApiResponse.ok();
    }

    @PostMapping("/groups/{groupId}/score")
    public ApiResponse<GroupVO> addScore(@PathVariable Long groupId,
            @Validated @RequestBody GroupScoreRequest request) {
        return ApiResponse.ok(groupService.addScore(groupId, request));
    }

    @GetMapping("/groups/{groupId}/history")
    public ApiResponse<?> getHistory(@PathVariable Long groupId) {
        return ApiResponse.ok(groupService.getHistory(groupId));
    }
}
