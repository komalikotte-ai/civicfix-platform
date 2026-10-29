package com.civicfix.platform.complaints.entity;

import com.civicfix.platform.common.entity.BaseAuditableEntity;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.users.entity.Employee;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "complaint_assignment_histories")
public class ComplaintAssignmentHistory extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_department_id")
    private Department previousDepartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_department_id")
    private Department newDepartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_assignee_id")
    private Employee previousAssignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_assignee_id")
    private Employee newAssignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_supervisor_id")
    private Employee previousSupervisor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_supervisor_id")
    private Employee newSupervisor;

    @Column(name = "assigned_by", length = 100)
    private String assignedBy;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt = Instant.now();

    public ComplaintAssignmentHistory() {
    }

    public ComplaintAssignmentHistory(Complaint complaint,
                                      Department previousDepartment, Department newDepartment,
                                      Employee previousAssignee, Employee newAssignee,
                                      Employee previousSupervisor, Employee newSupervisor,
                                      String assignedBy, String reason) {
        this.complaint = complaint;
        this.previousDepartment = previousDepartment;
        this.newDepartment = newDepartment;
        this.previousAssignee = previousAssignee;
        this.newAssignee = newAssignee;
        this.previousSupervisor = previousSupervisor;
        this.newSupervisor = newSupervisor;
        this.assignedBy = assignedBy;
        this.reason = reason;
        this.assignedAt = Instant.now();
    }

    public Complaint getComplaint() {
        return complaint;
    }

    public void setComplaint(Complaint complaint) {
        this.complaint = complaint;
    }

    public Department getPreviousDepartment() {
        return previousDepartment;
    }

    public void setPreviousDepartment(Department previousDepartment) {
        this.previousDepartment = previousDepartment;
    }

    public Department getNewDepartment() {
        return newDepartment;
    }

    public void setNewDepartment(Department newDepartment) {
        this.newDepartment = newDepartment;
    }

    public Employee getPreviousAssignee() {
        return previousAssignee;
    }

    public void setPreviousAssignee(Employee previousAssignee) {
        this.previousAssignee = previousAssignee;
    }

    public Employee getNewAssignee() {
        return newAssignee;
    }

    public void setNewAssignee(Employee newAssignee) {
        this.newAssignee = newAssignee;
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
