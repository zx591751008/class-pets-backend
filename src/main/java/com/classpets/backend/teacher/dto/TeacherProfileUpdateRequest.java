package com.classpets.backend.teacher.dto;

import javax.validation.constraints.Size;

public class TeacherProfileUpdateRequest {
    @Size(max = 50, message = "昵称长度不能超过50")
    private String nickname;

    @Size(min = 6, max = 32, message = "密码长度主要6-32位")
    private String password;

    private String activationCode;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }
}
