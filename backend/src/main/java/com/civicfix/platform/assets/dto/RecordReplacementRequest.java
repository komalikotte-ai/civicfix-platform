package com.civicfix.platform.assets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class RecordReplacementRequest {

    private Long replacedAssetId;

    @NotBlank(message = "Replacement reason cannot be empty")
    private String replacementReason;

    @NotNull(message = "Replacement cost is required")
    @PositiveOrZero(message = "Replacement cost must not be negative")
    private BigDecimal replacementCost = BigDecimal.ZERO;

    private String approvedBy;

    private String disposalMethod;

    @PositiveOrZero(message = "Salvage value must not be negative")
    private BigDecimal salvageValue = BigDecimal.ZERO;

    public RecordReplacementRequest() {
    }

    public Long getReplacedAssetId() {
        return replacedAssetId;
    }

    public void setReplacedAssetId(Long replacedAssetId) {
        this.replacedAssetId = replacedAssetId;
    }

    public String getReplacementReason() {
        return replacementReason;
    }

    public void setReplacementReason(String replacementReason) {
        this.replacementReason = replacementReason;
    }

    public BigDecimal getReplacementCost() {
        return replacementCost;
    }

    public void setReplacementCost(BigDecimal replacementCost) {
        this.replacementCost = replacementCost;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getDisposalMethod() {
        return disposalMethod;
    }

    public void setDisposalMethod(String disposalMethod) {
        this.disposalMethod = disposalMethod;
    }

    public BigDecimal getSalvageValue() {
        return salvageValue;
    }

    public void setSalvageValue(BigDecimal salvageValue) {
        this.salvageValue = salvageValue;
    }
}
