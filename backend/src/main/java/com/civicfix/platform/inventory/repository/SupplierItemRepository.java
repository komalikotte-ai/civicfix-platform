package com.civicfix.platform.inventory.repository;

import com.civicfix.platform.inventory.entity.SupplierItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierItemRepository extends JpaRepository<SupplierItem, Long> {

    Optional<SupplierItem> findBySupplierIdAndItemId(Long supplierId, Long itemId);

    List<SupplierItem> findBySupplierId(Long supplierId);

    List<SupplierItem> findByItemId(Long itemId);

    List<SupplierItem> findByItemIdAndPreferredTrue(Long itemId);
}
