package com.civicfix.platform.organizations.repository;

import com.civicfix.platform.organizations.entity.Municipality;
import com.civicfix.platform.organizations.enums.MunicipalityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MunicipalityRepository extends JpaRepository<Municipality, Long> {

    Optional<Municipality> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByName(String name);

    Page<Municipality> findByCityIdAndActiveTrue(Long cityId, Pageable pageable);

    List<Municipality> findByCityIdAndActiveTrue(Long cityId);

    Page<Municipality> findAllByActiveTrue(Pageable pageable);

    @Query("SELECT m FROM Municipality m WHERE " +
           "(:cityId IS NULL OR m.city.id = :cityId) AND " +
           "(:type IS NULL OR m.type = :type) AND " +
           "(:active IS NULL OR m.active = :active) AND " +
           "(:query IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(m.code) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Municipality> searchMunicipalities(@Param("cityId") Long cityId,
                                            @Param("type") MunicipalityType type,
                                            @Param("active") Boolean active,
                                            @Param("query") String query,
                                            Pageable pageable);
}
