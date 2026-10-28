package com.civicfix.platform.complaints.repository;

import com.civicfix.platform.complaints.entity.ComplaintAssignmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintAssignmentHistoryRepository extends JpaRepository<ComplaintAssignmentHistory, Long> {

    List<ComplaintAssignmentHistory> findByComplaintIdOrderByAssignedAtDesc(Long complaintId);
}
