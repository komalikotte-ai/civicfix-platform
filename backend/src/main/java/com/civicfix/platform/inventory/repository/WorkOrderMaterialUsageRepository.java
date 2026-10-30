package com.civicfix.platform.inventory.repository;

import com.civicfix.platform.inventory.entity.WorkOrderMaterialUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkOrderMaterialUsageRepository extends JpaRepository<WorkOrderMaterialUsage, Long> {

    List<WorkOrderMaterialUsage> findByWorkOrderIdOrderByRecordedAtDesc(Long workOrderId);

    List<WorkOrderMaterialUsage> findByItemId(Long itemId);

    List<WorkOrderMaterialUsage> findByWarehouseId(Long warehouseId);
}
