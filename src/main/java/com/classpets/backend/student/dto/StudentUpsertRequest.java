package com.classpets.backend.student.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class StudentUpsertRequest {
    @NotBlank
    private String name;
    private String no;
    @NotBlank
    private String group;
    @NotBlank
    @Pattern(regexp = "^(男|女)$", message = "性别必须为 男/女")
    private String gender;
    @NotNull
    private Integer total;
    @NotNull
    private Integer redeem;
    @NotNull
    private Integer level;
    private Long groupId; // ID of the group this student belongs to
    private String avatarImage;
    private String petId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getRedeem() {
        return redeem;
    }

    public void setRedeem(Integer redeem) {
        this.redeem = redeem;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getAvatarImage() {
        return avatarImage;
    }

    public void setAvatarImage(String avatarImage) {
        this.avatarImage = avatarImage;
    }

    public String getPetId() {
        return petId;
    }

    public void setPetId(String petId) {
        this.petId = petId;
    }
}
