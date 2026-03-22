package com.classpets.backend.lottery.dto;

import java.util.List;

public class LotteryPrizeConfigUpdateRequest {

    private List<LotteryPrizeConfigItemDTO> items;
    private Integer singleDrawCost;

    public List<LotteryPrizeConfigItemDTO> getItems() {
        return items;
    }

    public void setItems(List<LotteryPrizeConfigItemDTO> items) {
        this.items = items;
    }

    public Integer getSingleDrawCost() {
        return singleDrawCost;
    }

    public void setSingleDrawCost(Integer singleDrawCost) {
        this.singleDrawCost = singleDrawCost;
    }
}
