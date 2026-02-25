package com.classpets.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("rule_info")
public class RuleInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long classId;

    private String content;

    private Integer points;

    private String type; // "add" or "deduct"

    private Integer targetType; // 0: Student, 1: Group

    private String category;

    private Long categoryId;

    private Integer enabled; // 1 = enabled, 0 = disabled

    private BigDecimal cooldownHours;

    private Integer stackable; // 1 = can stack, 0 = can't

    private LocalDateTime createdAt;
}
