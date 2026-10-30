-- ==============================================================================
-- CivicFix Platform - V5 Work-Order Management Schema Baseline (MySQL 8.0+)
-- Work orders, assignments, lifecycle status history, dependencies, comments
-- ==============================================================================

-- 1. Work Orders
CREATE TABLE IF NOT EXISTS work_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    work_order_number VARCHAR(50) NOT NULL UNIQUE,
    complaint_id BIGINT NULL,
    organization_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    ward_id BIGINT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(30) NOT NULL DEFAULT 'CREATED',
    assigned_worker_id BIGINT NULL,
    assigned_supervisor_id BIGINT NULL,
    assigned_at TIMESTAMP NULL,
    scheduled_start_date TIMESTAMP NULL,
    expected_completion_date TIMESTAMP NULL,
    actual_start_date TIMESTAMP NULL,
    actual_completion_date TIMESTAMP NULL,
    address_line TEXT NULL,
    latitude DECIMAL(10, 8) NULL,
    longitude DECIMAL(11, 8) NULL,
    estimated_cost DECIMAL(12, 2) NULL DEFAULT 0.00,
    actual_cost DECIMAL(12, 2) NULL DEFAULT 0.00,
    completion_notes TEXT NULL,
    evidence_metadata TEXT NULL,
    rejection_reason TEXT NULL,
    rejection_count INT NOT NULL DEFAULT 0,
    verified_by_id BIGINT NULL,
    verified_at TIMESTAMP NULL,
    verification_notes TEXT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    modified_by VARCHAR(100) NULL,
    CONSTRAINT fk_wo_complaint FOREIGN KEY (complaint_id) REFERENCES complaints (id) ON DELETE SET NULL,
    CONSTRAINT fk_wo_org FOREIGN KEY (organization_id) REFERENCES organizations (id) ON DELETE RESTRICT,
    CONSTRAINT fk_wo_dept FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE RESTRICT,
    CONSTRAINT fk_wo_ward FOREIGN KEY (ward_id) REFERENCES wards (id) ON DELETE SET NULL,
    CONSTRAINT fk_wo_worker FOREIGN KEY (assigned_worker_id) REFERENCES employees (id) ON DELETE SET NULL,
    CONSTRAINT fk_wo_supervisor FOREIGN KEY (assigned_supervisor_id) REFERENCES employees (id) ON DELETE SET NULL,
    CONSTRAINT fk_wo_verifier FOREIGN KEY (verified_by_id) REFERENCES users (id) ON DELETE SET NULL,
    INDEX idx_wo_number (work_order_number),
    INDEX idx_wo_complaint (complaint_id),
    INDEX idx_wo_dept (department_id),
    INDEX idx_wo_status (status),
    INDEX idx_wo_worker (assigned_worker_id),
    INDEX idx_wo_supervisor (assigned_supervisor_id),
    INDEX idx_wo_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Work Order Dependencies
CREATE TABLE IF NOT EXISTS work_order_dependencies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    work_order_id BIGINT NOT NULL,
    depends_on_work_order_id BIGINT NOT NULL,
    dependency_type VARCHAR(30) NOT NULL DEFAULT 'FINISH_TO_START',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wod_wo FOREIGN KEY (work_order_id) REFERENCES work_orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_wod_dep FOREIGN KEY (depends_on_work_order_id) REFERENCES work_orders (id) ON DELETE CASCADE,
    CONSTRAINT uk_wo_dependency UNIQUE (work_order_id, depends_on_work_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Work Order Status Histories
CREATE TABLE IF NOT EXISTS work_order_status_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    work_order_id BIGINT NOT NULL,
    from_status VARCHAR(30) NOT NULL,
    to_status VARCHAR(30) NOT NULL,
    remarks TEXT NULL,
    action_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wosh_wo FOREIGN KEY (work_order_id) REFERENCES work_orders (id) ON DELETE CASCADE,
    INDEX idx_wosh_wo (work_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Work Order Assignment Histories
CREATE TABLE IF NOT EXISTS work_order_assignment_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    work_order_id BIGINT NOT NULL,
    previous_worker_id BIGINT NULL,
    new_worker_id BIGINT NULL,
    previous_supervisor_id BIGINT NULL,
    new_supervisor_id BIGINT NULL,
    assigned_by VARCHAR(100) NOT NULL,
    reason TEXT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_woah_wo FOREIGN KEY (work_order_id) REFERENCES work_orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_woah_prev_worker FOREIGN KEY (previous_worker_id) REFERENCES employees (id) ON DELETE SET NULL,
    CONSTRAINT fk_woah_new_worker FOREIGN KEY (new_worker_id) REFERENCES employees (id) ON DELETE SET NULL,
    CONSTRAINT fk_woah_prev_sup FOREIGN KEY (previous_supervisor_id) REFERENCES employees (id) ON DELETE SET NULL,
    CONSTRAINT fk_woah_new_sup FOREIGN KEY (new_supervisor_id) REFERENCES employees (id) ON DELETE SET NULL,
    INDEX idx_woah_wo (work_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Work Order Comments
CREATE TABLE IF NOT EXISTS work_order_comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    work_order_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    author_name VARCHAR(150) NOT NULL,
    author_role VARCHAR(50) NULL,
    comment TEXT NOT NULL,
    internal_only BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_woc_wo FOREIGN KEY (work_order_id) REFERENCES work_orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_woc_author FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_woc_wo (work_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Seed Work-Order Permissions
INSERT INTO permissions (name, description) VALUES
    ('WORKORDER_READ', 'View field operations work orders'),
    ('WORKORDER_CREATE', 'Create and convert complaints into work orders'),
    ('WORKORDER_UPDATE', 'Update work order details and schedules'),
    ('WORKORDER_ASSIGN', 'Assign and reassign field workers and supervisors'),
    ('WORKORDER_EXECUTE', 'Accept, progress, and complete work orders'),
    ('WORKORDER_VERIFY', 'Inspect, verify, or reject completed work orders')
ON DUPLICATE KEY UPDATE description=VALUES(description);
