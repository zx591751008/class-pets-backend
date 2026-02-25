package com.classpets.backend.group.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classpets.backend.classinfo.service.ClassInfoService;
import com.classpets.backend.common.BizException;
import com.classpets.backend.entity.GroupInfo;
import com.classpets.backend.entity.Student;
import com.classpets.backend.group.dto.GroupScoreRequest;
import com.classpets.backend.group.dto.GroupUpsertRequest;
import com.classpets.backend.group.vo.GroupVO;
import com.classpets.backend.mapper.GroupEventMapper;
import com.classpets.backend.mapper.GroupInfoMapper;
import com.classpets.backend.mapper.StudentMapper;
import com.classpets.backend.sync.SyncEventService;
import com.classpets.backend.entity.GroupEvent;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private final GroupInfoMapper groupInfoMapper;
    private final StudentMapper studentMapper;
    private final GroupEventMapper groupEventMapper;
    private final ClassInfoService classInfoService;
    private final SyncEventService syncEventService;

    public GroupService(GroupInfoMapper groupInfoMapper, StudentMapper studentMapper,
            GroupEventMapper groupEventMapper, ClassInfoService classInfoService, SyncEventService syncEventService) {
        this.groupInfoMapper = groupInfoMapper;
        this.studentMapper = studentMapper;
        this.groupEventMapper = groupEventMapper;
        this.classInfoService = classInfoService;
        this.syncEventService = syncEventService;
    }

    public List<GroupVO> listByClass(Long classId) {
        ensureClassOwnership(classId);
        List<GroupInfo> groups = groupInfoMapper.selectList(
                new LambdaQueryWrapper<GroupInfo>()
                        .eq(GroupInfo::getClassId, classId)
                        .orderByDesc(GroupInfo::getPoints));
        return groups.stream().map(g -> toVO(g, classId)).collect(Collectors.toList());
    }

    public GroupVO create(Long classId, GroupUpsertRequest request) {
        ensureClassOwnership(classId);
        GroupInfo group = new GroupInfo();
        group.setClassId(classId);
        group.setName(request.getName().trim());
        group.setIcon(request.getIcon());
        group.setNote(request.getNote());
        group.setPoints(0);
        groupInfoMapper.insert(group);
        syncEventService.publishClassChange(classId, "group_created", group.getId());
        return toVO(group, classId);
    }

    public GroupVO update(Long groupId, GroupUpsertRequest request) {
        GroupInfo group = mustGet(groupId);
        ensureClassOwnership(group.getClassId());
        group.setName(request.getName().trim());
        group.setIcon(request.getIcon());
        group.setNote(request.getNote());
        groupInfoMapper.updateById(group);
        syncEventService.publishClassChange(group.getClassId(), "group_updated", groupId);
        return toVO(group, group.getClassId());
    }

    public void delete(Long groupId) {
        GroupInfo group = mustGet(groupId);
        ensureClassOwnership(group.getClassId());
        // Remove group_id from students in this group
        List<Student> students = studentMapper.selectList(
                new LambdaQueryWrapper<Student>().eq(Student::getGroupId, groupId));
        for (Student s : students) {
            s.setGroupId(null);
            studentMapper.updateById(s);
        }
        groupInfoMapper.deleteById(groupId);
        syncEventService.publishClassChange(group.getClassId(), "group_deleted", groupId);
    }

    public GroupVO addScore(Long groupId, GroupScoreRequest request) {
        GroupInfo group = mustGet(groupId);
        ensureClassOwnership(group.getClassId());
        int newPoints = group.getPoints() + request.getPoints();
        group.setPoints(newPoints);
        groupInfoMapper.updateById(group);

        // Log group_event for history
        GroupEvent event = new GroupEvent();
        event.setClassId(group.getClassId());
        event.setGroupId(groupId);
        event.setReason(request.getReason());
        event.setChangeValue(request.getPoints());
        event.setTimestamp(System.currentTimeMillis());
        event.setRevoked(0);
        groupEventMapper.insert(event);

        syncEventService.publishClassChange(group.getClassId(), "group_score_changed", groupId);

        return toVO(group, group.getClassId());
    }

    public List<GroupEvent> getHistory(Long groupId) {
        GroupInfo group = mustGet(groupId);
        ensureClassOwnership(group.getClassId());
        return groupEventMapper.selectList(
                new LambdaQueryWrapper<GroupEvent>()
                        .eq(GroupEvent::getGroupId, groupId)
                        .orderByDesc(GroupEvent::getTimestamp));
    }

    private GroupVO toVO(GroupInfo group, Long classId) {
        GroupVO vo = new GroupVO();
        vo.setId(group.getId());
        vo.setName(group.getName());
        vo.setIcon(group.getIcon());
        vo.setNote(group.getNote());
        vo.setPoints(group.getPoints());

        // Fetch members
        List<Student> members = studentMapper.selectList(
                new LambdaQueryWrapper<Student>().eq(Student::getGroupId, group.getId()));
        vo.setMembers(members.stream().map(s -> {
            GroupVO.MemberSummary ms = new GroupVO.MemberSummary();
            ms.setId(s.getId());
            ms.setName(s.getName());
            // Truncate avatar to avoid massive payload
            String avatar = s.getAvatarImage();
            ms.setAvatarImage(avatar != null && avatar.length() > 100 ? avatar.substring(0, 100) : avatar);
            return ms;
        }).collect(Collectors.toList()));

        // Calculate sum of member points
        int memberSum = members.stream()
                .mapToInt(s -> s.getTotalPoints() != null ? s.getTotalPoints() : 0)
                .sum();
        vo.setMemberTotalPoints(memberSum);

        return vo;
    }

    private void ensureClassOwnership(Long classId) {
        if (!classInfoService.isOwnedByCurrentTeacher(classId)) {
            throw new BizException(40301, "无权访问此班级");
        }
    }

    private GroupInfo mustGet(Long id) {
        GroupInfo group = groupInfoMapper.selectById(id);
        if (group == null) {
            throw new BizException(40401, "小组不存在");
        }
        return group;
    }
}
