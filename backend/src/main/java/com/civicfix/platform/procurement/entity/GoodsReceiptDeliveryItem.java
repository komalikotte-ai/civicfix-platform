package com.civicfix.platform.procurement.entity;

import com.civicfix.platform.inventory.entity.InventoryItem;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "goods_receipt_delivery_items")
public class GoodsReceiptDeliveryItem implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", nullable = false)
    private GoodsReceiptDelivery delivery;

    @Column(name = "po_item_id", nullable = false)
    private Long poItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private InventoryItem item;

    @Column(name = "quantity_delivered", nullable = false)
    private int quantityDelivered;

    @Column(name = "quantity_accepted", nullable = false)
    private int quantityAccepted;

    @Column(name = "quantity_rejected", nullable = false)
    private int quantityRejected = 0;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public GoodsReceiptDeliveryItem() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public GoodsReceiptDelivery getDelivery() { return delivery; }
    public void setDelivery(GoodsReceiptDelivery delivery) { this.delivery = delivery; }
    public Long getPoItemId() { return poItemId; }
    public void setPoItemId(Long poItemId) { this.poItemId = poItemId; }
    public InventoryItem getItem() { return item; }
    public void setItem(InventoryItem item) { this.item = item; }
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
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
