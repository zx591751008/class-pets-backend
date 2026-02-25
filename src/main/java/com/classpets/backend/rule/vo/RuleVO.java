package com.classpets.backend.rule.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RuleVO {
    private Long id;
    private String content;
    private Integer points;
    private String type;
    private Integer targetType; // 0: Student, 1: Group
    private String category;
    private Long categoryId;
    private Integer enabled;
    private BigDecimal cooldownHours;
    private Integer stackable;
}
