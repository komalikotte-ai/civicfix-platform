package com.civicfix.platform.maintenance.repository;

import com.civicfix.platform.maintenance.entity.AssetFailureLog;
import com.civicfix.platform.maintenance.enums.FailureSeverity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetFailureLogRepository extends JpaRepository<AssetFailureLog, Long> {

    Optional<AssetFailureLog> findByFailureCode(String failureCode);

    boolean existsByFailureCode(String failureCode);

    List<AssetFailureLog> findByAssetIdOrderByFailureDateDesc(Long assetId);

    @Query("SELECT fl FROM AssetFailureLog fl WHERE " +
            "(:assetId IS NULL OR fl.asset.id = :assetId) AND " +
            "(:severity IS NULL OR fl.severity = :severity)")
    Page<AssetFailureLog> searchFailureLogs(
            @Param("assetId") Long assetId,
            @Param("severity") FailureSeverity severity,
            Pageable pageable
    );
}
