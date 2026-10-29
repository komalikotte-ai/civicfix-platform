package com.civicfix.platform.complaints.enums;

public enum ComplaintCategory {
    POTHOLE("Road Pothole & Surface Damage", "ROADS", ComplaintPriority.MEDIUM),
    STREETLIGHT_OUT("Streetlight Failure / Damaged Pole", "ELECTRICAL", ComplaintPriority.MEDIUM),
    GARBAGE_DUMP("Garbage Overflow / Irregular Collection", "SANITATION", ComplaintPriority.MEDIUM),
    WATER_LEAKAGE("Water Pipeline Leakage / Contamination", "WATER", ComplaintPriority.HIGH),
    DRAINAGE_OVERFLOW("Blocked Drain / Sewage Overflow", "SANITATION", ComplaintPriority.HIGH),
    DEAD_ANIMAL("Dead Animal Carcass Removal", "HEALTH", ComplaintPriority.HIGH),
    ILLEGAL_CONSTRUCTION("Illegal Encroachment or Construction", "TOWN_PLANNING", ComplaintPriority.LOW),
    TREE_FALL("Fallen Tree / Hazardous Branches", "PARKS", ComplaintPriority.URGENT),
    TRAFFIC_SIGNAL("Traffic Light Failure / Missing Signboard", "TRAFFIC", ComplaintPriority.HIGH),
    OTHER("General Municipal Grievance", "ADMINISTRATION", ComplaintPriority.LOW);

    private final String displayName;
    private final String defaultDepartmentCode;
    private final ComplaintPriority defaultPriority;

    ComplaintCategory(String displayName, String defaultDepartmentCode, ComplaintPriority defaultPriority) {
        this.displayName = displayName;
        this.defaultDepartmentCode = defaultDepartmentCode;
        this.defaultPriority = defaultPriority;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDefaultDepartmentCode() {
        return defaultDepartmentCode;
    }

    public ComplaintPriority getDefaultPriority() {
        return defaultPriority;
    }
}
