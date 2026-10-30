package com.civicfix.platform.procurement.entity;

import com.civicfix.platform.inventory.entity.Supplier;
import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "vendor_evaluations", indexes = {
        @Index(name = "idx_ve_sup", columnList = "supplier_id")
})
public class VendorEvaluation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id")
    private PurchaseOrder purchaseOrder;

    @Column(name = "delivery_rating", precision = 3, scale = 1, nullable = false)
    private BigDecimal deliveryRating;

    @Column(name = "quality_rating", precision = 3, scale = 1, nullable = false)
    private BigDecimal qualityRating;

    @Column(name = "pricing_rating", precision = 3, scale = 1, nullable = false)
    private BigDecimal pricingRating;

    @Column(name = "overall_rating", precision = 3, scale = 1, nullable = false)
    private BigDecimal overallRating;

    @Column(name = "evaluator_comments", columnDefinition = "TEXT")
    private String evaluatorComments;

    @Column(name = "evaluated_by", length = 100, nullable = false)
    private String evaluatedBy;

    @Column(name = "evaluated_at", nullable = false)
    private Instant evaluatedAt = Instant.now();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public VendorEvaluation() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
    public PurchaseOrder getPurchaseOrder() { return purchaseOrder; }
    public void setPurchaseOrder(PurchaseOrder purchaseOrder) { this.purchaseOrder = purchaseOrder; }
    public BigDecimal getDeliveryRating() { return deliveryRating; }
    public void setDeliveryRating(BigDecimal deliveryRating) { this.deliveryRating = deliveryRating; }
    public BigDecimal getQualityRating() { return qualityRating; }
    public void setQualityRating(BigDecimal qualityRating) { this.qualityRating = qualityRating; }
    public BigDecimal getPricingRating() { return pricingRating; }
    public void setPricingRating(BigDecimal pricingRating) { this.pricingRating = pricingRating; }
    public BigDecimal getOverallRating() { return overallRating; }
    public void setOverallRating(BigDecimal overallRating) { this.overallRating = overallRating; }
    public String getEvaluatorComments() { return evaluatorComments; }
    public void setEvaluatorComments(String evaluatorComments) { this.evaluatorComments = evaluatorComments; }
    public String getEvaluatedBy() { return evaluatedBy; }
    public void setEvaluatedBy(String evaluatedBy) { this.evaluatedBy = evaluatedBy; }
    public Instant getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(Instant evaluatedAt) { this.evaluatedAt = evaluatedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
