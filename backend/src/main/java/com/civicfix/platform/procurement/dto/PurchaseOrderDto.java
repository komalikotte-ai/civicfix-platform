package com.civicfix.platform.procurement.dto;

import com.civicfix.platform.procurement.entity.PurchaseOrder;
import com.civicfix.platform.procurement.enums.PurchaseOrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class PurchaseOrderDto {

    private Long id;
    private String orderNumber;
    private Long purchaseRequestId;
    private Long quotationId;
    private Long supplierId;
    private String supplierCode;
    private String supplierName;
    private Long departmentId;
    private String departmentName;
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private Long contractId;
    private LocalDate orderDate;
    private LocalDate expectedDelivery;
    private BigDecimal totalAmount;
    private PurchaseOrderStatus status;
    private String approvedBy;
    private Instant approvedAt;
    private String rejectionReason;
    private String termsAndConditions;
    private List<PurchaseOrderItemDto> items;
    private Instant createdAt;

    public PurchaseOrderDto() {}

    public static PurchaseOrderDto fromEntity(PurchaseOrder entity) {
        if (entity == null) return null;
        PurchaseOrderDto dto = new PurchaseOrderDto();
        dto.setId(entity.getId());
        dto.setOrderNumber(entity.getOrderNumber());
        if (entity.getPurchaseRequest() != null) dto.setPurchaseRequestId(entity.getPurchaseRequest().getId());
        if (entity.getQuotation() != null) dto.setQuotationId(entity.getQuotation().getId());
        if (entity.getSupplier() != null) {
            dto.setSupplierId(entity.getSupplier().getId());
            dto.setSupplierCode(entity.getSupplier().getSupplierCode());
            dto.setSupplierName(entity.getSupplier().getName());
        }
        if (entity.getDepartment() != null) {
            dto.setDepartmentId(entity.getDepartment().getId());
            dto.setDepartmentName(entity.getDepartment().getName());
        }
        if (entity.getWarehouse() != null) {
            dto.setWarehouseId(entity.getWarehouse().getId());
            dto.setWarehouseCode(entity.getWarehouse().getWarehouseCode());
            dto.setWarehouseName(entity.getWarehouse().getName());
        }
        if (entity.getContract() != null) dto.setContractId(entity.getContract().getId());
        dto.setOrderDate(entity.getOrderDate());
        dto.setExpectedDelivery(entity.getExpectedDelivery());
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setStatus(entity.getStatus());
        dto.setApprovedBy(entity.getApprovedBy());
        dto.setApprovedAt(entity.getApprovedAt());
        dto.setRejectionReason(entity.getRejectionReason());
        dto.setTermsAndConditions(entity.getTermsAndConditions());
        dto.setCreatedAt(entity.getCreatedAt());
        if (entity.getItems() != null) {
            dto.setItems(entity.getItems().stream().map(PurchaseOrderItemDto::fromEntity).collect(Collectors.toList()));
        }
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public Long getPurchaseRequestId() { return purchaseRequestId; }
    public void setPurchaseRequestId(Long purchaseRequestId) { this.purchaseRequestId = purchaseRequestId; }
    public Long getQuotationId() { return quotationId; }
    public void setQuotationId(Long quotationId) { this.quotationId = quotationId; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getWarehouseCode() { return warehouseCode; }
    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }
    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }
    public LocalDate getExpectedDelivery() { return expectedDelivery; }
    public void setExpectedDelivery(LocalDate expectedDelivery) { this.expectedDelivery = expectedDelivery; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public PurchaseOrderStatus getStatus() { return status; }
    public void setStatus(PurchaseOrderStatus status) { this.status = status; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public String getTermsAndConditions() { return termsAndConditions; }
    public void setTermsAndConditions(String termsAndConditions) { this.termsAndConditions = termsAndConditions; }
    public List<PurchaseOrderItemDto> getItems() { return items; }
    public void setItems(List<PurchaseOrderItemDto> items) { this.items = items; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static class PurchaseOrderItemDto {
        private Long id;
        private Long itemId;
        private String itemCode;
        private String itemName;
        private int quantityOrdered;
        private int quantityReceived;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;

        public PurchaseOrderItemDto() {}

        public static PurchaseOrderItemDto fromEntity(com.civicfix.platform.procurement.entity.PurchaseOrderItem entity) {
            if (entity == null) return null;
            PurchaseOrderItemDto dto = new PurchaseOrderItemDto();
            dto.setId(entity.getId());
            if (entity.getItem() != null) {
                dto.setItemId(entity.getItem().getId());
                dto.setItemCode(entity.getItem().getItemCode());
                dto.setItemName(entity.getItem().getName());
            }
            dto.setQuantityOrdered(entity.getQuantityOrdered());
            dto.setQuantityReceived(entity.getQuantityReceived());
            dto.setUnitPrice(entity.getUnitPrice());
            dto.setTotalPrice(entity.getTotalPrice());
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
        public int getQuantityOrdered() { return quantityOrdered; }
        public void setQuantityOrdered(int quantityOrdered) { this.quantityOrdered = quantityOrdered; }
        public int getQuantityReceived() { return quantityReceived; }
        public void setQuantityReceived(int quantityReceived) { this.quantityReceived = quantityReceived; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public BigDecimal getTotalPrice() { return totalPrice; }
        public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    }
}
