package com.civicfix.platform.procurement.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class ReceiveDeliveryRequest {

    private String deliveryNumber;

    @NotNull(message = "Purchase order ID is required")
    private Long purchaseOrderId;

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;

    private String deliveryChallanNo;
    private String inspectionNotes;

    @NotNull(message = "Delivered items list cannot be null")
    @Size(min = 1, message = "At least one delivered item is required")
    private List<DeliveryItemRequest> items;

    public ReceiveDeliveryRequest() {}

    public String getDeliveryNumber() { return deliveryNumber; }
    public void setDeliveryNumber(String deliveryNumber) { this.deliveryNumber = deliveryNumber; }
    public Long getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(Long purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getDeliveryChallanNo() { return deliveryChallanNo; }
    public void setDeliveryChallanNo(String deliveryChallanNo) { this.deliveryChallanNo = deliveryChallanNo; }
    public String getInspectionNotes() { return inspectionNotes; }
    public void setInspectionNotes(String inspectionNotes) { this.inspectionNotes = inspectionNotes; }
    public List<DeliveryItemRequest> getItems() { return items; }
    public void setItems(List<DeliveryItemRequest> items) { this.items = items; }

    public static class DeliveryItemRequest {
        @NotNull(message = "PO item ID is required")
        private Long poItemId;

        @NotNull(message = "Item ID is required")
        private Long itemId;

        @NotNull(message = "Delivered quantity is required")
        private Integer quantityDelivered;

        @NotNull(message = "Accepted quantity is required")
        private Integer quantityAccepted;

        private Integer quantityRejected = 0;
        private String rejectionReason;
        private String batchNumber;

        public DeliveryItemRequest() {}

        public Long getPoItemId() { return poItemId; }
        public void setPoItemId(Long poItemId) { this.poItemId = poItemId; }
        public Long getItemId() { return itemId; }
        public void setItemId(Long itemId) { this.itemId = itemId; }
        public Integer getQuantityDelivered() { return quantityDelivered; }
        public void setQuantityDelivered(Integer quantityDelivered) { this.quantityDelivered = quantityDelivered; }
        public Integer getQuantityAccepted() { return quantityAccepted; }
        public void setQuantityAccepted(Integer quantityAccepted) { this.quantityAccepted = quantityAccepted; }
        public Integer getQuantityRejected() { return quantityRejected; }
        public void setQuantityRejected(Integer quantityRejected) { this.quantityRejected = quantityRejected; }
        public String getRejectionReason() { return rejectionReason; }
        public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
        public String getBatchNumber() { return batchNumber; }
        public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    }
}
