package com.civicfix.platform.inventory.dto;

import com.civicfix.platform.inventory.entity.WarehouseStock;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class WarehouseStockDto {

    private Long id;
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private Long itemId;
    private String itemCode;
    private String itemName;
    private int quantity;
    private int reservedQty;
    private int availableQty;
    private String locationCode;
    private String batchNumber;
    private LocalDate expiryDate;
    private Instant lastCountedAt;
    private int reorderLevel;
    private int minimumStock;
    private boolean belowReorder;
    private boolean belowMinimum;

    public WarehouseStockDto() {}

    public static WarehouseStockDto fromEntity(WarehouseStock entity) {
        if (entity == null) return null;
        WarehouseStockDto dto = new WarehouseStockDto();
        dto.setId(entity.getId());
        if (entity.getWarehouse() != null) {
            dto.setWarehouseId(entity.getWarehouse().getId());
            dto.setWarehouseCode(entity.getWarehouse().getWarehouseCode());
            dto.setWarehouseName(entity.getWarehouse().getName());
        }
        if (entity.getItem() != null) {
            dto.setItemId(entity.getItem().getId());
            dto.setItemCode(entity.getItem().getItemCode());
            dto.setItemName(entity.getItem().getName());
            dto.setReorderLevel(entity.getItem().getReorderLevel());
            dto.setMinimumStock(entity.getItem().getMinimumStock());
            dto.setBelowReorder(entity.getQuantity() <= entity.getItem().getReorderLevel());
            dto.setBelowMinimum(entity.getQuantity() <= entity.getItem().getMinimumStock());
        }
        dto.setQuantity(entity.getQuantity());
        dto.setReservedQty(entity.getReservedQty());
        dto.setAvailableQty(entity.getQuantity() - entity.getReservedQty());
        dto.setLocationCode(entity.getLocationCode());
        dto.setBatchNumber(entity.getBatchNumber());
        dto.setExpiryDate(entity.getExpiryDate());
        dto.setLastCountedAt(entity.getLastCountedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getReservedQty() { return reservedQty; }
    public void setReservedQty(int reservedQty) { this.reservedQty = reservedQty; }
    public int getAvailableQty() { return availableQty; }
    public void setAvailableQty(int availableQty) { this.availableQty = availableQty; }
    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }
    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public Instant getLastCountedAt() { return lastCountedAt; }
    public void setLastCountedAt(Instant lastCountedAt) { this.lastCountedAt = lastCountedAt; }
    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }
    public int getMinimumStock() { return minimumStock; }
    public void setMinimumStock(int minimumStock) { this.minimumStock = minimumStock; }
    public boolean isBelowReorder() { return belowReorder; }
    public void setBelowReorder(boolean belowReorder) { this.belowReorder = belowReorder; }
    public boolean isBelowMinimum() { return belowMinimum; }
    public void setBelowMinimum(boolean belowMinimum) { this.belowMinimum = belowMinimum; }
}
