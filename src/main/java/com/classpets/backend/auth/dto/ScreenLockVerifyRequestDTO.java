package com.classpets.backend.auth.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class ScreenLockVerifyRequestDTO {

    @NotBlank
    @Pattern(regexp = "^\\d{6}$", message = "锁屏密码必须为6位数字")
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
