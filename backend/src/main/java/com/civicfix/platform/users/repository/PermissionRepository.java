package com.civicfix.platform.users.repository;

import com.civicfix.platform.users.entity.Permission;
import com.civicfix.platform.users.enums.PermissionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(PermissionType name);
}
