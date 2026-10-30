package com.civicfix.platform.procurement.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CreateVendorEvaluationRequest {

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    private Long purchaseOrderId;

    @NotNull(message = "Delivery rating is required")
    @DecimalMin(value = "1.0", message = "Rating must be at least 1.0")
    @DecimalMax(value = "5.0", message = "Rating cannot exceed 5.0")
    private BigDecimal deliveryRating;

    @NotNull(message = "Quality rating is required")
    @DecimalMin(value = "1.0", message = "Rating must be at least 1.0")
    @DecimalMax(value = "5.0", message = "Rating cannot exceed 5.0")
    private BigDecimal qualityRating;

    @NotNull(message = "Pricing rating is required")
    @DecimalMin(value = "1.0", message = "Rating must be at least 1.0")
    @DecimalMax(value = "5.0", message = "Rating cannot exceed 5.0")
    private BigDecimal pricingRating;

    private String evaluatorComments;

    public CreateVendorEvaluationRequest() {}

    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public Long getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(Long purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }
    public BigDecimal getDeliveryRating() { return deliveryRating; }
    public void setDeliveryRating(BigDecimal deliveryRating) { this.deliveryRating = deliveryRating; }
    public BigDecimal getQualityRating() { return qualityRating; }
    public void setQualityRating(BigDecimal qualityRating) { this.qualityRating = qualityRating; }
    public BigDecimal getPricingRating() { return pricingRating; }
    public void setPricingRating(BigDecimal pricingRating) { this.pricingRating = pricingRating; }
    public String getEvaluatorComments() { return evaluatorComments; }
    public void setEvaluatorComments(String evaluatorComments) { this.evaluatorComments = evaluatorComments; }
}
