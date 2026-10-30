package com.civicfix.platform.procurement.dto;

import com.civicfix.platform.procurement.entity.VendorEvaluation;

import java.math.BigDecimal;
import java.time.Instant;

public class VendorEvaluationDto {

    private Long id;
    private Long supplierId;
    private String supplierCode;
    private String supplierName;
    private Long purchaseOrderId;
    private String purchaseOrderNumber;
    private BigDecimal deliveryRating;
    private BigDecimal qualityRating;
    private BigDecimal pricingRating;
    private BigDecimal overallRating;
    private String evaluatorComments;
    private String evaluatedBy;
    private Instant evaluatedAt;

    public VendorEvaluationDto() {}

    public static VendorEvaluationDto fromEntity(VendorEvaluation entity) {
        if (entity == null) return null;
        VendorEvaluationDto dto = new VendorEvaluationDto();
        dto.setId(entity.getId());
        if (entity.getSupplier() != null) {
            dto.setSupplierId(entity.getSupplier().getId());
            dto.setSupplierCode(entity.getSupplier().getSupplierCode());
            dto.setSupplierName(entity.getSupplier().getName());
        }
        if (entity.getPurchaseOrder() != null) {
            dto.setPurchaseOrderId(entity.getPurchaseOrder().getId());
            dto.setPurchaseOrderNumber(entity.getPurchaseOrder().getOrderNumber());
        }
        dto.setDeliveryRating(entity.getDeliveryRating());
        dto.setQualityRating(entity.getQualityRating());
        dto.setPricingRating(entity.getPricingRating());
        dto.setOverallRating(entity.getOverallRating());
        dto.setEvaluatorComments(entity.getEvaluatorComments());
        dto.setEvaluatedBy(entity.getEvaluatedBy());
        dto.setEvaluatedAt(entity.getEvaluatedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public Long getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(Long purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }
    public String getPurchaseOrderNumber() { return purchaseOrderNumber; }
    public void setPurchaseOrderNumber(String purchaseOrderNumber) { this.purchaseOrderNumber = purchaseOrderNumber; }
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
}
