package com.civicfix.platform.assets.repository;

import com.civicfix.platform.assets.entity.AssetInspection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetInspectionRepository extends JpaRepository<AssetInspection, Long> {

    List<AssetInspection> findByAssetIdOrderByInspectionDateDesc(Long assetId);

    Page<AssetInspection> findByAssetId(Long assetId, Pageable pageable);

    List<AssetInspection> findByWorkOrderId(Long workOrderId);

    Page<AssetInspection> findByInspectorId(Long inspectorId, Pageable pageable);
}
