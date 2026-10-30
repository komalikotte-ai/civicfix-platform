package com.civicfix.platform.inventory.dto;

import com.civicfix.platform.inventory.enums.ItemCategory;
import com.civicfix.platform.inventory.enums.UnitOfMeasure;

import java.math.BigDecimal;

public class UpdateItemRequest {

    private String name;
    private String description;
    private ItemCategory itemCategory;
    private UnitOfMeasure unitOfMeasure;
    private BigDecimal unitPrice;
    private Integer minimumStock;
    private Integer reorderLevel;
    private Integer maximumStock;
    private String hsnSacCode;
    private String specifications;
    private Boolean active;

    public UpdateItemRequest() {}

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
    public Integer getMinimumStock() { return minimumStock; }
    public void setMinimumStock(Integer minimumStock) { this.minimumStock = minimumStock; }
    public Integer getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(Integer reorderLevel) { this.reorderLevel = reorderLevel; }
    public Integer getMaximumStock() { return maximumStock; }
    public void setMaximumStock(Integer maximumStock) { this.maximumStock = maximumStock; }
    public String getHsnSacCode() { return hsnSacCode; }
    public void setHsnSacCode(String hsnSacCode) { this.hsnSacCode = hsnSacCode; }
    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
