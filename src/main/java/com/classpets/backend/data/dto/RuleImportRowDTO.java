package com.classpets.backend.data.dto;

public class RuleImportRowDTO {
    private Integer lineNo;
    private String content;
    private String points;
    private String type;
    private String targetType;
    private String category;
    private String cooldownHours;
    private String stackable;
    private String error;

    public Integer getLineNo() {
        return lineNo;
    }

    public void setLineNo(Integer lineNo) {
        this.lineNo = lineNo;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCooldownHours() {
        return cooldownHours;
    }

    public void setCooldownHours(String cooldownHours) {
        this.cooldownHours = cooldownHours;
    }

    public String getStackable() {
        return stackable;
    }

    public void setStackable(String stackable) {
        this.stackable = stackable;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
