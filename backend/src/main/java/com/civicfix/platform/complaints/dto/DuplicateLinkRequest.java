package com.civicfix.platform.complaints.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DuplicateLinkRequest {

    @NotNull(message = "Primary complaint ID is required")
    private Long primaryComplaintId;

    private Double similarityScore;

    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;

    public DuplicateLinkRequest() {
    }

    public DuplicateLinkRequest(Long primaryComplaintId, Double similarityScore, String reason) {
        this.primaryComplaintId = primaryComplaintId;
        this.similarityScore = similarityScore;
        this.reason = reason;
    }

    public Long getPrimaryComplaintId() {
        return primaryComplaintId;
    }

    public void setPrimaryComplaintId(Long primaryComplaintId) {
        this.primaryComplaintId = primaryComplaintId;
    }

    public Double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(Double similarityScore) {
        this.similarityScore = similarityScore;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
