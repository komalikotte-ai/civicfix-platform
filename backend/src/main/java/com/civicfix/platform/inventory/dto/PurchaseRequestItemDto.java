package com.civicfix.platform.inventory.dto;

import com.civicfix.platform.inventory.entity.PurchaseRequestItem;

import java.math.BigDecimal;
import java.time.Instant;

public class PurchaseRequestItemDto {

    private Long id;
    private Long itemId;
    private String itemCode;
    private String itemName;
    private int quantityRequested;
    private Integer quantityApproved;
    private BigDecimal unitPriceEstimate;
    private BigDecimal totalEstimatedCost;
    private Long supplierId;
    private String supplierName;
    private String remarks;
    private Instant createdAt;

    public PurchaseRequestItemDto() {}

    public static PurchaseRequestItemDto fromEntity(PurchaseRequestItem entity) {
        if (entity == null) return null;
        PurchaseRequestItemDto dto = new PurchaseRequestItemDto();
        dto.setId(entity.getId());
        if (entity.getItem() != null) {
            dto.setItemId(entity.getItem().getId());
            dto.setItemCode(entity.getItem().getItemCode());
            dto.setItemName(entity.getItem().getName());
        }
        dto.setQuantityRequested(entity.getQuantityRequested());
        dto.setQuantityApproved(entity.getQuantityApproved());
        dto.setUnitPriceEstimate(entity.getUnitPriceEstimate());
        dto.setTotalEstimatedCost(entity.getTotalEstimatedCost());
        if (entity.getSupplier() != null) {
            dto.setSupplierId(entity.getSupplier().getId());
            dto.setSupplierName(entity.getSupplier().getName());
        }
        dto.setRemarks(entity.getRemarks());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public int getQuantityRequested() { return quantityRequested; }
    public void setQuantityRequested(int quantityRequested) { this.quantityRequested = quantityRequested; }
    public Integer getQuantityApproved() { return quantityApproved; }
    public void setQuantityApproved(Integer quantityApproved) { this.quantityApproved = quantityApproved; }
    public BigDecimal getUnitPriceEstimate() { return unitPriceEstimate; }
    public void setUnitPriceEstimate(BigDecimal unitPriceEstimate) { this.unitPriceEstimate = unitPriceEstimate; }
    public BigDecimal getTotalEstimatedCost() { return totalEstimatedCost; }
    public void setTotalEstimatedCost(BigDecimal totalEstimatedCost) { this.totalEstimatedCost = totalEstimatedCost; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
