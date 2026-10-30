package com.civicfix.platform.workorders.repository;

import com.civicfix.platform.workorders.entity.WorkOrderDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkOrderDependencyRepository extends JpaRepository<WorkOrderDependency, Long> {

    List<WorkOrderDependency> findByWorkOrderId(Long workOrderId);

    List<WorkOrderDependency> findByDependsOnWorkOrderId(Long dependsOnWorkOrderId);

    Optional<WorkOrderDependency> findByWorkOrderIdAndDependsOnWorkOrderId(Long workOrderId, Long dependsOnWorkOrderId);

    boolean existsByWorkOrderIdAndDependsOnWorkOrderId(Long workOrderId, Long dependsOnWorkOrderId);

    void deleteByWorkOrderIdAndDependsOnWorkOrderId(Long workOrderId, Long dependsOnWorkOrderId);
}
