package com.classpets.backend.lottery.vo;

import lombok.Data;

@Data
public class LotteryDrawResultVO {
    private Long drawId;
    private String prizeCode;
    private String prizeName;
    private String rarity;
    private String icon;
    private Integer costRedeem;
    private Integer rewardRedeem;
    private boolean addedToInventory;
    private Long inventoryId;
    private String inventoryItemCode;
    private String inventoryItemName;
    private Integer studentRedeem;
}
