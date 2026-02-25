package com.classpets.backend.group.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class GroupScoreRequest {
    @NotNull(message = "分值不能为空")
    private Integer points;
    private String reason;
}
