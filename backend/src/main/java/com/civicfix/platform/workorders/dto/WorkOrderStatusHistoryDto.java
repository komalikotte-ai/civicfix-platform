package com.civicfix.platform.workorders.dto;

import com.civicfix.platform.workorders.entity.WorkOrderStatusHistory;
import com.civicfix.platform.workorders.enums.WorkOrderStatus;

import java.time.Instant;

public class WorkOrderStatusHistoryDto {

    private Long id;
    private Long workOrderId;
    private WorkOrderStatus fromStatus;
    private WorkOrderStatus toStatus;
    private String remarks;
    private String actionBy;
    private Instant createdAt;

    public WorkOrderStatusHistoryDto() {
    }

    public static WorkOrderStatusHistoryDto fromEntity(WorkOrderStatusHistory entity) {
        if (entity == null) {
            return null;
        }

        WorkOrderStatusHistoryDto dto = new WorkOrderStatusHistoryDto();
        dto.setId(entity.getId());
        if (entity.getWorkOrder() != null) {
            dto.setWorkOrderId(entity.getWorkOrder().getId());
        }
        dto.setFromStatus(entity.getFromStatus());
        dto.setToStatus(entity.getToStatus());
        dto.setRemarks(entity.getRemarks());
        dto.setActionBy(entity.getActionBy());
        dto.setCreatedAt(entity.getCreatedAt());
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

    public WorkOrderStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(WorkOrderStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public WorkOrderStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(WorkOrderStatus toStatus) {
        this.toStatus = toStatus;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getActionBy() {
        return actionBy;
    }

    public void setActionBy(String actionBy) {
        this.actionBy = actionBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
