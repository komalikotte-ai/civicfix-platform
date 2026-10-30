package com.civicfix.platform.workorders.entity;

import com.civicfix.platform.workorders.enums.DependencyType;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "work_order_dependencies", uniqueConstraints = {
        @UniqueConstraint(name = "uk_wo_dependency", columnNames = {"work_order_id", "depends_on_work_order_id"})
})
public class WorkOrderDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depends_on_work_order_id", nullable = false)
    private WorkOrder dependsOnWorkOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "dependency_type", nullable = false, length = 30)
    private DependencyType dependencyType = DependencyType.FINISH_TO_START;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public WorkOrderDependency() {
    }

    public WorkOrderDependency(WorkOrder workOrder, WorkOrder dependsOnWorkOrder, DependencyType dependencyType) {
        this.workOrder = workOrder;
        this.dependsOnWorkOrder = dependsOnWorkOrder;
        this.dependencyType = dependencyType != null ? dependencyType : DependencyType.FINISH_TO_START;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public WorkOrder getWorkOrder() {
        return workOrder;
    }

    public void setWorkOrder(WorkOrder workOrder) {
        this.workOrder = workOrder;
    }

    public WorkOrder getDependsOnWorkOrder() {
        return dependsOnWorkOrder;
    }

    public void setDependsOnWorkOrder(WorkOrder dependsOnWorkOrder) {
        this.dependsOnWorkOrder = dependsOnWorkOrder;
    }

    public DependencyType getDependencyType() {
        return dependencyType;
    }

    public void setDependencyType(DependencyType dependencyType) {
        this.dependencyType = dependencyType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
