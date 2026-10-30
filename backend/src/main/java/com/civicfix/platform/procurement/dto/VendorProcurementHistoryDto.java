package com.civicfix.platform.procurement.dto;

import java.math.BigDecimal;
import java.util.List;

public class VendorProcurementHistoryDto {

    private VendorDto vendor;
    private List<VendorContractDto> contracts;
    private List<PurchaseOrderDto> purchaseOrders;
    private List<GoodsReceiptDeliveryDto> deliveries;
    private List<VendorEvaluationDto> evaluations;
    private int totalOrders;
    private BigDecimal totalSpend;
    private BigDecimal averageRating;

    public VendorProcurementHistoryDto() {}

    public VendorDto getVendor() { return vendor; }
    public void setVendor(VendorDto vendor) { this.vendor = vendor; }
    public List<VendorContractDto> getContracts() { return contracts; }
    public void setContracts(List<VendorContractDto> contracts) { this.contracts = contracts; }
    public List<PurchaseOrderDto> getPurchaseOrders() { return purchaseOrders; }
    public void setPurchaseOrders(List<PurchaseOrderDto> purchaseOrders) { this.purchaseOrders = purchaseOrders; }
    public List<GoodsReceiptDeliveryDto> getDeliveries() { return deliveries; }
    public void setDeliveries(List<GoodsReceiptDeliveryDto> deliveries) { this.deliveries = deliveries; }
    public List<VendorEvaluationDto> getEvaluations() { return evaluations; }
    public void setEvaluations(List<VendorEvaluationDto> evaluations) { this.evaluations = evaluations; }
    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }
    public BigDecimal getTotalSpend() { return totalSpend; }
    public void setTotalSpend(BigDecimal totalSpend) { this.totalSpend = totalSpend; }
    public BigDecimal getAverageRating() { return averageRating; }
    public void setAverageRating(BigDecimal averageRating) { this.averageRating = averageRating; }
}
