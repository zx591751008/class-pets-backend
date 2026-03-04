package com.classpets.backend.lottery.dto;

import javax.validation.constraints.NotNull;

public class LotteryDrawRequest {
    @NotNull
    private Long studentId;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }
}
