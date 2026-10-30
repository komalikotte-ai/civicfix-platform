package com.civicfix.platform.assets.entity;

import com.civicfix.platform.assets.enums.AssetStatus;
import com.civicfix.platform.assets.enums.ConditionRating;
import com.civicfix.platform.users.entity.Employee;
import com.civicfix.platform.workorders.entity.WorkOrder;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "asset_inspections", indexes = {
        @Index(name = "idx_insp_asset", columnList = "asset_id"),
        @Index(name = "idx_insp_date", columnList = "inspection_date")
})
public class AssetInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id")
    private WorkOrder workOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspector_id")
    private Employee inspector;

    @Column(name = "inspection_date", nullable = false)
    private Instant inspectionDate = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_rating", nullable = false, length = 30)
    private ConditionRating conditionRating;

    @Enumerated(EnumType.STRING)
    @Column(name = "operational_status", nullable = false, length = 30)
    private AssetStatus operationalStatus;

    @Column(name = "findings", nullable = false, columnDefinition = "TEXT")
    private String findings;

    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;

    @Column(name = "defects_observed", columnDefinition = "TEXT")
    private String defectsObserved;

    @Column(name = "score")
    private Integer score;

    @Column(name = "photos_json", columnDefinition = "TEXT")
    private String photosJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "created_by", length = 100)
    private String createdBy;

    public AssetInspection() {
    }

    public AssetInspection(Asset asset, ConditionRating conditionRating, AssetStatus operationalStatus, String findings, String createdBy) {
        this.asset = asset;
        this.conditionRating = conditionRating;
        this.operationalStatus = operationalStatus;
        this.findings = findings;
        this.createdBy = createdBy;
        this.inspectionDate = Instant.now();
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

    public WorkOrder getWorkOrder() {
        return workOrder;
    }

    public void setWorkOrder(WorkOrder workOrder) {
        this.workOrder = workOrder;
    }

    public Employee getInspector() {
        return inspector;
    }

    public void setInspector(Employee inspector) {
        this.inspector = inspector;
    }

    public Instant getInspectionDate() {
        return inspectionDate;
    }

    public void setInspectionDate(Instant inspectionDate) {
        this.inspectionDate = inspectionDate;
    }

    public ConditionRating getConditionRating() {
        return conditionRating;
    }

    public void setConditionRating(ConditionRating conditionRating) {
        this.conditionRating = conditionRating;
    }

    public AssetStatus getOperationalStatus() {
        return operationalStatus;
    }

    public void setOperationalStatus(AssetStatus operationalStatus) {
        this.operationalStatus = operationalStatus;
    }

    public String getFindings() {
        return findings;
    }

    public void setFindings(String findings) {
        this.findings = findings;
    }

    public String getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }

    public String getDefectsObserved() {
        return defectsObserved;
    }

    public void setDefectsObserved(String defectsObserved) {
        this.defectsObserved = defectsObserved;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getPhotosJson() {
        return photosJson;
    }

    public void setPhotosJson(String photosJson) {
        this.photosJson = photosJson;
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
