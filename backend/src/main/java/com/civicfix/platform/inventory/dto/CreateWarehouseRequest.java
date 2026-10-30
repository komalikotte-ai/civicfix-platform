package com.civicfix.platform.inventory.dto;

import com.civicfix.platform.inventory.enums.WarehouseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class CreateWarehouseRequest {

    private String warehouseCode;

    @NotBlank(message = "Warehouse name is required")
    @Size(max = 150, message = "Warehouse name must not exceed 150 characters")
    private String name;

    private String description;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;

    @NotNull(message = "Warehouse type is required")
    private WarehouseType warehouseType = WarehouseType.GENERAL;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotNull(message = "Organization ID is required")
    private Long organizationId;

    private Long managerId;
    private String capacityNotes;

    public CreateWarehouseRequest() {}

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
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }
    public Long getManagerId() { return managerId; }
    public void setManagerId(Long managerId) { this.managerId = managerId; }
    public String getCapacityNotes() { return capacityNotes; }
    public void setCapacityNotes(String capacityNotes) { this.capacityNotes = capacityNotes; }
}
