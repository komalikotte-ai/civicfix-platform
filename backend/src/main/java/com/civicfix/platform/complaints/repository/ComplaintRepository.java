package com.civicfix.platform.complaints.repository;

import com.civicfix.platform.complaints.entity.Complaint;
import com.civicfix.platform.complaints.enums.ComplaintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    Optional<Complaint> findByTrackingNumber(String trackingNumber);

    Page<Complaint> findByOrganizationId(Long organizationId, Pageable pageable);

    Page<Complaint> findByCitizenId(Long citizenId, Pageable pageable);

    Page<Complaint> findByDepartmentId(Long departmentId, Pageable pageable);

    Page<Complaint> findByWardId(Long wardId, Pageable pageable);

    Page<Complaint> findByStatus(ComplaintStatus status, Pageable pageable);

    Page<Complaint> findByAssignedOfficerId(Long officerId, Pageable pageable);
}
