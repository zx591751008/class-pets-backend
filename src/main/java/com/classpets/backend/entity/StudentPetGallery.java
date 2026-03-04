package com.classpets.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("student_pet_gallery")
public class StudentPetGallery {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long classId;
    private Long studentId;
    private String petRouteId;
    private String petName;
    private Long unlockTime;
}
