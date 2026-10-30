package com.civicfix.platform.inventory.repository;

import com.civicfix.platform.inventory.entity.Warehouse;
import com.civicfix.platform.inventory.enums.WarehouseStatus;
import com.civicfix.platform.inventory.enums.WarehouseType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    Optional<Warehouse> findByWarehouseCode(String warehouseCode);

    boolean existsByWarehouseCode(String warehouseCode);

    List<Warehouse> findByDepartmentId(Long departmentId);

    List<Warehouse> findByOrganizationId(Long organizationId);

    List<Warehouse> findByStatus(WarehouseStatus status);

    @Query("SELECT w FROM Warehouse w WHERE " +
            "(:keyword IS NULL OR LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(w.warehouseCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR (w.address IS NOT NULL AND LOWER(w.address) LIKE LOWER(CONCAT('%', :keyword, '%')))) AND " +
            "(:departmentId IS NULL OR w.department.id = :departmentId) AND " +
            "(:organizationId IS NULL OR w.organization.id = :organizationId) AND " +
            "(:status IS NULL OR w.status = :status) AND " +
            "(:warehouseType IS NULL OR w.warehouseType = :warehouseType)")
    Page<Warehouse> searchWarehouses(
            @Param("keyword") String keyword,
            @Param("departmentId") Long departmentId,
            @Param("organizationId") Long organizationId,
            @Param("status") WarehouseStatus status,
            @Param("warehouseType") WarehouseType warehouseType,
            Pageable pageable
    );
}
