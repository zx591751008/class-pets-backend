package com.classpets.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("inventory_use_record")
public class InventoryUseRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long classId;
    private Long studentId;
    private Long inventoryId;
    private String itemCode;
    private String itemName;
    private Long targetEventId;
    private String note;
    private Long createTime;
}
