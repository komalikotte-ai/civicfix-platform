package com.civicfix.platform.maintenance.repository;

import com.civicfix.platform.assets.enums.AssetType;
import com.civicfix.platform.maintenance.entity.InspectionPlan;
import com.civicfix.platform.maintenance.enums.PlanType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InspectionPlanRepository extends JpaRepository<InspectionPlan, Long> {

    Optional<InspectionPlan> findByPlanCode(String planCode);

    boolean existsByPlanCode(String planCode);

    List<InspectionPlan> findByDepartmentId(Long departmentId);

    List<InspectionPlan> findByTargetAssetTypeAndActiveTrue(AssetType targetAssetType);

    @Query("SELECT ip FROM InspectionPlan ip WHERE " +
            "(:keyword IS NULL OR LOWER(ip.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(ip.planCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:departmentId IS NULL OR ip.department.id = :departmentId) AND " +
            "(:planType IS NULL OR ip.planType = :planType) AND " +
            "(:targetAssetType IS NULL OR ip.targetAssetType = :targetAssetType) AND " +
            "(:active IS NULL OR ip.active = :active)")
    Page<InspectionPlan> searchPlans(
            @Param("keyword") String keyword,
            @Param("departmentId") Long departmentId,
            @Param("planType") PlanType planType,
            @Param("targetAssetType") AssetType targetAssetType,
            @Param("active") Boolean active,
            Pageable pageable
    );
}
