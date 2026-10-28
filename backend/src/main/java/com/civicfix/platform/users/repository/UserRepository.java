package com.civicfix.platform.users.repository;

import com.civicfix.platform.users.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Page<User> findAllByActiveTrue(Pageable pageable);

    Page<User> findByOrganizationIdAndActiveTrue(Long organizationId, Pageable pageable);

    Page<User> findByDepartmentIdAndActiveTrue(Long departmentId, Pageable pageable);
}
