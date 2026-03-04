package com.classpets.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("lottery_draw_record")
public class LotteryDrawRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long classId;
    private Long studentId;
    private String prizeCode;
    private String prizeName;
    private String rarity;
    private Integer costRedeem;
    private Integer rewardRedeem;
    private String inventoryItemCode;
    private String inventoryItemName;
    private String note;
    private Long createTime;
}
