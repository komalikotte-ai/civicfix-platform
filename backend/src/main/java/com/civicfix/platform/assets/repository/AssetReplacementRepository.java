package com.civicfix.platform.assets.repository;

import com.civicfix.platform.assets.entity.AssetReplacement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetReplacementRepository extends JpaRepository<AssetReplacement, Long> {

    List<AssetReplacement> findByAssetIdOrderByReplacementDateDesc(Long assetId);

    Page<AssetReplacement> findByAssetId(Long assetId, Pageable pageable);

    List<AssetReplacement> findByReplacedAssetId(Long replacedAssetId);
}
