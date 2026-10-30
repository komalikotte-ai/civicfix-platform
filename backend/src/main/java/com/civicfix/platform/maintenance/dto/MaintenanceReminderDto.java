package com.civicfix.platform.maintenance.dto;

import com.civicfix.platform.maintenance.entity.MaintenanceReminder;
import com.civicfix.platform.maintenance.enums.ReminderStatus;

import java.time.Instant;

public class MaintenanceReminderDto {

    private Long id;
    private String title;
    private String message;
    private Instant dueDate;
    private ReminderStatus status;
    private String reminderType;
    private Long scheduleId;
    private String scheduleCode;
    private Long assetId;
    private String assetName;
    private Long recipientId;
    private String recipientName;
    private Instant sentAt;
    private Instant acknowledgedAt;
    private Instant createdAt;

    public MaintenanceReminderDto() {}

    public static MaintenanceReminderDto fromEntity(MaintenanceReminder r) {
        if (r == null) return null;
        MaintenanceReminderDto dto = new MaintenanceReminderDto();
        dto.setId(r.getId());
        dto.setTitle(r.getTitle());
        dto.setMessage(r.getMessage());
        dto.setDueDate(r.getDueDate());
        dto.setStatus(r.getStatus());
        dto.setReminderType(r.getReminderType());
        if (r.getSchedule() != null) {
            dto.setScheduleId(r.getSchedule().getId());
            dto.setScheduleCode(r.getSchedule().getScheduleCode());
        }
        if (r.getAsset() != null) {
            dto.setAssetId(r.getAsset().getId());
            dto.setAssetName(r.getAsset().getName());
        }
        if (r.getRecipient() != null) {
            dto.setRecipientId(r.getRecipient().getId());
            dto.setRecipientName(r.getRecipient().getFullName());
        }
        dto.setSentAt(r.getSentAt());
        dto.setAcknowledgedAt(r.getAcknowledgedAt());
        dto.setCreatedAt(r.getCreatedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Instant getDueDate() { return dueDate; }
    public void setDueDate(Instant dueDate) { this.dueDate = dueDate; }
    public ReminderStatus getStatus() { return status; }
    public void setStatus(ReminderStatus status) { this.status = status; }
    public String getReminderType() { return reminderType; }
    public void setReminderType(String reminderType) { this.reminderType = reminderType; }
    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
    public String getScheduleCode() { return scheduleCode; }
    public void setScheduleCode(String scheduleCode) { this.scheduleCode = scheduleCode; }
    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }
    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }
    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }
    public Instant getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(Instant acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
