-- ==============================================================================
-- CivicFix Platform - V7 Preventive Maintenance Schema (MySQL 8.0+)
-- Maintenance schedules, inspection plans, recurring maintenance,
-- condition assessments, maintenance reminders, repair recommendations,
-- asset failure history, and work-order integration.
-- ==============================================================================

-- 1. Inspection Plans / Standard Operating Checklists
CREATE TABLE IF NOT EXISTS inspection_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    description TEXT NULL,
    plan_type VARCHAR(50) NOT NULL DEFAULT 'ROUTINE',
    target_asset_type VARCHAR(50) NULL,
    frequency VARCHAR(30) NOT NULL DEFAULT 'MONTHLY',
    checklist_json TEXT NOT NULL,
    safety_guidelines TEXT NULL,
    estimated_duration_minutes INT NULL DEFAULT 60,
    department_id BIGINT NOT NULL,
    organization_id BIGINT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    modified_by VARCHAR(100) NULL,
    CONSTRAINT fk_insp_plan_dept FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE RESTRICT,
    CONSTRAINT fk_insp_plan_org FOREIGN KEY (organization_id) REFERENCES organizations (id) ON DELETE RESTRICT,
    INDEX idx_plan_code (plan_code),
    INDEX idx_plan_type (plan_type),
    INDEX idx_plan_asset_type (target_asset_type),
    INDEX idx_plan_dept (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Maintenance Schedules (Recurring maintenance)
CREATE TABLE IF NOT EXISTS maintenance_schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_code VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    description TEXT NULL,
    asset_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    inspection_plan_id BIGINT NULL,
    assigned_worker_id BIGINT NULL,
    assigned_supervisor_id BIGINT NULL,
    frequency VARCHAR(30) NOT NULL DEFAULT 'MONTHLY',
    interval_days INT NULL DEFAULT 30,
    start_date DATE NOT NULL,
    end_date DATE NULL,
    last_run_date TIMESTAMP NULL,
    next_due_date TIMESTAMP NOT NULL,
    estimated_duration_hours DECIMAL(6, 2) NULL DEFAULT 2.00,
    estimated_cost DECIMAL(12, 2) NULL DEFAULT 0.00,
    priority VARCHAR(30) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    auto_generate_work_order BOOLEAN NOT NULL DEFAULT TRUE,
    checklist_json TEXT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    modified_by VARCHAR(100) NULL,
    CONSTRAINT fk_maint_sched_asset FOREIGN KEY (asset_id) REFERENCES assets (id) ON DELETE CASCADE,
    CONSTRAINT fk_maint_sched_dept FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE RESTRICT,
    CONSTRAINT fk_maint_sched_plan FOREIGN KEY (inspection_plan_id) REFERENCES inspection_plans (id) ON DELETE SET NULL,
    CONSTRAINT fk_maint_sched_worker FOREIGN KEY (assigned_worker_id) REFERENCES employees (id) ON DELETE SET NULL,
    CONSTRAINT fk_maint_sched_super FOREIGN KEY (assigned_supervisor_id) REFERENCES employees (id) ON DELETE SET NULL,
    INDEX idx_sched_code (schedule_code),
    INDEX idx_sched_asset (asset_id),
    INDEX idx_sched_dept (department_id),
    INDEX idx_sched_next_due (next_due_date),
    INDEX idx_sched_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Condition Assessments (In-depth physical condition, health scoring, degradation)
CREATE TABLE IF NOT EXISTS condition_assessments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assessment_code VARCHAR(50) NOT NULL UNIQUE,
    asset_id BIGINT NOT NULL,
    inspector_id BIGINT NULL,
    assessment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    overall_condition VARCHAR(30) NOT NULL DEFAULT 'GOOD',
    health_index_score INT NOT NULL,
    previous_health_index_score INT NULL,
    degradation_rate DECIMAL(6, 2) NULL DEFAULT 0.00,
    structural_health VARCHAR(100) NULL,
    functional_performance VARCHAR(100) NULL,
    safety_risk_assessment VARCHAR(100) NULL,
    estimated_remaining_useful_life_years DECIMAL(5, 1) NULL,
    assessment_notes TEXT NOT NULL,
    photos_json TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    modified_by VARCHAR(100) NULL,
    CONSTRAINT fk_cond_asset FOREIGN KEY (asset_id) REFERENCES assets (id) ON DELETE CASCADE,
    CONSTRAINT fk_cond_inspector FOREIGN KEY (inspector_id) REFERENCES employees (id) ON DELETE SET NULL,
    INDEX idx_cond_code (assessment_code),
    INDEX idx_cond_asset (asset_id),
    INDEX idx_cond_date (assessment_date),
    INDEX idx_cond_rating (overall_condition)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Maintenance Reminders (Due/overdue alerts for operations & supervisors)
CREATE TABLE IF NOT EXISTS maintenance_reminders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    due_date TIMESTAMP NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    reminder_type VARCHAR(50) NOT NULL DEFAULT 'UPCOMING',
    schedule_id BIGINT NULL,
    asset_id BIGINT NULL,
    recipient_id BIGINT NULL,
    sent_at TIMESTAMP NULL,
    acknowledged_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    modified_by VARCHAR(100) NULL,
    CONSTRAINT fk_rem_sched FOREIGN KEY (schedule_id) REFERENCES maintenance_schedules (id) ON DELETE CASCADE,
    CONSTRAINT fk_rem_asset FOREIGN KEY (asset_id) REFERENCES assets (id) ON DELETE CASCADE,
    CONSTRAINT fk_rem_recipient FOREIGN KEY (recipient_id) REFERENCES employees (id) ON DELETE SET NULL,
    INDEX idx_rem_status (status),
    INDEX idx_rem_due (due_date),
    INDEX idx_rem_recipient (recipient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Repair Recommendations (Proactive recommendations generated from inspections/assessments)
CREATE TABLE IF NOT EXISTS repair_recommendations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recommendation_code VARCHAR(50) NOT NULL UNIQUE,
    asset_id BIGINT NOT NULL,
    inspection_id BIGINT NULL,
    condition_assessment_id BIGINT NULL,
    recommended_action VARCHAR(50) NOT NULL DEFAULT 'REPAIR',
    priority VARCHAR(30) NOT NULL DEFAULT 'MEDIUM',
    title VARCHAR(200) NOT NULL,
    justification TEXT NOT NULL,
    suggested_scope_of_work TEXT NULL,
    estimated_cost DECIMAL(12, 2) NULL DEFAULT 0.00,
    estimated_downtime_hours DECIMAL(6, 2) NULL DEFAULT 0.00,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    work_order_id BIGINT NULL,
    reviewed_by VARCHAR(100) NULL,
    reviewed_at TIMESTAMP NULL,
    review_notes TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    modified_by VARCHAR(100) NULL,
    CONSTRAINT fk_rec_asset FOREIGN KEY (asset_id) REFERENCES assets (id) ON DELETE CASCADE,
    CONSTRAINT fk_rec_insp FOREIGN KEY (inspection_id) REFERENCES asset_inspections (id) ON DELETE SET NULL,
    CONSTRAINT fk_rec_cond FOREIGN KEY (condition_assessment_id) REFERENCES condition_assessments (id) ON DELETE SET NULL,
    CONSTRAINT fk_rec_wo FOREIGN KEY (work_order_id) REFERENCES work_orders (id) ON DELETE SET NULL,
    INDEX idx_rec_code (recommendation_code),
    INDEX idx_rec_asset (asset_id),
    INDEX idx_rec_status (status),
    INDEX idx_rec_priority (priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Asset Failure Logs (Breakdown / failure history)
CREATE TABLE IF NOT EXISTS asset_failure_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    failure_code VARCHAR(50) NOT NULL UNIQUE,
    asset_id BIGINT NOT NULL,
    failure_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reported_by_id BIGINT NULL,
    failure_category VARCHAR(100) NOT NULL,
    failure_mode VARCHAR(100) NOT NULL,
    root_cause TEXT NOT NULL,
    severity VARCHAR(30) NOT NULL DEFAULT 'MODERATE',
    downtime_hours DECIMAL(8, 2) NULL DEFAULT 0.00,
    immediate_action_taken TEXT NULL,
    preventive_action_recommended TEXT NULL,
    work_order_id BIGINT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    modified_by VARCHAR(100) NULL,
    CONSTRAINT fk_fail_asset FOREIGN KEY (asset_id) REFERENCES assets (id) ON DELETE CASCADE,
    CONSTRAINT fk_fail_reporter FOREIGN KEY (reported_by_id) REFERENCES employees (id) ON DELETE SET NULL,
    CONSTRAINT fk_fail_wo FOREIGN KEY (work_order_id) REFERENCES work_orders (id) ON DELETE SET NULL,
    INDEX idx_fail_code (failure_code),
    INDEX idx_fail_asset (asset_id),
    INDEX idx_fail_date (failure_date),
    INDEX idx_fail_severity (severity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Seed Preventive Maintenance Permissions
INSERT INTO permissions (name, description) VALUES
    ('MAINTENANCE_READ', 'View maintenance schedules, plans, condition assessments, and failure logs'),
    ('MAINTENANCE_MANAGE', 'Create and configure maintenance schedules and inspection plans'),
    ('MAINTENANCE_EXECUTE', 'Record condition assessments, log failures, and review recommendations')
ON DUPLICATE KEY UPDATE description=VALUES(description);
