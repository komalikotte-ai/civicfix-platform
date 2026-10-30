package com.civicfix.platform.inventory.repository;

import com.civicfix.platform.inventory.entity.InventoryItem;
import com.civicfix.platform.inventory.enums.ItemCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findByItemCode(String itemCode);

    boolean existsByItemCode(String itemCode);

    @Query("SELECT i FROM InventoryItem i WHERE " +
            "(:keyword IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(i.itemCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR (i.description IS NOT NULL AND LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')))) AND " +
            "(:itemCategory IS NULL OR i.itemCategory = :itemCategory) AND " +
            "(:active IS NULL OR i.active = :active)")
    Page<InventoryItem> searchItems(
            @Param("keyword") String keyword,
            @Param("itemCategory") ItemCategory itemCategory,
            @Param("active") Boolean active,
            Pageable pageable
    );
}
