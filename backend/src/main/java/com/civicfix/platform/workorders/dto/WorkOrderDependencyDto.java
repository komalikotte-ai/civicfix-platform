package com.civicfix.platform.workorders.dto;

import com.civicfix.platform.workorders.entity.WorkOrderDependency;
import com.civicfix.platform.workorders.enums.DependencyType;
import com.civicfix.platform.workorders.enums.WorkOrderStatus;

import java.time.Instant;

public class WorkOrderDependencyDto {

    private Long id;
    private Long workOrderId;
    private String workOrderNumber;
    private Long dependsOnWorkOrderId;
    private String dependsOnWorkOrderNumber;
    private String dependsOnWorkOrderTitle;
    private WorkOrderStatus dependsOnWorkOrderStatus;
    private DependencyType dependencyType;
    private Instant createdAt;

    public WorkOrderDependencyDto() {
    }

    public static WorkOrderDependencyDto fromEntity(WorkOrderDependency entity) {
        if (entity == null) {
            return null;
        }

        WorkOrderDependencyDto dto = new WorkOrderDependencyDto();
        dto.setId(entity.getId());
        if (entity.getWorkOrder() != null) {
            dto.setWorkOrderId(entity.getWorkOrder().getId());
            dto.setWorkOrderNumber(entity.getWorkOrder().getWorkOrderNumber());
        }
        if (entity.getDependsOnWorkOrder() != null) {
            dto.setDependsOnWorkOrderId(entity.getDependsOnWorkOrder().getId());
            dto.setDependsOnWorkOrderNumber(entity.getDependsOnWorkOrder().getWorkOrderNumber());
            dto.setDependsOnWorkOrderTitle(entity.getDependsOnWorkOrder().getTitle());
            dto.setDependsOnWorkOrderStatus(entity.getDependsOnWorkOrder().getStatus());
        }
        dto.setDependencyType(entity.getDependencyType());
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

    public String getWorkOrderNumber() {
        return workOrderNumber;
    }

    public void setWorkOrderNumber(String workOrderNumber) {
        this.workOrderNumber = workOrderNumber;
    }

    public Long getDependsOnWorkOrderId() {
        return dependsOnWorkOrderId;
    }

    public void setDependsOnWorkOrderId(Long dependsOnWorkOrderId) {
        this.dependsOnWorkOrderId = dependsOnWorkOrderId;
    }

    public String getDependsOnWorkOrderNumber() {
        return dependsOnWorkOrderNumber;
    }

    public void setDependsOnWorkOrderNumber(String dependsOnWorkOrderNumber) {
        this.dependsOnWorkOrderNumber = dependsOnWorkOrderNumber;
    }

    public String getDependsOnWorkOrderTitle() {
        return dependsOnWorkOrderTitle;
    }

    public void setDependsOnWorkOrderTitle(String dependsOnWorkOrderTitle) {
        this.dependsOnWorkOrderTitle = dependsOnWorkOrderTitle;
    }

    public WorkOrderStatus getDependsOnWorkOrderStatus() {
        return dependsOnWorkOrderStatus;
    }

    public void setDependsOnWorkOrderStatus(WorkOrderStatus dependsOnWorkOrderStatus) {
        this.dependsOnWorkOrderStatus = dependsOnWorkOrderStatus;
    }

    public DependencyType getDependencyType() {
        return dependencyType;
    }

    public void setDependencyType(DependencyType dependencyType) {
        this.dependencyType = dependencyType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
