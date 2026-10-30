package com.civicfix.platform.inventory.repository;

import com.civicfix.platform.inventory.entity.WarehouseStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseStockRepository extends JpaRepository<WarehouseStock, Long> {

    Optional<WarehouseStock> findByWarehouseIdAndItemId(Long warehouseId, Long itemId);

    Page<WarehouseStock> findByWarehouseId(Long warehouseId, Pageable pageable);

    Page<WarehouseStock> findByItemId(Long itemId, Pageable pageable);

    @Query("SELECT ws FROM WarehouseStock ws WHERE ws.warehouse.id = :warehouseId " +
            "AND ws.quantity <= ws.item.reorderLevel")
    Page<WarehouseStock> findLowStockByWarehouseId(@Param("warehouseId") Long warehouseId, Pageable pageable);

    @Query("SELECT ws FROM WarehouseStock ws WHERE ws.warehouse.id = :warehouseId " +
            "AND ws.quantity <= ws.item.minimumStock")
    Page<WarehouseStock> findBelowMinimumStockByWarehouseId(@Param("warehouseId") Long warehouseId, Pageable pageable);

    @Query("SELECT ws FROM WarehouseStock ws WHERE ws.quantity <= ws.item.reorderLevel")
    List<WarehouseStock> findAllLowStock();

    @Query("SELECT ws FROM WarehouseStock ws WHERE ws.quantity <= ws.item.minimumStock")
    List<WarehouseStock> findAllBelowMinimumStock();
}
