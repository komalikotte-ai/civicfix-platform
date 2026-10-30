package com.civicfix.platform.procurement.dto;

import com.civicfix.platform.procurement.entity.Quotation;
import com.civicfix.platform.procurement.enums.QuotationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class QuotationDto {

    private Long id;
    private String quotationNumber;
    private Long purchaseRequestId;
    private Long supplierId;
    private String supplierCode;
    private String supplierName;
    private Long departmentId;
    private String departmentName;
    private BigDecimal totalAmount;
    private QuotationStatus status;
    private LocalDate validUntil;
    private String deliveryTerms;
    private String paymentTerms;
    private String remarks;
    private List<QuotationItemDto> items;
    private Instant createdAt;

    public QuotationDto() {}

    public static QuotationDto fromEntity(Quotation entity) {
        if (entity == null) return null;
        QuotationDto dto = new QuotationDto();
        dto.setId(entity.getId());
        dto.setQuotationNumber(entity.getQuotationNumber());
        if (entity.getPurchaseRequest() != null) {
            dto.setPurchaseRequestId(entity.getPurchaseRequest().getId());
        }
        if (entity.getSupplier() != null) {
            dto.setSupplierId(entity.getSupplier().getId());
            dto.setSupplierCode(entity.getSupplier().getSupplierCode());
            dto.setSupplierName(entity.getSupplier().getName());
        }
        if (entity.getDepartment() != null) {
            dto.setDepartmentId(entity.getDepartment().getId());
            dto.setDepartmentName(entity.getDepartment().getName());
        }
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setStatus(entity.getStatus());
        dto.setValidUntil(entity.getValidUntil());
        dto.setDeliveryTerms(entity.getDeliveryTerms());
        dto.setPaymentTerms(entity.getPaymentTerms());
        dto.setRemarks(entity.getRemarks());
        dto.setCreatedAt(entity.getCreatedAt());
        if (entity.getItems() != null) {
            dto.setItems(entity.getItems().stream().map(QuotationItemDto::fromEntity).collect(Collectors.toList()));
        }
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getQuotationNumber() { return quotationNumber; }
    public void setQuotationNumber(String quotationNumber) { this.quotationNumber = quotationNumber; }
    public Long getPurchaseRequestId() { return purchaseRequestId; }
    public void setPurchaseRequestId(Long purchaseRequestId) { this.purchaseRequestId = purchaseRequestId; }
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
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public QuotationStatus getStatus() { return status; }
    public void setStatus(QuotationStatus status) { this.status = status; }
    public LocalDate getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDate validUntil) { this.validUntil = validUntil; }
    public String getDeliveryTerms() { return deliveryTerms; }
    public void setDeliveryTerms(String deliveryTerms) { this.deliveryTerms = deliveryTerms; }
    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public List<QuotationItemDto> getItems() { return items; }
    public void setItems(List<QuotationItemDto> items) { this.items = items; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static class QuotationItemDto {
        private Long id;
        private Long itemId;
        private String itemCode;
        private String itemName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private String specifications;

        public QuotationItemDto() {}

        public static QuotationItemDto fromEntity(com.civicfix.platform.procurement.entity.QuotationItem entity) {
            if (entity == null) return null;
            QuotationItemDto dto = new QuotationItemDto();
            dto.setId(entity.getId());
            if (entity.getItem() != null) {
                dto.setItemId(entity.getItem().getId());
                dto.setItemCode(entity.getItem().getItemCode());
                dto.setItemName(entity.getItem().getName());
            }
            dto.setQuantity(entity.getQuantity());
            dto.setUnitPrice(entity.getUnitPrice());
            dto.setTotalPrice(entity.getTotalPrice());
            dto.setSpecifications(entity.getSpecifications());
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
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public BigDecimal getTotalPrice() { return totalPrice; }
        public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
        public String getSpecifications() { return specifications; }
        public void setSpecifications(String specifications) { this.specifications = specifications; }
    }
}
