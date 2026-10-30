package com.civicfix.platform.inventory.dto;

import java.util.Map;

public class ApprovePurchaseRequestRequest {

    /** Map of PurchaseRequestItem ID → approved quantity (null = approve as requested) */
    private Map<Long, Integer> approvedQuantities;
    private String remarks;

    public ApprovePurchaseRequestRequest() {}

    public Map<Long, Integer> getApprovedQuantities() { return approvedQuantities; }
    public void setApprovedQuantities(Map<Long, Integer> approvedQuantities) { this.approvedQuantities = approvedQuantities; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
