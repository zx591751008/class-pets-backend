package com.classpets.backend.lottery.dto;

import java.util.List;

public class LotteryPrizeConfigUpdateRequest {

    private List<LotteryPrizeConfigItemDTO> items;

    public List<LotteryPrizeConfigItemDTO> getItems() {
        return items;
    }

    public void setItems(List<LotteryPrizeConfigItemDTO> items) {
        this.items = items;
    }
}
