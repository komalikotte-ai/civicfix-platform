package com.civicfix.platform.auth.dto;

import com.civicfix.platform.auth.entity.RefreshToken;

import java.time.Instant;

public class UserSessionDto {

    private Long id;
    private String ipAddress;
    private Instant createdAt;
    private Instant expiryDate;
    private boolean active;

    public UserSessionDto() {
    }

    public static UserSessionDto fromEntity(RefreshToken entity) {
        UserSessionDto dto = new UserSessionDto();
        dto.setId(entity.getId());
        dto.setIpAddress(entity.getIpAddress());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setExpiryDate(entity.getExpiryDate());
        dto.setActive(entity.isValid());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
