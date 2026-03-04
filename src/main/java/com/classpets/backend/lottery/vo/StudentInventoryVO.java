package com.classpets.backend.lottery.vo;

import lombok.Data;

@Data
public class StudentInventoryVO {
    private Long id;
    private Long studentId;
    private String itemCode;
    private String itemName;
    private String rarity;
    private Integer quantity;
}
