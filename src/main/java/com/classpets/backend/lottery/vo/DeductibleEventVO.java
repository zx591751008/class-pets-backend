package com.classpets.backend.lottery.vo;

import lombok.Data;

@Data
public class DeductibleEventVO {
    private Long id;
    private String reason;
    private Integer changeValue;
    private Long timestamp;
}
