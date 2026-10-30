package com.civicfix.platform.assets.repository;

import com.civicfix.platform.assets.entity.Asset;
import com.civicfix.platform.assets.enums.AssetStatus;
import com.civicfix.platform.assets.enums.AssetType;
import com.civicfix.platform.assets.enums.ConditionRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    Optional<Asset> findByAssetCode(String assetCode);

    boolean existsByAssetCode(String assetCode);

    Page<Asset> findByDepartmentId(Long departmentId, Pageable pageable);

    Page<Asset> findByWardId(Long wardId, Pageable pageable);

    Page<Asset> findByAssetType(AssetType assetType, Pageable pageable);

    Page<Asset> findByStatus(AssetStatus status, Pageable pageable);

    @Query("SELECT a FROM Asset a WHERE " +
            "(:keyword IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(a.assetCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR (a.description IS NOT NULL AND LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "   OR (a.addressLine IS NOT NULL AND LOWER(a.addressLine) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "   OR (a.manufacturer IS NOT NULL AND LOWER(a.manufacturer) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "   OR (a.modelNumber IS NOT NULL AND LOWER(a.modelNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "   OR (a.serialNumber IS NOT NULL AND LOWER(a.serialNumber) LIKE LOWER(CONCAT('%', :keyword, '%')))) AND " +
            "(:assetType IS NULL OR a.assetType = :assetType) AND " +
            "(:status IS NULL OR a.status = :status) AND " +
            "(:conditionRating IS NULL OR a.conditionRating = :conditionRating) AND " +
            "(:departmentId IS NULL OR a.department.id = :departmentId) AND " +
            "(:wardId IS NULL OR (a.ward IS NOT NULL AND a.ward.id = :wardId)) AND " +
            "(:active IS NULL OR a.active = :active)")
    Page<Asset> searchAssets(
            @Param("keyword") String keyword,
            @Param("assetType") AssetType assetType,
            @Param("status") AssetStatus status,
            @Param("conditionRating") ConditionRating conditionRating,
            @Param("departmentId") Long departmentId,
            @Param("wardId") Long wardId,
            @Param("active") Boolean active,
            Pageable pageable
    );
}
