package com.civicfix.platform.workorders.dto;

import jakarta.validation.constraints.NotNull;

public class VerifyWorkOrderRequest {

    @NotNull(message = "Approval decision (true/false) is required")
    private Boolean approved;

    private String verificationNotes;

    private String rejectionReason;

    public VerifyWorkOrderRequest() {
    }

    public VerifyWorkOrderRequest(Boolean approved, String verificationNotes, String rejectionReason) {
        this.approved = approved;
        this.verificationNotes = verificationNotes;
        this.rejectionReason = rejectionReason;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public String getVerificationNotes() {
        return verificationNotes;
    }

    public void setVerificationNotes(String verificationNotes) {
        this.verificationNotes = verificationNotes;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
