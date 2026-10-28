package com.civicfix.platform.sla.enums;

public enum EscalationLevel {
    LEVEL_0(0, "Assigned Worker / Officer", "ROLE_EMPLOYEE"),
    LEVEL_1(1, "Field Supervisor", "ROLE_SUPERVISOR"),
    LEVEL_2(2, "Department Manager", "ROLE_DEPARTMENT_MANAGER"),
    LEVEL_3(3, "Municipal Commissioner / Administrator", "ROLE_ADMIN");

    private final int level;
    private final String title;
    private final String targetRole;

    EscalationLevel(int level, String title, String targetRole) {
        this.level = level;
        this.title = title;
        this.targetRole = targetRole;
    }

    public int getLevel() {
        return level;
    }

    public String getTitle() {
        return title;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public EscalationLevel next() {
        return switch (this) {
            case LEVEL_0 -> LEVEL_1;
            case LEVEL_1 -> LEVEL_2;
            case LEVEL_2 -> LEVEL_3;
            case LEVEL_3 -> LEVEL_3; // Max escalation level
        };
    }
}
