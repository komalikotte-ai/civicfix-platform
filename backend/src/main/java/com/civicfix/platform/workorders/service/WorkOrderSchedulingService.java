package com.civicfix.platform.workorders.service;

import com.civicfix.platform.common.exception.BadRequestException;
import com.civicfix.platform.common.exception.ConflictException;
import com.civicfix.platform.common.exception.ResourceNotFoundException;
import com.civicfix.platform.workorders.dto.AddWorkOrderDependencyRequest;
import com.civicfix.platform.workorders.dto.WorkOrderDependencyDto;
import com.civicfix.platform.workorders.entity.WorkOrder;
import com.civicfix.platform.workorders.entity.WorkOrderDependency;
import com.civicfix.platform.workorders.enums.WorkOrderStatus;
import com.civicfix.platform.workorders.repository.WorkOrderDependencyRepository;
import com.civicfix.platform.workorders.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkOrderSchedulingService {

    private final WorkOrderDependencyRepository dependencyRepository;
    private final WorkOrderRepository workOrderRepository;

    public WorkOrderSchedulingService(WorkOrderDependencyRepository dependencyRepository,
                                      WorkOrderRepository workOrderRepository) {
        this.dependencyRepository = dependencyRepository;
        this.workOrderRepository = workOrderRepository;
    }

    /**
     * Validates scheduled start date and expected completion date.
     */
    public void validateScheduleDates(Instant scheduledStartDate, Instant expectedCompletionDate) {
        if (scheduledStartDate != null && expectedCompletionDate != null) {
            if (expectedCompletionDate.isBefore(scheduledStartDate)) {
                throw new BadRequestException("Expected completion date cannot be before scheduled start date");
            }
        }
    }

    /**
     * Adds a dependency between two work orders, ensuring no self-dependency, duplicate dependency, or circular chain.
     */
    @Transactional
    public WorkOrderDependencyDto addDependency(Long workOrderId, AddWorkOrderDependencyRequest request) {
        if (workOrderId.equals(request.getDependsOnWorkOrderId())) {
            throw new BadRequestException("Work order cannot depend on itself");
        }

        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with ID: " + workOrderId));

        WorkOrder dependsOnWorkOrder = workOrderRepository.findById(request.getDependsOnWorkOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Dependency work order not found with ID: " + request.getDependsOnWorkOrderId()));

        if (dependencyRepository.existsByWorkOrderIdAndDependsOnWorkOrderId(workOrderId, request.getDependsOnWorkOrderId())) {
            throw new ConflictException("Dependency relationship already exists between these work orders");
        }

        // Circular dependency check: does dependsOnWorkOrder directly or indirectly depend on workOrderId?
        if (hasCycle(request.getDependsOnWorkOrderId(), workOrderId)) {
            throw new BadRequestException(
                    String.format("Circular dependency detected: adding dependency on WO-%s would create a cycle",
                            dependsOnWorkOrder.getWorkOrderNumber()));
        }

        WorkOrderDependency dependency = new WorkOrderDependency(workOrder, dependsOnWorkOrder, request.getDependencyType());
        WorkOrderDependency saved = dependencyRepository.save(dependency);
        return WorkOrderDependencyDto.fromEntity(saved);
    }

    /**
     * Removes an existing dependency.
     */
    @Transactional
    public void removeDependency(Long workOrderId, Long dependsOnWorkOrderId) {
        WorkOrderDependency dependency = dependencyRepository.findByWorkOrderIdAndDependsOnWorkOrderId(workOrderId, dependsOnWorkOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Dependency not found between work order " + workOrderId + " and " + dependsOnWorkOrderId));
        dependencyRepository.delete(dependency);
    }

    @Transactional(readOnly = true)
    public List<WorkOrderDependencyDto> getDependencies(Long workOrderId) {
        return dependencyRepository.findByWorkOrderId(workOrderId).stream()
                .map(WorkOrderDependencyDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Checks if all dependencies for a given work order are completed/verified.
     */
    @Transactional(readOnly = true)
    public boolean arePrerequisitesMet(Long workOrderId) {
        List<WorkOrderDependency> dependencies = dependencyRepository.findByWorkOrderId(workOrderId);
        for (WorkOrderDependency dep : dependencies) {
            WorkOrderStatus status = dep.getDependsOnWorkOrder().getStatus();
            if (status != WorkOrderStatus.COMPLETED && status != WorkOrderStatus.VERIFIED) {
                return false;
            }
        }
        return true;
    }

    /**
     * DFS cycle detection: checks if targetId is reachable from startId through dependencies.
     */
    private boolean hasCycle(Long startId, Long targetId) {
        Set<Long> visited = new HashSet<>();
        Queue<Long> queue = new LinkedList<>();
        queue.add(startId);

        while (!queue.isEmpty()) {
            Long current = queue.poll();
            if (current.equals(targetId)) {
                return true;
            }
            if (visited.add(current)) {
                List<WorkOrderDependency> outgoing = dependencyRepository.findByWorkOrderId(current);
                for (WorkOrderDependency dep : outgoing) {
                    queue.add(dep.getDependsOnWorkOrder().getId());
                }
            }
        }
        return false;
    }
}
