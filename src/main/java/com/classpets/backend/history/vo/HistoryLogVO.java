package com.classpets.backend.history.vo;

import lombok.Data;

@Data
public class HistoryLogVO {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentAvatar; // Assuming avatar logic exists, or use default
    private Long groupId;
    private String groupName;
    private String reason;
    private Integer changeValue;
    private Integer redeemChange;
    private Integer expChange;
    private Long timestamp;
    private Integer revoked;
    private Boolean undoable;
}
