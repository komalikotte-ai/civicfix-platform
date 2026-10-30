package com.civicfix.platform.procurement.repository;

import com.civicfix.platform.procurement.entity.VendorContract;
import com.civicfix.platform.procurement.enums.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorContractRepository extends JpaRepository<VendorContract, Long> {

    Optional<VendorContract> findByContractNumber(String contractNumber);

    boolean existsByContractNumber(String contractNumber);

    List<VendorContract> findBySupplierId(Long supplierId);

    List<VendorContract> findByDepartmentId(Long departmentId);

    @Query("SELECT vc FROM VendorContract vc WHERE " +
            "(:supplierId IS NULL OR vc.supplier.id = :supplierId) AND " +
            "(:departmentId IS NULL OR vc.department.id = :departmentId) AND " +
            "(:status IS NULL OR vc.status = :status)")
    Page<VendorContract> searchContracts(
            @Param("supplierId") Long supplierId,
            @Param("departmentId") Long departmentId,
            @Param("status") ContractStatus status,
            Pageable pageable
    );
}
