package com.civicfix.platform.assets.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "asset_replacements", indexes = {
        @Index(name = "idx_repl_asset", columnList = "asset_id")
})
public class AssetReplacement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(name = "replacement_date", nullable = false)
    private Instant replacementDate = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replaced_asset_id")
    private Asset replacedAsset;

    @Column(name = "replacement_reason", nullable = false, columnDefinition = "TEXT")
    private String replacementReason;

    @Column(name = "replacement_cost", precision = 14, scale = 2, nullable = false)
    private BigDecimal replacementCost = BigDecimal.ZERO;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "disposal_method", length = 50)
    private String disposalMethod;

    @Column(name = "salvage_value", precision = 12, scale = 2)
    private BigDecimal salvageValue = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "created_by", length = 100)
    private String createdBy;

    public AssetReplacement() {
    }

    public AssetReplacement(Asset asset, Asset replacedAsset, String replacementReason, BigDecimal replacementCost, String createdBy) {
        this.asset = asset;
        this.replacedAsset = replacedAsset;
        this.replacementReason = replacementReason;
        this.replacementCost = replacementCost != null ? replacementCost : BigDecimal.ZERO;
        this.createdBy = createdBy;
        this.replacementDate = Instant.now();
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public Instant getReplacementDate() {
        return replacementDate;
    }

    public void setReplacementDate(Instant replacementDate) {
        this.replacementDate = replacementDate;
    }

    public Asset getReplacedAsset() {
        return replacedAsset;
    }

    public void setReplacedAsset(Asset replacedAsset) {
        this.replacedAsset = replacedAsset;
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
