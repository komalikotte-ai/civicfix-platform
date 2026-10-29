package com.civicfix.platform.departments.repository;

import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.departments.enums.DepartmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findByOrganizationIdAndActiveTrue(Long organizationId);

    Page<Department> findByOrganizationId(Long organizationId, Pageable pageable);

    Optional<Department> findByOrganizationIdAndCode(Long organizationId, String code);

    boolean existsByOrganizationIdAndCode(Long organizationId, String code);

    List<Department> findByMunicipalityIdAndActiveTrue(Long municipalityId);

    Page<Department> findByMunicipalityId(Long municipalityId, Pageable pageable);

    Optional<Department> findByMunicipalityIdAndCode(Long municipalityId, String code);

    boolean existsByMunicipalityIdAndCode(Long municipalityId, String code);

    Optional<Department> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT d FROM Department d WHERE " +
           "(:municipalityId IS NULL OR d.municipality.id = :municipalityId) AND " +
           "(:status IS NULL OR d.status = :status) AND " +
           "(:active IS NULL OR d.active = :active) AND " +
           "(:query IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(d.code) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Department> searchDepartments(@Param("municipalityId") Long municipalityId,
                                       @Param("status") DepartmentStatus status,
                                       @Param("active") Boolean active,
                                       @Param("query") String query,
                                       Pageable pageable);
}
