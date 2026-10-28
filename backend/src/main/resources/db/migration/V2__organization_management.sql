-- ==============================================================================
-- CivicFix Platform - Organization Management Schema (City, Municipality, Ward, Dept, Team, Employee)
-- ==============================================================================

-- 1. Cities
CREATE TABLE IF NOT EXISTS cities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(50) NOT NULL UNIQUE,
    state VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL DEFAULT 'India',
    timezone VARCHAR(50) NOT NULL DEFAULT 'Asia/Kolkata',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    modified_by VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Municipalities
CREATE TABLE IF NOT EXISTS municipalities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    city_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL UNIQUE,
    code VARCHAR(50) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL DEFAULT 'MUNICIPAL_CORPORATION',
    contact_email VARCHAR(150),
    contact_phone VARCHAR(25),
    address TEXT,
    website VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    modified_by VARCHAR(100),
    CONSTRAINT fk_municipality_city FOREIGN KEY (city_id) REFERENCES cities (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Update Wards for Municipality & GIS Metadata
ALTER TABLE wards
    ADD COLUMN IF NOT EXISTS municipality_id BIGINT NULL AFTER organization_id,
    ADD COLUMN IF NOT EXISTS center_latitude DECIMAL(10, 8) NULL AFTER boundary_geojson,
    ADD COLUMN IF NOT EXISTS center_longitude DECIMAL(11, 8) NULL AFTER center_latitude,
    ADD COLUMN IF NOT EXISTS area_sq_km DECIMAL(10, 2) NULL AFTER center_longitude,
    ADD COLUMN IF NOT EXISTS population INT NULL AFTER area_sq_km;

-- 4. Update Departments for Municipality & Status
ALTER TABLE departments
    ADD COLUMN IF NOT EXISTS municipality_id BIGINT NULL AFTER organization_id,
    ADD COLUMN IF NOT EXISTS contact_phone VARCHAR(25) NULL AFTER escalation_email,
    ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' AFTER contact_phone;

-- 5. Teams
CREATE TABLE IF NOT EXISTS teams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    department_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    supervisor_id BIGINT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    modified_by VARCHAR(100),
    CONSTRAINT fk_team_dept FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Employees
CREATE TABLE IF NOT EXISTS employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL UNIQUE,
    employee_code VARCHAR(50) NOT NULL UNIQUE,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone_number VARCHAR(25),
    designation VARCHAR(100),
    department_id BIGINT NOT NULL,
    team_id BIGINT NULL,
    supervisor_id BIGINT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_EMPLOYEE',
    joining_date DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    modified_by VARCHAR(100),
    CONSTRAINT fk_emp_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_emp_dept FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE RESTRICT,
    CONSTRAINT fk_emp_team FOREIGN KEY (team_id) REFERENCES teams (id) ON DELETE SET NULL,
    CONSTRAINT fk_emp_supervisor FOREIGN KEY (supervisor_id) REFERENCES employees (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add supervisor constraint to teams after employees table exists
ALTER TABLE teams
    ADD CONSTRAINT fk_team_supervisor FOREIGN KEY (supervisor_id) REFERENCES employees (id) ON DELETE SET NULL;

-- 7. Employee Assignment History
CREATE TABLE IF NOT EXISTS employee_assignment_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    previous_department_id BIGINT NULL,
    new_department_id BIGINT NOT NULL,
    previous_team_id BIGINT NULL,
    new_team_id BIGINT NULL,
    previous_supervisor_id BIGINT NULL,
    new_supervisor_id BIGINT NULL,
    effective_from TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    effective_to TIMESTAMP NULL,
    assigned_by VARCHAR(100),
    reason TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    modified_by VARCHAR(100),
    CONSTRAINT fk_eah_emp FOREIGN KEY (employee_id) REFERENCES employees (id) ON DELETE CASCADE,
    CONSTRAINT fk_eah_prev_dept FOREIGN KEY (previous_department_id) REFERENCES departments (id) ON DELETE SET NULL,
    CONSTRAINT fk_eah_new_dept FOREIGN KEY (new_department_id) REFERENCES departments (id) ON DELETE RESTRICT,
    CONSTRAINT fk_eah_prev_team FOREIGN KEY (previous_team_id) REFERENCES teams (id) ON DELETE SET NULL,
    CONSTRAINT fk_eah_new_team FOREIGN KEY (new_team_id) REFERENCES teams (id) ON DELETE SET NULL,
    CONSTRAINT fk_eah_prev_sup FOREIGN KEY (previous_supervisor_id) REFERENCES employees (id) ON DELETE SET NULL,
    CONSTRAINT fk_eah_new_sup FOREIGN KEY (new_supervisor_id) REFERENCES employees (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Sample Seed Data for Cities & Municipalities
INSERT INTO cities (name, code, state, country, timezone) VALUES
    ('Bengaluru', 'BLR', 'Karnataka', 'India', 'Asia/Kolkata'),
    ('Mumbai', 'BOM', 'Maharashtra', 'India', 'Asia/Kolkata'),
    ('New Delhi', 'DEL', 'Delhi', 'India', 'Asia/Kolkata')
ON DUPLICATE KEY UPDATE state=VALUES(state);

INSERT INTO municipalities (city_id, name, code, type, contact_email, contact_phone, address, website) VALUES
    (1, 'Bruhat Bengaluru Mahanagara Palike', 'BBMP', 'MUNICIPAL_CORPORATION', 'commissioner@bbmp.gov.in', '+91-80-22221188', 'N.R. Square, Bengaluru, Karnataka 560002', 'https://bbmp.gov.in'),
    (2, 'Brihanmumbai Municipal Corporation', 'BMC', 'MUNICIPAL_CORPORATION', 'mc@mcgm.gov.in', '+91-22-22620251', 'Mahapalika Marg, Mumbai, Maharashtra 400001', 'https://portal.mcgm.gov.in'),
    (3, 'New Delhi Municipal Council', 'NDMC', 'MUNICIPAL_COUNCIL', 'chairperson@ndmc.gov.in', '+91-11-23360059', 'Palika Kendra, Sansad Marg, New Delhi 110001', 'https://ndmc.gov.in')
ON DUPLICATE KEY UPDATE type=VALUES(type);
