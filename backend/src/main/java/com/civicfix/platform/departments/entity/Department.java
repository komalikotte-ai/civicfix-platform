package com.civicfix.platform.departments.entity;

import com.civicfix.platform.common.entity.BaseAuditableEntity;
import com.civicfix.platform.departments.enums.DepartmentStatus;
import com.civicfix.platform.organizations.entity.Municipality;
import com.civicfix.platform.organizations.entity.Organization;
import jakarta.persistence.*;

@Entity
@Table(name = "departments")
public class Department extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipality_id")
    private Municipality municipality;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "escalation_email", length = 150)
    private String escalationEmail;

    @Column(name = "contact_phone", length = 25)
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private DepartmentStatus status = DepartmentStatus.ACTIVE;

    public Department() {
    }

    public Department(Municipality municipality, String name, String code) {
        this.municipality = municipality;
        this.name = name;
        this.code = code;
    }

    public Department(Organization organization, String name, String code) {
        this.organization = organization;
        this.name = name;
        this.code = code;
    }

    public Municipality getMunicipality() {
        return municipality;
    }

    public void setMunicipality(Municipality municipality) {
        this.municipality = municipality;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEscalationEmail() {
        return escalationEmail;
    }

    public void setEscalationEmail(String escalationEmail) {
        this.escalationEmail = escalationEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public DepartmentStatus getStatus() {
        return status;
    }

    public void setStatus(DepartmentStatus status) {
        this.status = status;
    }
}
