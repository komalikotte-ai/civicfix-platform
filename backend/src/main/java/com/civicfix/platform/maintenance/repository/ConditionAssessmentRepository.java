package com.civicfix.platform.maintenance.repository;

import com.civicfix.platform.assets.enums.ConditionRating;
import com.civicfix.platform.maintenance.entity.ConditionAssessment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConditionAssessmentRepository extends JpaRepository<ConditionAssessment, Long> {

    Optional<ConditionAssessment> findByAssessmentCode(String assessmentCode);

    boolean existsByAssessmentCode(String assessmentCode);

    List<ConditionAssessment> findByAssetIdOrderByAssessmentDateDesc(Long assetId);

    Optional<ConditionAssessment> findFirstByAssetIdOrderByAssessmentDateDesc(Long assetId);

    @Query("SELECT ca FROM ConditionAssessment ca WHERE " +
            "(:assetId IS NULL OR ca.asset.id = :assetId) AND " +
            "(:overallCondition IS NULL OR ca.overallCondition = :overallCondition)")
    Page<ConditionAssessment> searchAssessments(
            @Param("assetId") Long assetId,
            @Param("overallCondition") ConditionRating overallCondition,
            Pageable pageable
    );
}
