package com.civicfix.platform.inventory.dto;

import com.civicfix.platform.inventory.enums.WarehouseStatus;
import com.civicfix.platform.inventory.enums.WarehouseType;

import java.math.BigDecimal;

public class UpdateWarehouseRequest {

    private String name;
    private String description;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private WarehouseType warehouseType;
    private WarehouseStatus status;
    private Long managerId;
    private String capacityNotes;

    public UpdateWarehouseRequest() {}

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
    public Long getManagerId() { return managerId; }
    public void setManagerId(Long managerId) { this.managerId = managerId; }
    public String getCapacityNotes() { return capacityNotes; }
    public void setCapacityNotes(String capacityNotes) { this.capacityNotes = capacityNotes; }
}
