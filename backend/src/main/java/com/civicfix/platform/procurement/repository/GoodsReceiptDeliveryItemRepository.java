package com.civicfix.platform.procurement.repository;

import com.civicfix.platform.procurement.entity.GoodsReceiptDeliveryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoodsReceiptDeliveryItemRepository extends JpaRepository<GoodsReceiptDeliveryItem, Long> {
    List<GoodsReceiptDeliveryItem> findByDeliveryId(Long deliveryId);
}
