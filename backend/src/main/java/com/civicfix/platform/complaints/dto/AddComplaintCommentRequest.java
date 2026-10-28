package com.civicfix.platform.complaints.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AddComplaintCommentRequest {

    @NotBlank(message = "Comment text is required")
    @Size(max = 2000, message = "Comment cannot exceed 2000 characters")
    private String comment;

    private boolean internalOnly = false;

    public AddComplaintCommentRequest() {
    }

    public AddComplaintCommentRequest(String comment, boolean internalOnly) {
        this.comment = comment;
        this.internalOnly = internalOnly;
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
}
