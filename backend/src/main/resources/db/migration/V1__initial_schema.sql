-- ==============================================================================
-- CivicFix Platform - Initial Relational Schema Baseline (MySQL 8.0+)
-- ==============================================================================

-- 1. Organizations (Municipal Corporations / City Councils)
CREATE TABLE IF NOT EXISTS organizations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    code VARCHAR(50) NOT NULL UNIQUE,
    state VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL DEFAULT 'India',
    contact_email VARCHAR(150),
    contact_phone VARCHAR(25),
    timezone VARCHAR(50) NOT NULL DEFAULT 'Asia/Kolkata',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    modified_by VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Wards (Geographical Administrative Units)
CREATE TABLE IF NOT EXISTS wards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    ward_number VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    zone_name VARCHAR(100),
    boundary_geojson TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    modified_by VARCHAR(100),
    CONSTRAINT fk_ward_organization FOREIGN KEY (organization_id) REFERENCES organizations (id) ON DELETE RESTRICT,
    CONSTRAINT uk_org_ward_number UNIQUE (organization_id, ward_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Departments (Sanitation, Roads, Electricity, Water, Health, etc.)
CREATE TABLE IF NOT EXISTS departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    description TEXT,
    escalation_email VARCHAR(150),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    modified_by VARCHAR(100),
    CONSTRAINT fk_department_organization FOREIGN KEY (organization_id) REFERENCES organizations (id) ON DELETE RESTRICT,
    CONSTRAINT uk_org_dept_code UNIQUE (organization_id, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Permissions
CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Roles
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Role Permissions Mapping
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_rp_permission FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Users
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id BIGINT,
    department_id BIGINT,
    ward_id BIGINT,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    phone_number VARCHAR(20),
    failed_login_attempts INT NOT NULL DEFAULT 0,
    account_locked BOOLEAN NOT NULL DEFAULT FALSE,
    lockout_expires_at TIMESTAMP NULL,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at TIMESTAMP NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    modified_by VARCHAR(100),
    CONSTRAINT fk_user_organization FOREIGN KEY (organization_id) REFERENCES organizations (id) ON DELETE SET NULL,
    CONSTRAINT fk_user_department FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE SET NULL,
    CONSTRAINT fk_user_ward FOREIGN KEY (ward_id) REFERENCES wards (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. User Roles Mapping
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Refresh Tokens
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at TIMESTAMP NULL,
    replaced_by_token VARCHAR(255) NULL,
    ip_address VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rt_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_rt_token (token),
    INDEX idx_rt_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Password Reset Tokens
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_prt_token (token),
    INDEX idx_prt_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Email Verification Tokens
CREATE TABLE IF NOT EXISTS email_verification_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    confirmed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_evt_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_evt_token (token),
    INDEX idx_evt_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. Complaints
CREATE TABLE IF NOT EXISTS complaints (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tracking_number VARCHAR(50) NOT NULL UNIQUE,
    organization_id BIGINT NOT NULL,
    ward_id BIGINT NOT NULL,
    department_id BIGINT,
    citizen_id BIGINT NOT NULL,
    assigned_officer_id BIGINT,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(100) NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED',
    address_line TEXT,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    sla_due_at TIMESTAMP NULL,
    resolved_at TIMESTAMP NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    modified_by VARCHAR(100),
    CONSTRAINT fk_complaint_org FOREIGN KEY (organization_id) REFERENCES organizations (id) ON DELETE RESTRICT,
    CONSTRAINT fk_complaint_ward FOREIGN KEY (ward_id) REFERENCES wards (id) ON DELETE RESTRICT,
    CONSTRAINT fk_complaint_dept FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE SET NULL,
    CONSTRAINT fk_complaint_citizen FOREIGN KEY (citizen_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_complaint_officer FOREIGN KEY (assigned_officer_id) REFERENCES users (id) ON DELETE SET NULL,
    INDEX idx_complaint_status (status),
    INDEX idx_complaint_ward (ward_id),
    INDEX idx_complaint_dept (department_id),
    INDEX idx_complaint_tracking (tracking_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. Workflow Transitions (History & State Machine)
CREATE TABLE IF NOT EXISTS workflow_transitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_id BIGINT NOT NULL,
    from_status VARCHAR(30) NOT NULL,
    to_status VARCHAR(30) NOT NULL,
    remarks TEXT,
    action_by_user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wt_complaint FOREIGN KEY (complaint_id) REFERENCES complaints (id) ON DELETE CASCADE,
    CONSTRAINT fk_wt_user FOREIGN KEY (action_by_user_id) REFERENCES users (id) ON DELETE RESTRICT,
    INDEX idx_wt_complaint (complaint_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. SLA Policies
CREATE TABLE IF NOT EXISTS sla_policies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    department_id BIGINT,
    priority VARCHAR(20) NOT NULL,
    resolution_time_hours INT NOT NULL,
    escalation_time_hours INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    modified_by VARCHAR(100),
    CONSTRAINT fk_sla_org FOREIGN KEY (organization_id) REFERENCES organizations (id) ON DELETE RESTRICT,
    CONSTRAINT fk_sla_dept FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. Audit Logs
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_name VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    performed_by VARCHAR(100) NOT NULL,
    ip_address VARCHAR(50),
    details TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_entity (entity_name, entity_id),
    INDEX idx_audit_user (performed_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed Default Platform Roles
INSERT INTO roles (name, description) VALUES
    ('ROLE_CITIZEN', 'Citizen user submitting and tracking municipal complaints'),
    ('ROLE_EMPLOYEE', 'Municipal internal staff member'),
    ('ROLE_SUPERVISOR', 'Field and operational supervisor managing crews and escalations'),
    ('ROLE_DEPARTMENT_MANAGER', 'Department head overseeing department grievances and SLAs'),
    ('ROLE_ADMIN', 'Municipal administrator with full administrative access'),
    ('ROLE_FIELD_WORKER', 'Field worker executing on-ground repair work'),
    ('ROLE_OPERATOR', 'City desk / Call center operator triaging complaints'),
    ('ROLE_OFFICER', 'Field officer assigned to resolve complaints'),
    ('ROLE_DEPT_ADMIN', 'Department administrator'),
    ('ROLE_SUPER_ADMIN', 'Platform super administrator')
ON DUPLICATE KEY UPDATE description=VALUES(description);

-- Seed Permissions
INSERT INTO permissions (name, description) VALUES
    ('USER_READ', 'View user accounts'),
    ('USER_WRITE', 'Create and modify user accounts'),
    ('USER_DELETE', 'Deactivate or delete user accounts'),
    ('ROLE_MANAGE', 'Assign and manage user roles'),
    ('COMPLAINT_READ', 'View grievance records'),
    ('COMPLAINT_CREATE', 'Lodge new grievances'),
    ('COMPLAINT_UPDATE', 'Update grievance details'),
    ('COMPLAINT_ASSIGN', 'Assign grievances to officers/workers'),
    ('COMPLAINT_RESOLVE', 'Mark grievances as resolved'),
    ('DEPARTMENT_MANAGE', 'Manage municipal departments'),
    ('WARD_MANAGE', 'Manage municipal wards'),
    ('ORGANIZATION_MANAGE', 'Manage municipal organization settings'),
    ('AUDIT_READ', 'Inspect security audit logs'),
    ('SECURITY_ADMIN', 'Manage security and token invalidation')
ON DUPLICATE KEY UPDATE description=VALUES(description);
