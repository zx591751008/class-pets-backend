package com.classpets.backend.lottery.vo;

public class LotteryPrizeVO {

    private String code;
    private String name;
    private String rarity;
    private String icon;
    private String note;
    private Integer weight;
    private Integer singleDrawCost;
    private Boolean enabled;
    private Boolean editable;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRarity() {
        return rarity;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Integer getSingleDrawCost() {
        return singleDrawCost;
    }

    public void setSingleDrawCost(Integer singleDrawCost) {
        this.singleDrawCost = singleDrawCost;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getEditable() {
        return editable;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }
}
