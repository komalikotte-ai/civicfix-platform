package com.civicfix.platform.departments.repository;

import com.civicfix.platform.departments.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findByDepartmentIdAndActiveTrue(Long departmentId);

    Page<Team> findByDepartmentId(Long departmentId, Pageable pageable);

    Optional<Team> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByDepartmentIdAndCode(Long departmentId, String code);

    List<Team> findBySupervisorIdAndActiveTrue(Long supervisorId);

    @Query("SELECT t FROM Team t WHERE " +
           "(:departmentId IS NULL OR t.department.id = :departmentId) AND " +
           "(:supervisorId IS NULL OR t.supervisor.id = :supervisorId) AND " +
           "(:active IS NULL OR t.active = :active) AND " +
           "(:query IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(t.code) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Team> searchTeams(@Param("departmentId") Long departmentId,
                           @Param("supervisorId") Long supervisorId,
                           @Param("active") Boolean active,
                           @Param("query") String query,
                           Pageable pageable);
}
