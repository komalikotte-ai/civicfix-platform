package com.civicfix.platform.inventory.dto;

import java.math.BigDecimal;
import java.util.List;

public class InventorySummaryReportDto {

    private long totalWarehouses;
    private long totalItems;
    private long totalLowStockItems;
    private long totalBelowMinimumStockItems;
    private BigDecimal totalInventoryValuation;
    private List<WarehouseStockSummaryDto> warehouseSummaries;

    public InventorySummaryReportDto() {}

    public long getTotalWarehouses() { return totalWarehouses; }
    public void setTotalWarehouses(long totalWarehouses) { this.totalWarehouses = totalWarehouses; }
    public long getTotalItems() { return totalItems; }
    public void setTotalItems(long totalItems) { this.totalItems = totalItems; }
    public long getTotalLowStockItems() { return totalLowStockItems; }
    public void setTotalLowStockItems(long totalLowStockItems) { this.totalLowStockItems = totalLowStockItems; }
    public long getTotalBelowMinimumStockItems() { return totalBelowMinimumStockItems; }
    public void setTotalBelowMinimumStockItems(long totalBelowMinimumStockItems) { this.totalBelowMinimumStockItems = totalBelowMinimumStockItems; }
    public BigDecimal getTotalInventoryValuation() { return totalInventoryValuation; }
    public void setTotalInventoryValuation(BigDecimal totalInventoryValuation) { this.totalInventoryValuation = totalInventoryValuation; }
    public List<WarehouseStockSummaryDto> getWarehouseSummaries() { return warehouseSummaries; }
    public void setWarehouseSummaries(List<WarehouseStockSummaryDto> warehouseSummaries) { this.warehouseSummaries = warehouseSummaries; }

    public static class WarehouseStockSummaryDto {
        private Long warehouseId;
        private String warehouseCode;
        private String warehouseName;
        private int totalStockItems;
        private int totalUnits;
        private int totalReservedUnits;
        private int lowStockItemsCount;
        private BigDecimal totalValuation;

        public WarehouseStockSummaryDto() {}

        public Long getWarehouseId() { return warehouseId; }
        public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
        public String getWarehouseCode() { return warehouseCode; }
        public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }
        public String getWarehouseName() { return warehouseName; }
        public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
        public int getTotalStockItems() { return totalStockItems; }
        public void setTotalStockItems(int totalStockItems) { this.totalStockItems = totalStockItems; }
        public int getTotalUnits() { return totalUnits; }
        public void setTotalUnits(int totalUnits) { this.totalUnits = totalUnits; }
        public int getTotalReservedUnits() { return totalReservedUnits; }
        public void setTotalReservedUnits(int totalReservedUnits) { this.totalReservedUnits = totalReservedUnits; }
        public int getLowStockItemsCount() { return lowStockItemsCount; }
        public void setLowStockItemsCount(int lowStockItemsCount) { this.lowStockItemsCount = lowStockItemsCount; }
        public BigDecimal getTotalValuation() { return totalValuation; }
        public void setTotalValuation(BigDecimal totalValuation) { this.totalValuation = totalValuation; }
    }
}
