package com.civicfix.platform.users.entity;

import com.civicfix.platform.common.entity.BaseAuditableEntity;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.departments.entity.Team;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "employee_assignment_histories")
public class EmployeeAssignmentHistory extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_department_id")
    private Department previousDepartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_department_id", nullable = false)
    private Department newDepartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_team_id")
    private Team previousTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_team_id")
    private Team newTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_supervisor_id")
    private Employee previousSupervisor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_supervisor_id")
    private Employee newSupervisor;

    @Column(name = "effective_from", nullable = false)
    private Instant effectiveFrom = Instant.now();

    @Column(name = "effective_to")
    private Instant effectiveTo;

    @Column(name = "assigned_by", length = 100)
    private String assignedBy;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    public EmployeeAssignmentHistory() {
    }

    public EmployeeAssignmentHistory(Employee employee,
                                     Department previousDepartment, Department newDepartment,
                                     Team previousTeam, Team newTeam,
                                     Employee previousSupervisor, Employee newSupervisor,
                                     String assignedBy, String reason) {
        this.employee = employee;
        this.previousDepartment = previousDepartment;
        this.newDepartment = newDepartment;
        this.previousTeam = previousTeam;
        this.newTeam = newTeam;
        this.previousSupervisor = previousSupervisor;
        this.newSupervisor = newSupervisor;
        this.assignedBy = assignedBy;
        this.reason = reason;
        this.effectiveFrom = Instant.now();
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
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

    public Team getPreviousTeam() {
        return previousTeam;
    }

    public void setPreviousTeam(Team previousTeam) {
        this.previousTeam = previousTeam;
    }

    public Team getNewTeam() {
        return newTeam;
    }

    public void setNewTeam(Team newTeam) {
        this.newTeam = newTeam;
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

    public Instant getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(Instant effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public Instant getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(Instant effectiveTo) {
        this.effectiveTo = effectiveTo;
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
}
