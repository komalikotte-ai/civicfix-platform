package com.civicfix.platform.workorders.dto;

import com.civicfix.platform.workorders.enums.DependencyType;
import jakarta.validation.constraints.NotNull;

public class AddWorkOrderDependencyRequest {

    @NotNull(message = "Depends-on work order ID is required")
    private Long dependsOnWorkOrderId;

    private DependencyType dependencyType = DependencyType.FINISH_TO_START;

    public AddWorkOrderDependencyRequest() {
    }

    public AddWorkOrderDependencyRequest(Long dependsOnWorkOrderId, DependencyType dependencyType) {
        this.dependsOnWorkOrderId = dependsOnWorkOrderId;
        this.dependencyType = dependencyType != null ? dependencyType : DependencyType.FINISH_TO_START;
    }

    public Long getDependsOnWorkOrderId() {
        return dependsOnWorkOrderId;
    }

    public void setDependsOnWorkOrderId(Long dependsOnWorkOrderId) {
        this.dependsOnWorkOrderId = dependsOnWorkOrderId;
    }

    public DependencyType getDependencyType() {
        return dependencyType;
    }

    public void setDependencyType(DependencyType dependencyType) {
        this.dependencyType = dependencyType;
    }
}
