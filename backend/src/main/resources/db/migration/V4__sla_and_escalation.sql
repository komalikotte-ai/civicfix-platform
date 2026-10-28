-- ============================================================================
-- CivicFix Platform - V4 SLA & Escalation Engine Migration
-- Adds SLA states, warning timestamps, escalation levels, business calendars,
-- holidays, enhanced SLA policies, and escalation history tracking.
-- ============================================================================

-- 1. Alter Complaints table with SLA and Escalation tracking columns
ALTER TABLE complaints
    ADD COLUMN sla_state VARCHAR(30) NOT NULL DEFAULT 'NOT_STARTED' AFTER sla_due_at,
    ADD COLUMN sla_warning_at TIMESTAMP NULL AFTER sla_state,
    ADD COLUMN escalation_level VARCHAR(30) NOT NULL DEFAULT 'LEVEL_0' AFTER sla_warning_at,
    ADD COLUMN last_escalated_at TIMESTAMP NULL AFTER escalation_level,
    ADD INDEX idx_complaint_sla_state (sla_state),
    ADD INDEX idx_complaint_esc_level (escalation_level);

-- 2. Business Working-Hours Calendars Table
CREATE TABLE IF NOT EXISTS business_calendars (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    department_id BIGINT NULL,
    name VARCHAR(150) NOT NULL DEFAULT 'Standard Municipal Calendar',
    working_days VARCHAR(100) NOT NULL DEFAULT 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY',
    start_time TIME NOT NULL DEFAULT '09:00:00',
    end_time TIME NOT NULL DEFAULT '18:00:00',
    time_zone VARCHAR(50) NOT NULL DEFAULT 'Asia/Kolkata',
    is_24x7 BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    modified_by VARCHAR(100),
    CONSTRAINT fk_bcal_org FOREIGN KEY (organization_id) REFERENCES organizations (id) ON DELETE CASCADE,
    CONSTRAINT fk_bcal_dept FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE CASCADE,
    INDEX idx_cal_org (organization_id),
    INDEX idx_cal_dept (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Business Holidays Table
CREATE TABLE IF NOT EXISTS business_holidays (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    department_id BIGINT NULL,
    holiday_date DATE NOT NULL,
    name VARCHAR(150) NOT NULL,
    recurring BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    modified_by VARCHAR(100),
    CONSTRAINT fk_bhol_org FOREIGN KEY (organization_id) REFERENCES organizations (id) ON DELETE CASCADE,
    CONSTRAINT fk_bhol_dept FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE CASCADE,
    INDEX idx_holiday_date (holiday_date),
    INDEX idx_holiday_org (organization_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Extend SLA Policies Table
ALTER TABLE sla_policies
    ADD COLUMN category VARCHAR(100) NULL AFTER department_id,
    ADD COLUMN severity VARCHAR(20) NULL AFTER priority,
    ADD COLUMN response_time_hours INT NOT NULL DEFAULT 2 AFTER severity,
    ADD COLUMN warning_threshold_percent INT NOT NULL DEFAULT 75 AFTER escalation_time_hours,
    ADD COLUMN business_hours_only BOOLEAN NOT NULL DEFAULT TRUE AFTER warning_threshold_percent,
    ADD COLUMN calendar_id BIGINT NULL AFTER business_hours_only,
    ADD CONSTRAINT fk_sla_calendar FOREIGN KEY (calendar_id) REFERENCES business_calendars (id) ON DELETE SET NULL,
    ADD INDEX idx_sla_priority (priority),
    ADD INDEX idx_sla_cat (category);

-- 5. Complaint Escalation Histories Table
CREATE TABLE IF NOT EXISTS complaint_escalation_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_id BIGINT NOT NULL,
    from_level VARCHAR(30) NOT NULL,
    to_level VARCHAR(30) NOT NULL,
    escalated_to_employee_id BIGINT NULL,
    escalated_to_user_id BIGINT NULL,
    target_role VARCHAR(50) NULL,
    reason TEXT NOT NULL,
    action_taken TEXT NULL,
    escalated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    modified_by VARCHAR(100),
    CONSTRAINT fk_cesc_complaint FOREIGN KEY (complaint_id) REFERENCES complaints (id) ON DELETE CASCADE,
    CONSTRAINT fk_cesc_emp FOREIGN KEY (escalated_to_employee_id) REFERENCES employees (id) ON DELETE SET NULL,
    CONSTRAINT fk_cesc_user FOREIGN KEY (escalated_to_user_id) REFERENCES users (id) ON DELETE SET NULL,
    INDEX idx_esc_complaint (complaint_id),
    INDEX idx_esc_time (escalated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
