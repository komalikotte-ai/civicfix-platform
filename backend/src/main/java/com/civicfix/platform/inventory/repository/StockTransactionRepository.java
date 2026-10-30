package com.civicfix.platform.inventory.repository;

import com.civicfix.platform.inventory.entity.StockTransaction;
import com.civicfix.platform.inventory.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {

    Page<StockTransaction> findByWarehouseIdOrderByTransactionDateDesc(Long warehouseId, Pageable pageable);

    Page<StockTransaction> findByItemIdOrderByTransactionDateDesc(Long itemId, Pageable pageable);

    List<StockTransaction> findByWorkOrderId(Long workOrderId);

    @Query("SELECT t FROM StockTransaction t WHERE " +
            "(:warehouseId IS NULL OR t.warehouse.id = :warehouseId) AND " +
            "(:itemId IS NULL OR t.item.id = :itemId) AND " +
            "(:transactionType IS NULL OR t.transactionType = :transactionType) AND " +
            "(:startDate IS NULL OR t.transactionDate >= :startDate) AND " +
            "(:endDate IS NULL OR t.transactionDate <= :endDate)")
    Page<StockTransaction> searchTransactions(
            @Param("warehouseId") Long warehouseId,
            @Param("itemId") Long itemId,
            @Param("transactionType") TransactionType transactionType,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );
}
