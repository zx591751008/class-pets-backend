package com.classpets.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("group_event")
public class GroupEvent {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long classId;
    private Long groupId;
    private String reason;
    private Integer changeValue;
    private String note;
    private Long timestamp;
    private Integer revoked;
    private java.time.LocalDateTime createdAt;
}
