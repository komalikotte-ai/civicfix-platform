package com.civicfix.platform.assets.dto;

import com.civicfix.platform.assets.enums.ConditionRating;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class RecordRepairRequest {

    private Long workOrderId;

    private Long complaintId;

    private String repairType = "CORRECTIVE";

    private Long repairedById;

    @NotNull(message = "Repair cost is required")
    @PositiveOrZero(message = "Cost must not be negative")
    private BigDecimal cost = BigDecimal.ZERO;

    @NotBlank(message = "Repair description cannot be empty")
    private String description;

    private String partsReplaced;

    private ConditionRating beforeCondition;

    private ConditionRating afterCondition;

    @PositiveOrZero(message = "Downtime hours must not be negative")
    private BigDecimal downtimeHours = BigDecimal.ZERO;

    public RecordRepairRequest() {
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    public Long getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(Long complaintId) {
        this.complaintId = complaintId;
    }

    public String getRepairType() {
        return repairType;
    }

    public void setRepairType(String repairType) {
        this.repairType = repairType;
    }

    public Long getRepairedById() {
        return repairedById;
    }

    public void setRepairedById(Long repairedById) {
        this.repairedById = repairedById;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPartsReplaced() {
        return partsReplaced;
    }

    public void setPartsReplaced(String partsReplaced) {
        this.partsReplaced = partsReplaced;
    }

    public ConditionRating getBeforeCondition() {
        return beforeCondition;
    }

    public void setBeforeCondition(ConditionRating beforeCondition) {
        this.beforeCondition = beforeCondition;
    }

    public ConditionRating getAfterCondition() {
        return afterCondition;
    }

    public void setAfterCondition(ConditionRating afterCondition) {
        this.afterCondition = afterCondition;
    }

    public BigDecimal getDowntimeHours() {
        return downtimeHours;
    }

    public void setDowntimeHours(BigDecimal downtimeHours) {
        this.downtimeHours = downtimeHours;
    }
}
