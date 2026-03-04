package com.classpets.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("abandoned_pet")
public class AbandonedPet {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long classId;
    private Long studentId;
    private String petId;
    private Long abandonedAt;

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

    public String getPetId() {
        return petId;
    }

    public void setPetId(String petId) {
        this.petId = petId;
    }

    public Long getAbandonedAt() {
        return abandonedAt;
    }

    public void setAbandonedAt(Long abandonedAt) {
        this.abandonedAt = abandonedAt;
    }
}
