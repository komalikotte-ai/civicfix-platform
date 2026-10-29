package com.civicfix.platform.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordRequest {

    @NotBlank(message = "Username or email is required")
    private String emailOrUsername;

    public ForgotPasswordRequest() {
    }

    public ForgotPasswordRequest(String emailOrUsername) {
        this.emailOrUsername = emailOrUsername;
    }

    public String getEmailOrUsername() {
        return emailOrUsername;
    }

    public void setEmailOrUsername(String emailOrUsername) {
        this.emailOrUsername = emailOrUsername;
    }
}
