package com.civicfix.platform.inventory.dto;

import com.civicfix.platform.inventory.entity.InventoryItem;
import com.civicfix.platform.inventory.enums.ItemCategory;
import com.civicfix.platform.inventory.enums.UnitOfMeasure;

import java.math.BigDecimal;
import java.time.Instant;

public class InventoryItemDto {

    private Long id;
    private String itemCode;
    private String name;
    private String description;
    private ItemCategory itemCategory;
    private UnitOfMeasure unitOfMeasure;
    private BigDecimal unitPrice;
    private int minimumStock;
    private int reorderLevel;
    private Integer maximumStock;
    private String hsnSacCode;
    private String specifications;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public InventoryItemDto() {}

    public static InventoryItemDto fromEntity(InventoryItem entity) {
        if (entity == null) return null;
        InventoryItemDto dto = new InventoryItemDto();
        dto.setId(entity.getId());
        dto.setItemCode(entity.getItemCode());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setItemCategory(entity.getItemCategory());
        dto.setUnitOfMeasure(entity.getUnitOfMeasure());
        dto.setUnitPrice(entity.getUnitPrice());
        dto.setMinimumStock(entity.getMinimumStock());
        dto.setReorderLevel(entity.getReorderLevel());
        dto.setMaximumStock(entity.getMaximumStock());
        dto.setHsnSacCode(entity.getHsnSacCode());
        dto.setSpecifications(entity.getSpecifications());
        dto.setActive(entity.isActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ItemCategory getItemCategory() { return itemCategory; }
    public void setItemCategory(ItemCategory itemCategory) { this.itemCategory = itemCategory; }
    public UnitOfMeasure getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(UnitOfMeasure unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public int getMinimumStock() { return minimumStock; }
    public void setMinimumStock(int minimumStock) { this.minimumStock = minimumStock; }
    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }
    public Integer getMaximumStock() { return maximumStock; }
    public void setMaximumStock(Integer maximumStock) { this.maximumStock = maximumStock; }
    public String getHsnSacCode() { return hsnSacCode; }
    public void setHsnSacCode(String hsnSacCode) { this.hsnSacCode = hsnSacCode; }
    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
