package com.civicfix.platform.workorders.dto;

import jakarta.validation.constraints.NotBlank;

public class AddWorkOrderCommentRequest {

    @NotBlank(message = "Comment cannot be empty")
    private String comment;

    private boolean internalOnly = false;

    public AddWorkOrderCommentRequest() {
    }

    public AddWorkOrderCommentRequest(String comment, boolean internalOnly) {
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
