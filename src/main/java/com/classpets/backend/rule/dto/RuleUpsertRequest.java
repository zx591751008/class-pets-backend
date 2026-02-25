package com.classpets.backend.rule.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RuleUpsertRequest {
    private String content;
    private Integer points;
    private String type; // "add" or "deduct"
    private Integer targetType; // 0: Student, 1: Group
    private String category;
    private Long categoryId;
    private BigDecimal cooldownHours;
    private Integer stackable;
}
