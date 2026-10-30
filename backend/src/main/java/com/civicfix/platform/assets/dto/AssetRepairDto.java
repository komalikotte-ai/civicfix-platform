package com.civicfix.platform.assets.dto;

import com.civicfix.platform.assets.entity.AssetRepair;
import com.civicfix.platform.assets.enums.ConditionRating;

import java.math.BigDecimal;
import java.time.Instant;

public class AssetRepairDto {

    private Long id;
    private Long assetId;
    private String assetCode;
    private String assetName;
    private Long workOrderId;
    private String workOrderNumber;
    private Long complaintId;
    private String complaintTrackingNumber;
    private String repairType;
    private Instant repairDate;
    private Long repairedById;
    private String repairedByName;
    private BigDecimal cost;
    private String description;
    private String partsReplaced;
    private ConditionRating beforeCondition;
    private ConditionRating afterCondition;
    private BigDecimal downtimeHours;
    private Instant createdAt;
    private String createdBy;

    public AssetRepairDto() {
    }

    public static AssetRepairDto fromEntity(AssetRepair entity) {
        if (entity == null) return null;

        AssetRepairDto dto = new AssetRepairDto();
        dto.setId(entity.getId());
        if (entity.getAsset() != null) {
            dto.setAssetId(entity.getAsset().getId());
            dto.setAssetCode(entity.getAsset().getAssetCode());
            dto.setAssetName(entity.getAsset().getName());
        }
        if (entity.getWorkOrder() != null) {
            dto.setWorkOrderId(entity.getWorkOrder().getId());
            dto.setWorkOrderNumber(entity.getWorkOrder().getWorkOrderNumber());
        }
        if (entity.getComplaint() != null) {
            dto.setComplaintId(entity.getComplaint().getId());
            dto.setComplaintTrackingNumber(entity.getComplaint().getTrackingNumber());
        }
        dto.setRepairType(entity.getRepairType());
        dto.setRepairDate(entity.getRepairDate());
        if (entity.getRepairedBy() != null) {
            dto.setRepairedById(entity.getRepairedBy().getId());
            dto.setRepairedByName(entity.getRepairedBy().getFullName());
        }
        dto.setCost(entity.getCost());
        dto.setDescription(entity.getDescription());
        dto.setPartsReplaced(entity.getPartsReplaced());
        dto.setBeforeCondition(entity.getBeforeCondition());
        dto.setAfterCondition(entity.getAfterCondition());
        dto.setDowntimeHours(entity.getDowntimeHours());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setCreatedBy(entity.getCreatedBy());

        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public String getAssetCode() {
        return assetCode;
    }

    public void setAssetCode(String assetCode) {
        this.assetCode = assetCode;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
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

    public Long getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(Long complaintId) {
        this.complaintId = complaintId;
    }

    public String getComplaintTrackingNumber() {
        return complaintTrackingNumber;
    }

    public void setComplaintTrackingNumber(String complaintTrackingNumber) {
        this.complaintTrackingNumber = complaintTrackingNumber;
    }

    public String getRepairType() {
        return repairType;
    }

    public void setRepairType(String repairType) {
        this.repairType = repairType;
    }

    public Instant getRepairDate() {
        return repairDate;
    }

    public void setRepairDate(Instant repairDate) {
        this.repairDate = repairDate;
    }

    public Long getRepairedById() {
        return repairedById;
    }

    public void setRepairedById(Long repairedById) {
        this.repairedById = repairedById;
    }

    public String getRepairedByName() {
        return repairedByName;
    }

    public void setRepairedByName(String repairedByName) {
        this.repairedByName = repairedByName;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
