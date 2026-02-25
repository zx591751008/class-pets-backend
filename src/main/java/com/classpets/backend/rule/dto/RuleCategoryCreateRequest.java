package com.classpets.backend.rule.dto;

import lombok.Data;

@Data
public class RuleCategoryCreateRequest {
    private String name;
    private Integer targetType;
}
