package com.civicfix.platform.workorders.enums;

/**
 * Supported dependency relationship types between municipal work orders.
 */
public enum DependencyType {
    FINISH_TO_START,
    START_TO_START,
    FINISH_TO_FINISH
}
