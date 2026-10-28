package com.civicfix.platform.users.repository;

import com.civicfix.platform.users.entity.Employee;
import com.civicfix.platform.users.enums.RoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeCode(String employeeCode);

    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByUserId(Long userId);

    boolean existsByEmployeeCode(String employeeCode);

    boolean existsByEmail(String email);

    List<Employee> findByDepartmentIdAndActiveTrue(Long departmentId);

    List<Employee> findByTeamIdAndActiveTrue(Long teamId);

    List<Employee> findBySupervisorIdAndActiveTrue(Long supervisorId);

    Page<Employee> findAllByActiveTrue(Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE " +
           "(:departmentId IS NULL OR e.department.id = :departmentId) AND " +
           "(:teamId IS NULL OR e.team.id = :teamId) AND " +
           "(:supervisorId IS NULL OR e.supervisor.id = :supervisorId) AND " +
           "(:role IS NULL OR e.role = :role) AND " +
           "(:active IS NULL OR e.active = :active) AND " +
           "(:query IS NULL OR LOWER(e.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(e.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Employee> searchEmployees(@Param("departmentId") Long departmentId,
                                   @Param("teamId") Long teamId,
                                   @Param("supervisorId") Long supervisorId,
                                   @Param("role") RoleType role,
                                   @Param("active") Boolean active,
                                   @Param("query") String query,
                                   Pageable pageable);
}
