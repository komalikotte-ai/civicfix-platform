package com.civicfix.platform.users.repository;

import com.civicfix.platform.users.entity.EmployeeAssignmentHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeAssignmentHistoryRepository extends JpaRepository<EmployeeAssignmentHistory, Long> {

    List<EmployeeAssignmentHistory> findByEmployeeIdOrderByEffectiveFromDesc(Long employeeId);

    @Query("SELECT h FROM EmployeeAssignmentHistory h WHERE h.employee.id = :employeeId AND h.effectiveTo IS NULL")
    Optional<EmployeeAssignmentHistory> findActiveAssignment(@Param("employeeId") Long employeeId);

    Page<EmployeeAssignmentHistory> findByNewDepartmentId(Long departmentId, Pageable pageable);

    Page<EmployeeAssignmentHistory> findByNewTeamId(Long teamId, Pageable pageable);
}
