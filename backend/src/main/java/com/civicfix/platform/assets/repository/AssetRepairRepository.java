package com.civicfix.platform.assets.repository;

import com.civicfix.platform.assets.entity.AssetRepair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepairRepository extends JpaRepository<AssetRepair, Long> {

    List<AssetRepair> findByAssetIdOrderByRepairDateDesc(Long assetId);

    Page<AssetRepair> findByAssetId(Long assetId, Pageable pageable);

    List<AssetRepair> findByWorkOrderId(Long workOrderId);

    List<AssetRepair> findByComplaintId(Long complaintId);

    Page<AssetRepair> findByRepairedById(Long repairedById, Pageable pageable);
}
