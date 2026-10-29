package com.civicfix.platform.organizations.repository;

import com.civicfix.platform.organizations.entity.City;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {

    Optional<City> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByName(String name);

    Page<City> findAllByActiveTrue(Pageable pageable);

    List<City> findAllByActiveTrue();

    @Query("SELECT c FROM City c WHERE (:query IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.code) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.state) LIKE LOWER(CONCAT('%', :query, '%'))) AND (:active IS NULL OR c.active = :active)")
    Page<City> searchCities(@Param("query") String query, @Param("active") Boolean active, Pageable pageable);
}
