package com.civicfix.platform.users.enums;

/**
 * Fine-grained platform permissions for authorization.
 */
public enum PermissionType {
    USER_READ,
    USER_WRITE,
    USER_DELETE,
    ROLE_MANAGE,
    COMPLAINT_READ,
    COMPLAINT_CREATE,
    COMPLAINT_UPDATE,
    COMPLAINT_ASSIGN,
    COMPLAINT_RESOLVE,
    DEPARTMENT_MANAGE,
    WARD_MANAGE,
    ORGANIZATION_MANAGE,
    AUDIT_READ,
    SECURITY_ADMIN
}
