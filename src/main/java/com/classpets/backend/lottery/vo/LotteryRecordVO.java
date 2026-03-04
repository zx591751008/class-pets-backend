package com.classpets.backend.lottery.vo;

import lombok.Data;

@Data
public class LotteryRecordVO {
    private Long id;
    private Long studentId;
    private String studentName;
    private String prizeCode;
    private String prizeName;
    private String rarity;
    private Integer costRedeem;
    private Integer rewardRedeem;
    private Long timestamp;
}
