package com.classpets.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("group_info")
public class GroupInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long classId; // maps to class_id via underscore-to-camel
    private String name;
    private String icon;
    private String note;
    private Integer points;
    private java.time.LocalDateTime createdAt;
}
