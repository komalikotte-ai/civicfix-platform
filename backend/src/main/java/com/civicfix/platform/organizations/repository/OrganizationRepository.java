package com.civicfix.platform.organizations.repository;

import com.civicfix.platform.organizations.entity.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findByCode(String code);
    boolean existsByCode(String code);
    Page<Organization> findAllByActiveTrue(Pageable pageable);
}
