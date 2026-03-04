package com.classpets.backend.student.dto;

import javax.validation.constraints.NotNull;

public class StudentSpendRedeemRequest {
    @NotNull
    private Integer points;

    private String reason;

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
