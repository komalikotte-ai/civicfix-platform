package com.civicfix.platform.wards.repository;

import com.civicfix.platform.wards.entity.Ward;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WardRepository extends JpaRepository<Ward, Long> {

    List<Ward> findByOrganizationIdAndActiveTrue(Long organizationId);

    Page<Ward> findByOrganizationId(Long organizationId, Pageable pageable);

    Optional<Ward> findByOrganizationIdAndWardNumber(Long organizationId, String wardNumber);

    boolean existsByOrganizationIdAndWardNumber(Long organizationId, String wardNumber);

    List<Ward> findByMunicipalityIdAndActiveTrue(Long municipalityId);

    Page<Ward> findByMunicipalityId(Long municipalityId, Pageable pageable);

    Optional<Ward> findByMunicipalityIdAndWardNumber(Long municipalityId, String wardNumber);

    boolean existsByMunicipalityIdAndWardNumber(Long municipalityId, String wardNumber);

    @Query("SELECT w FROM Ward w WHERE " +
           "(:municipalityId IS NULL OR w.municipality.id = :municipalityId) AND " +
           "(:organizationId IS NULL OR w.organization.id = :organizationId) AND " +
           "(:zoneName IS NULL OR LOWER(w.zoneName) = LOWER(:zoneName)) AND " +
           "(:active IS NULL OR w.active = :active) AND " +
           "(:query IS NULL OR LOWER(w.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(w.wardNumber) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Ward> searchWards(@Param("municipalityId") Long municipalityId,
                           @Param("organizationId") Long organizationId,
                           @Param("zoneName") String zoneName,
                           @Param("active") Boolean active,
                           @Param("query") String query,
                           Pageable pageable);
}
