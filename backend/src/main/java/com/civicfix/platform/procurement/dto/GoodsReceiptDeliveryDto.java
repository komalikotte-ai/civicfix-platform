package com.civicfix.platform.procurement.dto;

import com.civicfix.platform.procurement.entity.GoodsReceiptDelivery;
import com.civicfix.platform.procurement.enums.DeliveryStatus;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class GoodsReceiptDeliveryDto {

    private Long id;
    private String deliveryNumber;
    private Long purchaseOrderId;
    private String purchaseOrderNumber;
    private Long warehouseId;
    private String warehouseCode;
    private String receivedBy;
    private Instant receivedAt;
    private String deliveryChallanNo;
    private DeliveryStatus status;
    private String inspectionNotes;
    private List<GoodsReceiptDeliveryItemDto> items;

    public GoodsReceiptDeliveryDto() {}

    public static GoodsReceiptDeliveryDto fromEntity(GoodsReceiptDelivery entity) {
        if (entity == null) return null;
        GoodsReceiptDeliveryDto dto = new GoodsReceiptDeliveryDto();
        dto.setId(entity.getId());
        dto.setDeliveryNumber(entity.getDeliveryNumber());
        if (entity.getPurchaseOrder() != null) {
            dto.setPurchaseOrderId(entity.getPurchaseOrder().getId());
            dto.setPurchaseOrderNumber(entity.getPurchaseOrder().getOrderNumber());
        }
        if (entity.getWarehouse() != null) {
            dto.setWarehouseId(entity.getWarehouse().getId());
            dto.setWarehouseCode(entity.getWarehouse().getWarehouseCode());
        }
        dto.setReceivedBy(entity.getReceivedBy());
        dto.setReceivedAt(entity.getReceivedAt());
        dto.setDeliveryChallanNo(entity.getDeliveryChallanNo());
        dto.setStatus(entity.getStatus());
        dto.setInspectionNotes(entity.getInspectionNotes());
        if (entity.getItems() != null) {
            dto.setItems(entity.getItems().stream().map(GoodsReceiptDeliveryItemDto::fromEntity).collect(Collectors.toList()));
        }
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDeliveryNumber() { return deliveryNumber; }
    public void setDeliveryNumber(String deliveryNumber) { this.deliveryNumber = deliveryNumber; }
    public Long getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(Long purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }
    public String getPurchaseOrderNumber() { return purchaseOrderNumber; }
    public void setPurchaseOrderNumber(String purchaseOrderNumber) { this.purchaseOrderNumber = purchaseOrderNumber; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getWarehouseCode() { return warehouseCode; }
    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }
    public String getReceivedBy() { return receivedBy; }
    public void setReceivedBy(String receivedBy) { this.receivedBy = receivedBy; }
    public Instant getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Instant receivedAt) { this.receivedAt = receivedAt; }
    public String getDeliveryChallanNo() { return deliveryChallanNo; }
    public void setDeliveryChallanNo(String deliveryChallanNo) { this.deliveryChallanNo = deliveryChallanNo; }
    public DeliveryStatus getStatus() { return status; }
    public void setStatus(DeliveryStatus status) { this.status = status; }
    public String getInspectionNotes() { return inspectionNotes; }
    public void setInspectionNotes(String inspectionNotes) { this.inspectionNotes = inspectionNotes; }
    public List<GoodsReceiptDeliveryItemDto> getItems() { return items; }
    public void setItems(List<GoodsReceiptDeliveryItemDto> items) { this.items = items; }

    public static class GoodsReceiptDeliveryItemDto {
        private Long id;
        private Long poItemId;
        private Long itemId;
        private String itemCode;
        private String itemName;
        private int quantityDelivered;
        private int quantityAccepted;
        private int quantityRejected;
        private String rejectionReason;
        private String batchNumber;

        public GoodsReceiptDeliveryItemDto() {}

        public static GoodsReceiptDeliveryItemDto fromEntity(com.civicfix.platform.procurement.entity.GoodsReceiptDeliveryItem entity) {
            if (entity == null) return null;
            GoodsReceiptDeliveryItemDto dto = new GoodsReceiptDeliveryItemDto();
            dto.setId(entity.getId());
            dto.setPoItemId(entity.getPoItemId());
            if (entity.getItem() != null) {
                dto.setItemId(entity.getItem().getId());
                dto.setItemCode(entity.getItem().getItemCode());
                dto.setItemName(entity.getItem().getName());
            }
            dto.setQuantityDelivered(entity.getQuantityDelivered());
            dto.setQuantityAccepted(entity.getQuantityAccepted());
            dto.setQuantityRejected(entity.getQuantityRejected());
            dto.setRejectionReason(entity.getRejectionReason());
            dto.setBatchNumber(entity.getBatchNumber());
            return dto;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getPoItemId() { return poItemId; }
        public void setPoItemId(Long poItemId) { this.poItemId = poItemId; }
        public Long getItemId() { return itemId; }
        public void setItemId(Long itemId) { this.itemId = itemId; }
        public String getItemCode() { return itemCode; }
        public void setItemCode(String itemCode) { this.itemCode = itemCode; }
        public String getItemName() { return itemName; }
        public void setItemName(String itemName) { this.itemName = itemName; }
        public int getQuantityDelivered() { return quantityDelivered; }
        public void setQuantityDelivered(int quantityDelivered) { this.quantityDelivered = quantityDelivered; }
        public int getQuantityAccepted() { return quantityAccepted; }
        public void setQuantityAccepted(int quantityAccepted) { this.quantityAccepted = quantityAccepted; }
        public int getQuantityRejected() { return quantityRejected; }
        public void setQuantityRejected(int quantityRejected) { this.quantityRejected = quantityRejected; }
        public String getRejectionReason() { return rejectionReason; }
        public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
        public String getBatchNumber() { return batchNumber; }
        public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    }
}
