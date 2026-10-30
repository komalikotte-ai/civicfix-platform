-- ==============================================================================
-- CivicFix Platform - V6 Infrastructure Asset Registry Schema (MySQL 8.0+)
-- Assets, specifications, inspection history, repair history, replacement history
-- ==============================================================================

-- 1. Infrastructure Assets
CREATE TABLE IF NOT EXISTS assets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    asset_type VARCHAR(50) NOT NULL,
    organization_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    ward_id BIGINT NULL,
    description TEXT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPERATIONAL',
    condition_rating VARCHAR(30) NOT NULL DEFAULT 'GOOD',
    address_line TEXT NULL,
    latitude DECIMAL(10, 8) NULL,
    longitude DECIMAL(11, 8) NULL,
    gis_geometry TEXT NULL,
    installation_date DATE NULL,
    commissioned_date DATE NULL,
    warranty_expiry_date DATE NULL,
    expected_lifecycle_years INT NULL,
    estimated_replacement_cost DECIMAL(14, 2) NULL DEFAULT 0.00,
    manufacturer VARCHAR(100) NULL,
    model_number VARCHAR(100) NULL,
    serial_number VARCHAR(100) NULL,
    specifications_json TEXT NULL,
    custodian_employee_id BIGINT NULL,
    last_inspected_at TIMESTAMP NULL,
    last_repaired_at TIMESTAMP NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    modified_by VARCHAR(100) NULL,
    CONSTRAINT fk_asset_org FOREIGN KEY (organization_id) REFERENCES organizations (id) ON DELETE RESTRICT,
    CONSTRAINT fk_asset_dept FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE RESTRICT,
    CONSTRAINT fk_asset_ward FOREIGN KEY (ward_id) REFERENCES wards (id) ON DELETE SET NULL,
    CONSTRAINT fk_asset_custodian FOREIGN KEY (custodian_employee_id) REFERENCES employees (id) ON DELETE SET NULL,
    INDEX idx_asset_code (asset_code),
    INDEX idx_asset_type (asset_type),
    INDEX idx_asset_dept (department_id),
    INDEX idx_asset_ward (ward_id),
    INDEX idx_asset_status (status),
    INDEX idx_asset_condition (condition_rating)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Link Complaints to Assets
ALTER TABLE complaints
    ADD COLUMN IF NOT EXISTS asset_id BIGINT NULL AFTER ward_id;

-- Add foreign key constraint if not exists (using alter table)
ALTER TABLE complaints
    ADD CONSTRAINT fk_complaint_asset FOREIGN KEY (asset_id) REFERENCES assets (id) ON DELETE SET NULL;

-- 3. Link Work Orders to Assets
ALTER TABLE work_orders
    ADD COLUMN IF NOT EXISTS asset_id BIGINT NULL AFTER ward_id;

ALTER TABLE work_orders
    ADD CONSTRAINT fk_wo_asset FOREIGN KEY (asset_id) REFERENCES assets (id) ON DELETE SET NULL;

-- 4. Asset Inspection Histories
CREATE TABLE IF NOT EXISTS asset_inspections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_id BIGINT NOT NULL,
    work_order_id BIGINT NULL,
    inspector_id BIGINT NULL,
    inspection_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    condition_rating VARCHAR(30) NOT NULL,
    operational_status VARCHAR(30) NOT NULL,
    findings TEXT NOT NULL,
    recommendations TEXT NULL,
    defects_observed TEXT NULL,
    score INT NULL,
    photos_json TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    CONSTRAINT fk_insp_asset FOREIGN KEY (asset_id) REFERENCES assets (id) ON DELETE CASCADE,
    CONSTRAINT fk_insp_wo FOREIGN KEY (work_order_id) REFERENCES work_orders (id) ON DELETE SET NULL,
    CONSTRAINT fk_insp_inspector FOREIGN KEY (inspector_id) REFERENCES employees (id) ON DELETE SET NULL,
    INDEX idx_insp_asset (asset_id),
    INDEX idx_insp_date (inspection_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Asset Repair Histories
CREATE TABLE IF NOT EXISTS asset_repairs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_id BIGINT NOT NULL,
    work_order_id BIGINT NULL,
    complaint_id BIGINT NULL,
    repair_type VARCHAR(50) NOT NULL DEFAULT 'CORRECTIVE',
    repair_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    repaired_by_id BIGINT NULL,
    cost DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    description TEXT NOT NULL,
    parts_replaced TEXT NULL,
    before_condition VARCHAR(30) NULL,
    after_condition VARCHAR(30) NULL,
    downtime_hours DECIMAL(8, 2) NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    CONSTRAINT fk_rep_asset FOREIGN KEY (asset_id) REFERENCES assets (id) ON DELETE CASCADE,
    CONSTRAINT fk_rep_wo FOREIGN KEY (work_order_id) REFERENCES work_orders (id) ON DELETE SET NULL,
    CONSTRAINT fk_rep_comp FOREIGN KEY (complaint_id) REFERENCES complaints (id) ON DELETE SET NULL,
    CONSTRAINT fk_rep_repairer FOREIGN KEY (repaired_by_id) REFERENCES employees (id) ON DELETE SET NULL,
    INDEX idx_rep_asset (asset_id),
    INDEX idx_rep_date (repair_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Asset Replacement Histories
CREATE TABLE IF NOT EXISTS asset_replacements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_id BIGINT NOT NULL,
    replacement_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    replaced_asset_id BIGINT NULL,
    replacement_reason TEXT NOT NULL,
    replacement_cost DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    approved_by VARCHAR(100) NULL,
    disposal_method VARCHAR(50) NULL,
    salvage_value DECIMAL(12, 2) NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,
    CONSTRAINT fk_repl_asset FOREIGN KEY (asset_id) REFERENCES assets (id) ON DELETE CASCADE,
    CONSTRAINT fk_repl_old_asset FOREIGN KEY (replaced_asset_id) REFERENCES assets (id) ON DELETE SET NULL,
    INDEX idx_repl_asset (asset_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Seed Asset Permissions
INSERT INTO permissions (name, description) VALUES
    ('ASSET_READ', 'View municipal infrastructure assets and registry'),
    ('ASSET_CREATE', 'Create and register new infrastructure assets'),
    ('ASSET_UPDATE', 'Modify asset details and technical specifications'),
    ('ASSET_INSPECT', 'Record asset inspections and health evaluations'),
    ('ASSET_REPAIR', 'Record repair work and replacement events')
ON DUPLICATE KEY UPDATE description=VALUES(description);
