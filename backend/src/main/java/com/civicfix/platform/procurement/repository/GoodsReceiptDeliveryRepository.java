package com.civicfix.platform.procurement.repository;

import com.civicfix.platform.procurement.entity.GoodsReceiptDelivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoodsReceiptDeliveryRepository extends JpaRepository<GoodsReceiptDelivery, Long> {

    Optional<GoodsReceiptDelivery> findByDeliveryNumber(String deliveryNumber);

    boolean existsByDeliveryNumber(String deliveryNumber);

    List<GoodsReceiptDelivery> findByPurchaseOrderId(Long purchaseOrderId);

    Page<GoodsReceiptDelivery> findByWarehouseIdOrderByReceivedAtDesc(Long warehouseId, Pageable pageable);
}
