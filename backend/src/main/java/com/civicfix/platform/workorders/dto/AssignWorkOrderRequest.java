package com.civicfix.platform.workorders.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class AssignWorkOrderRequest {

    @NotNull(message = "Field worker ID is required")
    private Long workerId;

    private Long supervisorId;

    private String reason;

    private Instant scheduledStartDate;

    private Instant expectedCompletionDate;

    public AssignWorkOrderRequest() {
    }

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    public Long getSupervisorId() {
        return supervisorId;
    }

    public void setSupervisorId(Long supervisorId) {
        this.supervisorId = supervisorId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getScheduledStartDate() {
        return scheduledStartDate;
    }

    public void setScheduledStartDate(Instant scheduledStartDate) {
        this.scheduledStartDate = scheduledStartDate;
    }

    public Instant getExpectedCompletionDate() {
        return expectedCompletionDate;
    }

    public void setExpectedCompletionDate(Instant expectedCompletionDate) {
        this.expectedCompletionDate = expectedCompletionDate;
    }
}
