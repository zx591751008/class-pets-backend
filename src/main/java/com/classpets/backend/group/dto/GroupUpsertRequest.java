package com.classpets.backend.group.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class GroupUpsertRequest {
    @NotBlank(message = "小组名称不能为空")
    private String name;
    private String icon;
    private String note;
}
