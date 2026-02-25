package com.classpets.backend.entity;

import com.baomidou.mybatisplus.annotation.*;

@TableName("redemption_record")
public class RedemptionRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long classId;

    private Long studentId;

    private Long itemId;

    /**
     * Snapshot of the item name at time of redemption
     */
    private String itemName;

    /**
     * Points spent
     */
    private Integer cost;

    /**
     * PENDING, COMPLETED, REFUNDED
     */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private Long createTime;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Integer getCost() {
        return cost;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }
}
