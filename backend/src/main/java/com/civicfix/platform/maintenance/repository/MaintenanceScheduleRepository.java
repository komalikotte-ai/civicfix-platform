package com.civicfix.platform.maintenance.repository;

import com.civicfix.platform.maintenance.entity.MaintenanceSchedule;
import com.civicfix.platform.maintenance.enums.ScheduleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceScheduleRepository extends JpaRepository<MaintenanceSchedule, Long> {

    Optional<MaintenanceSchedule> findByScheduleCode(String scheduleCode);

    boolean existsByScheduleCode(String scheduleCode);

    List<MaintenanceSchedule> findByAssetId(Long assetId);

    List<MaintenanceSchedule> findByDepartmentId(Long departmentId);

    List<MaintenanceSchedule> findByStatusAndNextDueDateLessThanEqual(ScheduleStatus status, Instant dueThreshold);

    @Query("SELECT ms FROM MaintenanceSchedule ms WHERE " +
            "(:keyword IS NULL OR LOWER(ms.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(ms.scheduleCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:assetId IS NULL OR ms.asset.id = :assetId) AND " +
            "(:departmentId IS NULL OR ms.department.id = :departmentId) AND " +
            "(:status IS NULL OR ms.status = :status)")
    Page<MaintenanceSchedule> searchSchedules(
            @Param("keyword") String keyword,
            @Param("assetId") Long assetId,
            @Param("departmentId") Long departmentId,
            @Param("status") ScheduleStatus status,
            Pageable pageable
    );
}
