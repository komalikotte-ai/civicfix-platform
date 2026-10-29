package com.civicfix.platform.complaints.dto;

import com.civicfix.platform.complaints.entity.ComplaintComment;

import java.time.Instant;

public class ComplaintCommentDto {

    private Long id;
    private Long authorId;
    private String authorName;
    private String authorRole;
    private String comment;
    private boolean internalOnly;
    private Instant createdAt;

    public ComplaintCommentDto() {
    }

    public static ComplaintCommentDto fromEntity(ComplaintComment entity) {
        if (entity == null) return null;
        ComplaintCommentDto dto = new ComplaintCommentDto();
        dto.setId(entity.getId());
        if (entity.getAuthor() != null) {
            dto.setAuthorId(entity.getAuthor().getId());
        }
        dto.setAuthorName(entity.getAuthorName());
        dto.setAuthorRole(entity.getAuthorRole());
        dto.setComment(entity.getComment());
        dto.setInternalOnly(entity.isInternalOnly());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
