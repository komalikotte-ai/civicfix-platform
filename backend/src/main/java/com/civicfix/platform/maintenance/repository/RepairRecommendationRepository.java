package com.civicfix.platform.maintenance.repository;

import com.civicfix.platform.maintenance.entity.RepairRecommendation;
import com.civicfix.platform.maintenance.enums.RecommendationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepairRecommendationRepository extends JpaRepository<RepairRecommendation, Long> {

    Optional<RepairRecommendation> findByRecommendationCode(String recommendationCode);

    boolean existsByRecommendationCode(String recommendationCode);

    List<RepairRecommendation> findByAssetId(Long assetId);

    @Query("SELECT rr FROM RepairRecommendation rr WHERE " +
            "(:assetId IS NULL OR rr.asset.id = :assetId) AND " +
            "(:status IS NULL OR rr.status = :status)")
    Page<RepairRecommendation> searchRecommendations(
            @Param("assetId") Long assetId,
            @Param("status") RecommendationStatus status,
            Pageable pageable
    );
}
