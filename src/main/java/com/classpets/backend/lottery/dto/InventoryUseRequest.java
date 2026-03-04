package com.classpets.backend.lottery.dto;

public class InventoryUseRequest {
    private Long targetEventId;
    private String note;

    public Long getTargetEventId() {
        return targetEventId;
    }

    public void setTargetEventId(Long targetEventId) {
        this.targetEventId = targetEventId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
