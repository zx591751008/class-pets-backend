package com.classpets.backend.rule.vo;

import lombok.Data;

@Data
public class RuleCategoryVO {
    private Long id;
    private String name;
    private Integer targetType;
    private Integer sort;
    private Integer enabled;
}
