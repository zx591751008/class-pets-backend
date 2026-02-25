package com.classpets.backend.auth.vo;

import java.time.LocalDateTime;

public class TeacherVO {
    private Long id;
    private String username;
    private String nickname;
    private Boolean screenLockEnabled;
    private LocalDateTime licenseExpiresAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Boolean getScreenLockEnabled() {
        return screenLockEnabled;
    }

    public void setScreenLockEnabled(Boolean screenLockEnabled) {
        this.screenLockEnabled = screenLockEnabled;
    }

    public LocalDateTime getLicenseExpiresAt() {
        return licenseExpiresAt;
    }

    public void setLicenseExpiresAt(LocalDateTime licenseExpiresAt) {
        this.licenseExpiresAt = licenseExpiresAt;
    }
}
