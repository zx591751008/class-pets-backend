package com.classpets.backend.history.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classpets.backend.classinfo.service.ClassInfoService;
import com.classpets.backend.common.ApiResponse;
import com.classpets.backend.common.BizException;
import com.classpets.backend.entity.StudentEvent;
import com.classpets.backend.mapper.StudentEventMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.classpets.backend.history.vo.HistoryLogVO;
import com.classpets.backend.history.service.HistoryUndoService;
import com.classpets.backend.entity.Student;
import com.classpets.backend.entity.GroupInfo;
import com.classpets.backend.mapper.StudentMapper;
import com.classpets.backend.mapper.GroupMapper;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/classes/{classId}/history")
public class HistoryController {

    private final StudentEventMapper studentEventMapper;
    private final ClassInfoService classInfoService;
    private final StudentMapper studentMapper;
    private final GroupMapper groupMapper;
    private final HistoryUndoService historyUndoService;

    public HistoryController(StudentEventMapper studentEventMapper, ClassInfoService classInfoService,
            StudentMapper studentMapper, GroupMapper groupMapper, HistoryUndoService historyUndoService) {
        this.studentEventMapper = studentEventMapper;
        this.classInfoService = classInfoService;
        this.studentMapper = studentMapper;
        this.groupMapper = groupMapper;
        this.historyUndoService = historyUndoService;
    }

    @GetMapping
    public ApiResponse<List<HistoryLogVO>> getHistory(
            @PathVariable Long classId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String reason) {

        if (!classInfoService.isOwnedByCurrentTeacher(classId)) {
            throw new BizException(40301, "无权访问此班级");
        }

        LambdaQueryWrapper<StudentEvent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentEvent::getClassId, classId);
        wrapper.eq(StudentEvent::getRevoked, 0);

        if (studentId != null) {
            wrapper.eq(StudentEvent::getStudentId, studentId);
        }
        if (reason != null && !reason.isEmpty()) {
            wrapper.like(StudentEvent::getReason, reason);
        }

        wrapper.orderByDesc(StudentEvent::getTimestamp);
        wrapper.last("LIMIT 100");

        List<StudentEvent> events = studentEventMapper.selectList(wrapper);

        // Fetch related data
        List<Long> studentIds = events.stream().map(StudentEvent::getStudentId).distinct().collect(Collectors.toList());
        List<Student> students = studentIds.isEmpty() ? new ArrayList<>() : studentMapper.selectBatchIds(studentIds);
        Map<Long, Student> studentMap = students.stream().collect(Collectors.toMap(Student::getId, s -> s));

        List<Long> groupIds = students.stream().filter(s -> s.getGroupId() != null).map(Student::getGroupId).distinct()
                .collect(Collectors.toList());
        List<GroupInfo> groups = groupIds.isEmpty() ? new ArrayList<>() : groupMapper.selectBatchIds(groupIds);
        Map<Long, GroupInfo> groupMap = groups.stream().collect(Collectors.toMap(GroupInfo::getId, g -> g));

        // Convert to VO
        List<HistoryLogVO> logs = events.stream().map(event -> {
            HistoryLogVO vo = new HistoryLogVO();
            vo.setId(event.getId());
            vo.setStudentId(event.getStudentId());
            vo.setReason(event.getReason());
            vo.setChangeValue(event.getChangeValue());
            vo.setRedeemChange(event.getRedeemChange());
            vo.setTimestamp(event.getTimestamp());

            Student s = studentMap.get(event.getStudentId());
            if (s != null) {
                vo.setStudentName(s.getName());
                // vo.setStudentAvatar(s.getAvatar()); // If avatar exists
                if (s.getGroupId() != null) {
                    vo.setGroupId(s.getGroupId());
                    GroupInfo g = groupMap.get(s.getGroupId());
                    if (g != null) {
                        vo.setGroupName(g.getName());
                    }
                }
            } else {
                vo.setStudentName("未知学生");
            }
            return vo;
        }).collect(Collectors.toList());

        return ApiResponse.ok(logs);
    }

    @PostMapping("/{eventId}/undo")
    public ApiResponse<Void> undoHistory(@PathVariable Long classId, @PathVariable Long eventId) {
        historyUndoService.undoByEvent(classId, eventId);
        return ApiResponse.ok();
    }
}
