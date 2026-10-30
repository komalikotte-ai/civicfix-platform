package com.civicfix.platform.inventory.repository;

import com.civicfix.platform.inventory.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findBySupplierCode(String supplierCode);

    boolean existsBySupplierCode(String supplierCode);

    @Query("SELECT s FROM Supplier s WHERE " +
            "(:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(s.supplierCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR (s.contactPerson IS NOT NULL AND LOWER(s.contactPerson) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "   OR (s.email IS NOT NULL AND LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%')))) AND " +
            "(:active IS NULL OR s.active = :active)")
    Page<Supplier> searchSuppliers(
            @Param("keyword") String keyword,
            @Param("active") Boolean active,
            Pageable pageable
    );
}
