package com.civicfix.platform.workorders.dto;

import com.civicfix.platform.workorders.entity.WorkOrderComment;

import java.time.Instant;

public class WorkOrderCommentDto {

    private Long id;
    private Long workOrderId;
    private Long authorId;
    private String authorName;
    private String authorRole;
    private String comment;
    private boolean internalOnly;
    private Instant createdAt;
    private Instant updatedAt;

    public WorkOrderCommentDto() {
    }

    public static WorkOrderCommentDto fromEntity(WorkOrderComment entity) {
        if (entity == null) {
            return null;
        }

        WorkOrderCommentDto dto = new WorkOrderCommentDto();
        dto.setId(entity.getId());
        if (entity.getWorkOrder() != null) {
            dto.setWorkOrderId(entity.getWorkOrder().getId());
        }
        if (entity.getAuthor() != null) {
            dto.setAuthorId(entity.getAuthor().getId());
        }
        dto.setAuthorName(entity.getAuthorName());
        dto.setAuthorRole(entity.getAuthorRole());
        dto.setComment(entity.getComment());
        dto.setInternalOnly(entity.isInternalOnly());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorRole() {
        return authorRole;
    }

    public void setAuthorRole(String authorRole) {
        this.authorRole = authorRole;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isInternalOnly() {
        return internalOnly;
    }

    public void setInternalOnly(boolean internalOnly) {
        this.internalOnly = internalOnly;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
