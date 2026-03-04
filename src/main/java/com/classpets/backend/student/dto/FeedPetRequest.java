package com.classpets.backend.student.dto;

import javax.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedPetRequest {

    // CANDY, MEAT, GACHA, AMNESIA
    @NotBlank(message = "物品类型不能为空")
    private String itemType;

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }
}
