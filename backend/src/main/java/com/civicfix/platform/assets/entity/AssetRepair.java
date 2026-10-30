package com.civicfix.platform.assets.entity;

import com.civicfix.platform.assets.enums.ConditionRating;
import com.civicfix.platform.complaints.entity.Complaint;
import com.civicfix.platform.users.entity.Employee;
import com.civicfix.platform.workorders.entity.WorkOrder;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "asset_repairs", indexes = {
        @Index(name = "idx_rep_asset", columnList = "asset_id"),
        @Index(name = "idx_rep_date", columnList = "repair_date")
})
public class AssetRepair {

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
    @JoinColumn(name = "complaint_id")
    private Complaint complaint;

    @Column(name = "repair_type", nullable = false, length = 50)
    private String repairType = "CORRECTIVE";

    @Column(name = "repair_date", nullable = false)
    private Instant repairDate = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repaired_by_id")
    private Employee repairedBy;

    @Column(name = "cost", precision = 12, scale = 2, nullable = false)
    private BigDecimal cost = BigDecimal.ZERO;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "parts_replaced", columnDefinition = "TEXT")
    private String partsReplaced;

    @Enumerated(EnumType.STRING)
    @Column(name = "before_condition", length = 30)
    private ConditionRating beforeCondition;

    @Enumerated(EnumType.STRING)
    @Column(name = "after_condition", length = 30)
    private ConditionRating afterCondition;

    @Column(name = "downtime_hours", precision = 8, scale = 2)
    private BigDecimal downtimeHours = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "created_by", length = 100)
    private String createdBy;

    public AssetRepair() {
    }

    public AssetRepair(Asset asset, BigDecimal cost, String description, String createdBy) {
        this.asset = asset;
        this.cost = cost != null ? cost : BigDecimal.ZERO;
        this.description = description;
        this.createdBy = createdBy;
        this.repairDate = Instant.now();
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

    public Complaint getComplaint() {
        return complaint;
    }

    public void setComplaint(Complaint complaint) {
        this.complaint = complaint;
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

    public Employee getRepairedBy() {
        return repairedBy;
    }

    public void setRepairedBy(Employee repairedBy) {
        this.repairedBy = repairedBy;
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
