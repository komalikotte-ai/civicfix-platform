package com.civicfix.platform.workorders.entity;

import com.civicfix.platform.users.entity.Employee;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "work_order_assignment_histories", indexes = {
        @Index(name = "idx_woah_wo", columnList = "work_order_id")
})
public class WorkOrderAssignmentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_worker_id")
    private Employee previousWorker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_worker_id")
    private Employee newWorker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_supervisor_id")
    private Employee previousSupervisor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_supervisor_id")
    private Employee newSupervisor;

    @Column(name = "assigned_by", nullable = false, length = 100)
    private String assignedBy;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt = Instant.now();

    public WorkOrderAssignmentHistory() {
    }

    public WorkOrderAssignmentHistory(WorkOrder workOrder, Employee previousWorker, Employee newWorker,
                                       Employee previousSupervisor, Employee newSupervisor,
                                       String assignedBy, String reason) {
        this.workOrder = workOrder;
        this.previousWorker = previousWorker;
        this.newWorker = newWorker;
        this.previousSupervisor = previousSupervisor;
        this.newSupervisor = newSupervisor;
        this.assignedBy = assignedBy;
        this.reason = reason;
        this.assignedAt = Instant.now();
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

    public Employee getPreviousWorker() {
        return previousWorker;
    }

    public void setPreviousWorker(Employee previousWorker) {
        this.previousWorker = previousWorker;
    }

    public Employee getNewWorker() {
        return newWorker;
    }

    public void setNewWorker(Employee newWorker) {
        this.newWorker = newWorker;
    }

    public Employee getPreviousSupervisor() {
        return previousSupervisor;
    }

    public void setPreviousSupervisor(Employee previousSupervisor) {
        this.previousSupervisor = previousSupervisor;
    }

    public Employee getNewSupervisor() {
        return newSupervisor;
    }

    public void setNewSupervisor(Employee newSupervisor) {
        this.newSupervisor = newSupervisor;
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Instant assignedAt) {
        this.assignedAt = assignedAt;
    }
}
