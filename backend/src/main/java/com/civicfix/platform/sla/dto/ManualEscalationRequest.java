package com.civicfix.platform.sla.dto;

import com.civicfix.platform.sla.enums.EscalationLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ManualEscalationRequest {

    @NotNull(message = "Target escalation level is required")
    private EscalationLevel targetLevel;

    private Long targetEmployeeId;

    @NotBlank(message = "Escalation reason is required")
    private String reason;

    public ManualEscalationRequest() {
    }

    public ManualEscalationRequest(EscalationLevel targetLevel, Long targetEmployeeId, String reason) {
        this.targetLevel = targetLevel;
        this.targetEmployeeId = targetEmployeeId;
        this.reason = reason;
    }

    public EscalationLevel getTargetLevel() {
        return targetLevel;
    }

    public void setTargetLevel(EscalationLevel targetLevel) {
        this.targetLevel = targetLevel;
    }

    public Long getTargetEmployeeId() {
        return targetEmployeeId;
    }

    public void setTargetEmployeeId(Long targetEmployeeId) {
        this.targetEmployeeId = targetEmployeeId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
