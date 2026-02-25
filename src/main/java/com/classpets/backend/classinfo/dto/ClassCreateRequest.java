package com.classpets.backend.classinfo.dto;

import javax.validation.constraints.NotBlank;

public class ClassCreateRequest {
    @NotBlank
    private String name;
    private String teacherName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }
}
