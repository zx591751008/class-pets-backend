package com.classpets.backend.auth.dto;

import javax.validation.constraints.Pattern;

public class UpdatePreferencesRequestDTO {

    @Pattern(regexp = "^(pet|normal)$", message = "System mode must be 'pet' or 'normal'")
    private String systemMode;

    private String theme;

    public String getSystemMode() {
        return systemMode;
    }

    public void setSystemMode(String systemMode) {
        this.systemMode = systemMode;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }
}
