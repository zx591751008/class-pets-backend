package com.classpets.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("student_event")
public class StudentEvent {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long classId;

    private Long studentId;

    private String reason;

    private Integer changeValue;

    private Integer redeemChange;

    private Integer expChange;

    private Long ruleId;

    private String note;

    private Long timestamp;

    private Integer revoked; // 1 = revoked, 0 = active

    private LocalDateTime createdAt;
}
