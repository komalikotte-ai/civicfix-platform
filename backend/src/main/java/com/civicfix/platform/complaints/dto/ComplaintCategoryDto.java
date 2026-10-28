package com.civicfix.platform.complaints.dto;

import com.civicfix.platform.complaints.enums.ComplaintCategory;
import com.civicfix.platform.complaints.enums.ComplaintPriority;

public class ComplaintCategoryDto {

    private String name;
    private String displayName;
    private String defaultDepartmentCode;
    private ComplaintPriority defaultPriority;

    public ComplaintCategoryDto() {
    }

    public ComplaintCategoryDto(String name, String displayName, String defaultDepartmentCode, ComplaintPriority defaultPriority) {
        this.name = name;
        this.displayName = displayName;
        this.defaultDepartmentCode = defaultDepartmentCode;
        this.defaultPriority = defaultPriority;
    }

    public static ComplaintCategoryDto fromEnum(ComplaintCategory category) {
        if (category == null) return null;
        return new ComplaintCategoryDto(
                category.name(),
                category.getDisplayName(),
                category.getDefaultDepartmentCode(),
                category.getDefaultPriority()
        );
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDefaultDepartmentCode() {
        return defaultDepartmentCode;
    }

    public void setDefaultDepartmentCode(String defaultDepartmentCode) {
        this.defaultDepartmentCode = defaultDepartmentCode;
    }

    public ComplaintPriority getDefaultPriority() {
        return defaultPriority;
    }

    public void setDefaultPriority(ComplaintPriority defaultPriority) {
        this.defaultPriority = defaultPriority;
    }
}
