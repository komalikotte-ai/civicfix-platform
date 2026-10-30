package com.civicfix.platform.inventory.dto;

import com.civicfix.platform.inventory.entity.StockTransaction;
import com.civicfix.platform.inventory.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public class StockTransactionDto {

    private Long id;
    private String transactionCode;
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private Long itemId;
    private String itemCode;
    private String itemName;
    private TransactionType transactionType;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalValue;
    private String referenceType;
    private Long referenceId;
    private Long workOrderId;
    private String workOrderNumber;
    private Long toWarehouseId;
    private String toWarehouseCode;
    private String remarks;
    private Instant transactionDate;
    private Long performedById;
    private String performedByName;
    private Instant createdAt;
    private String createdBy;

    public StockTransactionDto() {}

    public static StockTransactionDto fromEntity(StockTransaction entity) {
        if (entity == null) return null;
        StockTransactionDto dto = new StockTransactionDto();
        dto.setId(entity.getId());
        dto.setTransactionCode(entity.getTransactionCode());
        if (entity.getWarehouse() != null) {
            dto.setWarehouseId(entity.getWarehouse().getId());
            dto.setWarehouseCode(entity.getWarehouse().getWarehouseCode());
            dto.setWarehouseName(entity.getWarehouse().getName());
        }
        if (entity.getItem() != null) {
            dto.setItemId(entity.getItem().getId());
            dto.setItemCode(entity.getItem().getItemCode());
            dto.setItemName(entity.getItem().getName());
        }
        dto.setTransactionType(entity.getTransactionType());
        dto.setQuantity(entity.getQuantity());
        dto.setUnitPrice(entity.getUnitPrice());
        dto.setTotalValue(entity.getTotalValue());
        dto.setReferenceType(entity.getReferenceType());
        dto.setReferenceId(entity.getReferenceId());
        if (entity.getWorkOrder() != null) {
            dto.setWorkOrderId(entity.getWorkOrder().getId());
            dto.setWorkOrderNumber(entity.getWorkOrder().getWorkOrderNumber());
        }
        if (entity.getToWarehouse() != null) {
            dto.setToWarehouseId(entity.getToWarehouse().getId());
            dto.setToWarehouseCode(entity.getToWarehouse().getWarehouseCode());
        }
        dto.setRemarks(entity.getRemarks());
        dto.setTransactionDate(entity.getTransactionDate());
        if (entity.getPerformedBy() != null) {
            dto.setPerformedById(entity.getPerformedBy().getId());
            dto.setPerformedByName(entity.getPerformedBy().getFullName());
        }
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTransactionCode() { return transactionCode; }
    public void setTransactionCode(String transactionCode) { this.transactionCode = transactionCode; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getWarehouseCode() { return warehouseCode; }
    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getTotalValue() { return totalValue; }
    public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }
    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }
    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public String getWorkOrderNumber() { return workOrderNumber; }
    public void setWorkOrderNumber(String workOrderNumber) { this.workOrderNumber = workOrderNumber; }
    public Long getToWarehouseId() { return toWarehouseId; }
    public void setToWarehouseId(Long toWarehouseId) { this.toWarehouseId = toWarehouseId; }
    public String getToWarehouseCode() { return toWarehouseCode; }
    public void setToWarehouseCode(String toWarehouseCode) { this.toWarehouseCode = toWarehouseCode; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public Instant getTransactionDate() { return transactionDate; }
    public void setTransactionDate(Instant transactionDate) { this.transactionDate = transactionDate; }
    public Long getPerformedById() { return performedById; }
    public void setPerformedById(Long performedById) { this.performedById = performedById; }
    public String getPerformedByName() { return performedByName; }
    public void setPerformedByName(String performedByName) { this.performedByName = performedByName; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
