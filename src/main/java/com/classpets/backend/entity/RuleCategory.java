package com.classpets.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("rule_category")
public class RuleCategory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long classId;

    private String name;

    private Integer targetType;

    private Integer sort;

    private Integer enabled;

    private LocalDateTime createdAt;
}
