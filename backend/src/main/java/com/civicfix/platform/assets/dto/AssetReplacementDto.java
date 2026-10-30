package com.civicfix.platform.assets.dto;

import com.civicfix.platform.assets.entity.AssetReplacement;

import java.math.BigDecimal;
import java.time.Instant;

public class AssetReplacementDto {

    private Long id;
    private Long assetId;
    private String assetCode;
    private String assetName;
    private Instant replacementDate;
    private Long replacedAssetId;
    private String replacedAssetCode;
    private String replacedAssetName;
    private String replacementReason;
    private BigDecimal replacementCost;
    private String approvedBy;
    private String disposalMethod;
    private BigDecimal salvageValue;
    private Instant createdAt;
    private String createdBy;

    public AssetReplacementDto() {
    }

    public static AssetReplacementDto fromEntity(AssetReplacement entity) {
        if (entity == null) return null;

        AssetReplacementDto dto = new AssetReplacementDto();
        dto.setId(entity.getId());
        if (entity.getAsset() != null) {
            dto.setAssetId(entity.getAsset().getId());
            dto.setAssetCode(entity.getAsset().getAssetCode());
            dto.setAssetName(entity.getAsset().getName());
        }
        dto.setReplacementDate(entity.getReplacementDate());
        if (entity.getReplacedAsset() != null) {
            dto.setReplacedAssetId(entity.getReplacedAsset().getId());
            dto.setReplacedAssetCode(entity.getReplacedAsset().getAssetCode());
            dto.setReplacedAssetName(entity.getReplacedAsset().getName());
        }
        dto.setReplacementReason(entity.getReplacementReason());
        dto.setReplacementCost(entity.getReplacementCost());
        dto.setApprovedBy(entity.getApprovedBy());
        dto.setDisposalMethod(entity.getDisposalMethod());
        dto.setSalvageValue(entity.getSalvageValue());
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

    public Instant getReplacementDate() {
        return replacementDate;
    }

    public void setReplacementDate(Instant replacementDate) {
        this.replacementDate = replacementDate;
    }

    public Long getReplacedAssetId() {
        return replacedAssetId;
    }

    public void setReplacedAssetId(Long replacedAssetId) {
        this.replacedAssetId = replacedAssetId;
    }

    public String getReplacedAssetCode() {
        return replacedAssetCode;
    }

    public void setReplacedAssetCode(String replacedAssetCode) {
        this.replacedAssetCode = replacedAssetCode;
    }

    public String getReplacedAssetName() {
        return replacedAssetName;
    }

    public void setReplacedAssetName(String replacedAssetName) {
        this.replacedAssetName = replacedAssetName;
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
