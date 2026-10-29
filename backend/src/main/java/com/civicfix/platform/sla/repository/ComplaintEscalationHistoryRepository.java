package com.civicfix.platform.sla.repository;

import com.civicfix.platform.sla.entity.ComplaintEscalationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintEscalationHistoryRepository extends JpaRepository<ComplaintEscalationHistory, Long> {

    List<ComplaintEscalationHistory> findByComplaintIdOrderByEscalatedAtDesc(Long complaintId);

    List<ComplaintEscalationHistory> findByComplaintIdOrderByEscalatedAtAsc(Long complaintId);
}
