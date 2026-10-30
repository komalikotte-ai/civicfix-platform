package com.civicfix.platform.inventory.dto;

import com.civicfix.platform.inventory.entity.SupplierItem;

import java.math.BigDecimal;
import java.time.Instant;

public class SupplierItemDto {

    private Long id;
    private Long supplierId;
    private String supplierCode;
    private String supplierName;
    private Long itemId;
    private String itemCode;
    private String itemName;
    private BigDecimal unitPrice;
    private Integer leadTimeDays;
    private boolean preferred;
    private Instant createdAt;

    public SupplierItemDto() {}

    public static SupplierItemDto fromEntity(SupplierItem entity) {
        if (entity == null) return null;
        SupplierItemDto dto = new SupplierItemDto();
        dto.setId(entity.getId());
        if (entity.getSupplier() != null) {
            dto.setSupplierId(entity.getSupplier().getId());
            dto.setSupplierCode(entity.getSupplier().getSupplierCode());
            dto.setSupplierName(entity.getSupplier().getName());
        }
        if (entity.getItem() != null) {
            dto.setItemId(entity.getItem().getId());
            dto.setItemCode(entity.getItem().getItemCode());
            dto.setItemName(entity.getItem().getName());
        }
        dto.setUnitPrice(entity.getUnitPrice());
        dto.setLeadTimeDays(entity.getLeadTimeDays());
        dto.setPreferred(entity.isPreferred());
        dto.setCreatedAt(entity.getCreatedAt());
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
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public Integer getLeadTimeDays() { return leadTimeDays; }
    public void setLeadTimeDays(Integer leadTimeDays) { this.leadTimeDays = leadTimeDays; }
    public boolean isPreferred() { return preferred; }
    public void setPreferred(boolean preferred) { this.preferred = preferred; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
