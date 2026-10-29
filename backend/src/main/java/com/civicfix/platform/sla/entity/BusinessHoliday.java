package com.civicfix.platform.sla.entity;

import com.civicfix.platform.common.entity.BaseAuditableEntity;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.organizations.entity.Organization;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "business_holidays", indexes = {
        @Index(name = "idx_holiday_date", columnList = "holiday_date"),
        @Index(name = "idx_holiday_org", columnList = "organization_id")
})
public class BusinessHoliday extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "recurring", nullable = false)
    private boolean recurring = false;

    public BusinessHoliday() {
    }

    public BusinessHoliday(Organization organization, Department department, LocalDate holidayDate, String name, boolean recurring) {
        this.organization = organization;
        this.department = department;
        this.holidayDate = holidayDate;
        this.name = name;
        this.recurring = recurring;
    }

    public boolean matches(LocalDate date) {
        if (date == null) return false;
        if (recurring) {
            return holidayDate.getMonth() == date.getMonth() && holidayDate.getDayOfMonth() == date.getDayOfMonth();
        }
        return holidayDate.equals(date);
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public LocalDate getHolidayDate() {
        return holidayDate;
    }

    public void setHolidayDate(LocalDate holidayDate) {
        this.holidayDate = holidayDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRecurring() {
        return recurring;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }
}
