package com.civicfix.platform.workorders.dto;

import com.civicfix.platform.workorders.enums.WorkOrderStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateWorkOrderStatusRequest {

    @NotNull(message = "Target status is required")
    private WorkOrderStatus targetStatus;

    private String remarks;

    public UpdateWorkOrderStatusRequest() {
    }

    public UpdateWorkOrderStatusRequest(WorkOrderStatus targetStatus, String remarks) {
        this.targetStatus = targetStatus;
        this.remarks = remarks;
    }

    public WorkOrderStatus getTargetStatus() {
        return targetStatus;
    }

    public void setTargetStatus(WorkOrderStatus targetStatus) {
        this.targetStatus = targetStatus;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
