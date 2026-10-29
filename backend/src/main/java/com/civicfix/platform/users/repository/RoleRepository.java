package com.civicfix.platform.users.repository;

import com.civicfix.platform.users.entity.Role;
import com.civicfix.platform.users.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleType name);
}
