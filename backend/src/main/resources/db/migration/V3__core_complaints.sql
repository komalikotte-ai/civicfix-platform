-- ============================================================================
-- CivicFix Platform - V3 Core Complaint Management Migration
-- Extends complaints table and creates attachments, comments, assignment,
-- and status transition history tables.
-- ============================================================================

-- 1. Alter Complaints table with new attributes and foreign keys
ALTER TABLE complaints
    ADD COLUMN assigned_employee_id BIGINT NULL AFTER assigned_officer_id,
    ADD COLUMN assigned_supervisor_id BIGINT NULL AFTER assigned_employee_id,
    ADD COLUMN assigned_at TIMESTAMP NULL AFTER assigned_supervisor_id,
    ADD COLUMN severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM' AFTER priority,
    ADD COLUMN source VARCHAR(30) NOT NULL DEFAULT 'CITIZEN_PORTAL' AFTER status,
    ADD COLUMN landmark VARCHAR(255) NULL AFTER address_line,
    ADD COLUMN reopened_count INT NOT NULL DEFAULT 0 AFTER resolved_at,
    ADD COLUMN last_reopened_at TIMESTAMP NULL AFTER reopened_count,
    ADD COLUMN duplicate_group_id VARCHAR(100) NULL AFTER last_reopened_at,
    ADD COLUMN is_duplicate BOOLEAN NOT NULL DEFAULT FALSE AFTER duplicate_group_id,
    ADD COLUMN duplicate_of_id BIGINT NULL AFTER is_duplicate,
    ADD COLUMN duplicate_similarity_score DOUBLE NULL AFTER duplicate_of_id,
    ADD COLUMN duplicate_confirmed BOOLEAN NOT NULL DEFAULT FALSE AFTER duplicate_similarity_score;

ALTER TABLE complaints
    ADD CONSTRAINT fk_complaint_assigned_emp FOREIGN KEY (assigned_employee_id) REFERENCES employees (id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_complaint_assigned_sup FOREIGN KEY (assigned_supervisor_id) REFERENCES employees (id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_complaint_dup_of FOREIGN KEY (duplicate_of_id) REFERENCES complaints (id) ON DELETE SET NULL,
    ADD INDEX idx_complaint_assigned_emp (assigned_employee_id),
    ADD INDEX idx_complaint_dup_group (duplicate_group_id),
    ADD INDEX idx_complaint_citizen (citizen_id);

-- 2. Complaint Attachments Table
CREATE TABLE IF NOT EXISTS complaint_attachments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_type VARCHAR(100),
    file_size BIGINT,
    uploaded_by VARCHAR(100),
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    modified_by VARCHAR(100),
    CONSTRAINT fk_att_complaint FOREIGN KEY (complaint_id) REFERENCES complaints (id) ON DELETE CASCADE,
    INDEX idx_att_complaint (complaint_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Complaint Comments Table (Public & Internal Staff Notes)
CREATE TABLE IF NOT EXISTS complaint_comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    author_name VARCHAR(150) NOT NULL,
    author_role VARCHAR(50),
    comment TEXT NOT NULL,
    internal_only BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    modified_by VARCHAR(100),
    CONSTRAINT fk_comm_complaint FOREIGN KEY (complaint_id) REFERENCES complaints (id) ON DELETE CASCADE,
    CONSTRAINT fk_comm_author FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_comm_complaint (complaint_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Complaint Assignment Histories Table
CREATE TABLE IF NOT EXISTS complaint_assignment_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_id BIGINT NOT NULL,
    previous_department_id BIGINT NULL,
    new_department_id BIGINT NULL,
    previous_assignee_id BIGINT NULL,
    new_assignee_id BIGINT NULL,
    previous_supervisor_id BIGINT NULL,
    new_supervisor_id BIGINT NULL,
    assigned_by VARCHAR(100),
    reason TEXT,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    modified_by VARCHAR(100),
    CONSTRAINT fk_cah_complaint FOREIGN KEY (complaint_id) REFERENCES complaints (id) ON DELETE CASCADE,
    CONSTRAINT fk_cah_prev_dept FOREIGN KEY (previous_department_id) REFERENCES departments (id) ON DELETE SET NULL,
    CONSTRAINT fk_cah_new_dept FOREIGN KEY (new_department_id) REFERENCES departments (id) ON DELETE SET NULL,
    CONSTRAINT fk_cah_prev_emp FOREIGN KEY (previous_assignee_id) REFERENCES employees (id) ON DELETE SET NULL,
    CONSTRAINT fk_cah_new_emp FOREIGN KEY (new_assignee_id) REFERENCES employees (id) ON DELETE SET NULL,
    CONSTRAINT fk_cah_prev_sup FOREIGN KEY (previous_supervisor_id) REFERENCES employees (id) ON DELETE SET NULL,
    CONSTRAINT fk_cah_new_sup FOREIGN KEY (new_supervisor_id) REFERENCES employees (id) ON DELETE SET NULL,
    INDEX idx_cah_complaint (complaint_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Complaint Status Histories Table
CREATE TABLE IF NOT EXISTS complaint_status_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_id BIGINT NOT NULL,
    from_status VARCHAR(30) NULL,
    to_status VARCHAR(30) NOT NULL,
    action_by_user_id BIGINT NULL,
    action_by_username VARCHAR(100),
    remarks TEXT,
    transition_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    modified_by VARCHAR(100),
    CONSTRAINT fk_csh_complaint FOREIGN KEY (complaint_id) REFERENCES complaints (id) ON DELETE CASCADE,
    CONSTRAINT fk_csh_user FOREIGN KEY (action_by_user_id) REFERENCES users (id) ON DELETE SET NULL,
    INDEX idx_csh_complaint (complaint_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
