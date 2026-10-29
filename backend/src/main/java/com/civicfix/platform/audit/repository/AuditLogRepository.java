package com.civicfix.platform.audit.repository;

import com.civicfix.platform.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByEntityNameAndEntityId(String entityName, String entityId, Pageable pageable);

    List<AuditLog> findTop50ByOrderByTimestampDesc();
}
