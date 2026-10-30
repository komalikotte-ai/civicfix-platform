package com.civicfix.platform.workorders.repository;

import com.civicfix.platform.workorders.entity.WorkOrderAssignmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkOrderAssignmentHistoryRepository extends JpaRepository<WorkOrderAssignmentHistory, Long> {

    List<WorkOrderAssignmentHistory> findByWorkOrderIdOrderByAssignedAtAsc(Long workOrderId);
}
