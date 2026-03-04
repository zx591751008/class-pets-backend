package com.classpets.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("student_inventory")
public class StudentInventory {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long classId;
    private Long studentId;
    private String itemCode;
    private String itemName;
    private String rarity;
    private Integer quantity;
    private String status;
    private Long createTime;
    private Long updateTime;
}
