package com.classpets.backend.student.dto;

import lombok.Data;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class BatchGroupRequest {
    @NotEmpty(message = "学生列表不能为空")
    private List<Long> studentIds;
    private Long groupId; // Target group ID (null means unassign)
}
