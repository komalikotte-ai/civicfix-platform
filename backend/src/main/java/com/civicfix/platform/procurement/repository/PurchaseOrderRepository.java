package com.civicfix.platform.procurement.repository;

import com.civicfix.platform.procurement.entity.PurchaseOrder;
import com.civicfix.platform.procurement.enums.PurchaseOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    Optional<PurchaseOrder> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

    List<PurchaseOrder> findBySupplierId(Long supplierId);

    List<PurchaseOrder> findByWarehouseId(Long warehouseId);

    @Query("SELECT po FROM PurchaseOrder po WHERE " +
            "(:supplierId IS NULL OR po.supplier.id = :supplierId) AND " +
            "(:departmentId IS NULL OR po.department.id = :departmentId) AND " +
            "(:warehouseId IS NULL OR po.warehouse.id = :warehouseId) AND " +
            "(:status IS NULL OR po.status = :status)")
    Page<PurchaseOrder> searchPurchaseOrders(
            @Param("supplierId") Long supplierId,
            @Param("departmentId") Long departmentId,
            @Param("warehouseId") Long warehouseId,
            @Param("status") PurchaseOrderStatus status,
            Pageable pageable
    );
}
