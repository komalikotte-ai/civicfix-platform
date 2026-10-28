package com.civicfix.platform.sla.entity;

import com.civicfix.platform.common.entity.BaseAuditableEntity;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.organizations.entity.Organization;
import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "business_calendars", indexes = {
        @Index(name = "idx_cal_org", columnList = "organization_id"),
        @Index(name = "idx_cal_dept", columnList = "department_id")
})
public class BusinessCalendar extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "name", nullable = false, length = 150)
    private String name = "Default Municipal Calendar";

    @Column(name = "working_days", nullable = false, length = 100)
    private String workingDays = "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY";

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime = LocalTime.of(9, 0);

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime = LocalTime.of(18, 0);

    @Column(name = "time_zone", nullable = false, length = 50)
    private String timeZone = "Asia/Kolkata";

    @Column(name = "is_24x7", nullable = false)
    private boolean is24x7 = false;

    public BusinessCalendar() {
    }

    public BusinessCalendar(Organization organization, Department department, String name,
                            String workingDays, LocalTime startTime, LocalTime endTime,
                            String timeZone, boolean is24x7) {
        this.organization = organization;
        this.department = department;
        this.name = name;
        this.workingDays = workingDays;
        this.startTime = startTime;
        this.endTime = endTime;
        this.timeZone = timeZone != null ? timeZone : "Asia/Kolkata";
        this.is24x7 = is24x7;
    }

    public Set<DayOfWeek> getWorkingDaySet() {
        if (is24x7) {
            return EnumSet.allOf(DayOfWeek.class);
        }
        if (workingDays == null || workingDays.isBlank()) {
            return EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
        }
        return Arrays.stream(workingDays.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toSet());
    }

    public boolean isWorkingDay(DayOfWeek day) {
        if (is24x7) return true;
        return getWorkingDaySet().contains(day);
    }

    public boolean isWithinWorkingHours(LocalTime time) {
        if (is24x7) return true;
        return !time.isBefore(startTime) && time.isBefore(endTime);
    }

    public long getDailyWorkingMinutes() {
        if (is24x7) return 24 * 60;
        return Duration.between(startTime, endTime).toMinutes();
    }

    public ZoneId getZoneId() {
        try {
            return ZoneId.of(timeZone);
        } catch (Exception e) {
            return ZoneId.of("Asia/Kolkata");
        }
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(String workingDays) {
        this.workingDays = workingDays;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public boolean is24x7() {
        return is24x7;
    }

    public boolean isIs24x7() {
        return is24x7;
    }

    public void setIs24x7(boolean is24x7) {
        this.is24x7 = is24x7;
    }
}
