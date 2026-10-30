package com.civicfix.platform.inventory.entity;

import com.civicfix.platform.common.entity.BaseAuditableEntity;
import com.civicfix.platform.departments.entity.Department;
import com.civicfix.platform.inventory.enums.WarehouseStatus;
import com.civicfix.platform.inventory.enums.WarehouseType;
import com.civicfix.platform.organizations.entity.Organization;
import com.civicfix.platform.users.entity.Employee;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "warehouses", indexes = {
        @Index(name = "idx_wh_code",   columnList = "warehouse_code"),
        @Index(name = "idx_wh_dept",   columnList = "department_id"),
        @Index(name = "idx_wh_status", columnList = "status")
})
public class Warehouse extends BaseAuditableEntity {

    @Column(name = "warehouse_code", nullable = false, unique = true, length = 30)
    private String warehouseCode;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "warehouse_type", nullable = false, length = 30)
    private WarehouseType warehouseType = WarehouseType.GENERAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private WarehouseStatus status = WarehouseStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Employee manager;

    @Column(name = "capacity_notes", columnDefinition = "TEXT")
    private String capacityNotes;

    public Warehouse() {}

    public String getWarehouseCode() { return warehouseCode; }
    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public WarehouseType getWarehouseType() { return warehouseType; }
    public void setWarehouseType(WarehouseType warehouseType) { this.warehouseType = warehouseType; }

    public WarehouseStatus getStatus() { return status; }
    public void setStatus(WarehouseStatus status) { this.status = status; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public Employee getManager() { return manager; }
    public void setManager(Employee manager) { this.manager = manager; }

    public String getCapacityNotes() { return capacityNotes; }
    public void setCapacityNotes(String capacityNotes) { this.capacityNotes = capacityNotes; }
}
