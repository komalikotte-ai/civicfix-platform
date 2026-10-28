package com.civicfix.platform.workflow.repository;

import com.civicfix.platform.workflow.entity.WorkflowTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransition, Long> {
    List<WorkflowTransition> findByComplaintIdOrderByCreatedAtAsc(Long complaintId);
}
