-- =============================================================================
-- CivicFix Platform - V8 Inventory & Warehouses Schema (MySQL 8.0+)
-- Warehouses, item catalog, stock, transactions, work-order material usage,
-- supplier catalog, and purchase requests.
-- =============================================================================

-- 1. Warehouses / Stores
CREATE TABLE IF NOT EXISTS warehouses (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_code VARCHAR(30)  NOT NULL UNIQUE,
    name           VARCHAR(150) NOT NULL,
    description    TEXT         NULL,
    address        TEXT         NULL,
    latitude       DECIMAL(10,8) NULL,
    longitude      DECIMAL(11,8) NULL,
    warehouse_type VARCHAR(30)  NOT NULL DEFAULT 'GENERAL',
    status         VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    department_id  BIGINT       NOT NULL,
    organization_id BIGINT      NOT NULL,
    manager_id     BIGINT       NULL,
    capacity_notes TEXT         NULL,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by     VARCHAR(100) NULL,
    modified_by    VARCHAR(100) NULL,
    CONSTRAINT fk_wh_dept FOREIGN KEY (department_id)   REFERENCES departments  (id) ON DELETE RESTRICT,
    CONSTRAINT fk_wh_org  FOREIGN KEY (organization_id) REFERENCES organizations (id) ON DELETE RESTRICT,
    CONSTRAINT fk_wh_mgr  FOREIGN KEY (manager_id)      REFERENCES employees     (id) ON DELETE SET NULL,
    INDEX idx_wh_code   (warehouse_code),
    INDEX idx_wh_dept   (department_id),
    INDEX idx_wh_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Item Catalog (Spare parts, consumables, tools, materials)
CREATE TABLE IF NOT EXISTS inventory_items (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_code       VARCHAR(50)  NOT NULL UNIQUE,
    name            VARCHAR(200) NOT NULL,
    description     TEXT         NULL,
    item_category   VARCHAR(50)  NOT NULL DEFAULT 'GENERAL',
    unit_of_measure VARCHAR(30)  NOT NULL DEFAULT 'UNIT',
    unit_price      DECIMAL(12,4) NOT NULL DEFAULT 0.0000,
    minimum_stock   INT          NOT NULL DEFAULT 0,
    reorder_level   INT          NOT NULL DEFAULT 0,
    maximum_stock   INT          NULL,
    hsn_sac_code    VARCHAR(20)  NULL,
    specifications  TEXT         NULL,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by      VARCHAR(100) NULL,
    modified_by     VARCHAR(100) NULL,
    INDEX idx_item_code     (item_code),
    INDEX idx_item_category (item_category),
    INDEX idx_item_active   (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Warehouse Stock (per warehouse stock level for each item)
CREATE TABLE IF NOT EXISTS warehouse_stock (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_id  BIGINT NOT NULL,
    item_id       BIGINT NOT NULL,
    quantity      INT    NOT NULL DEFAULT 0,
    reserved_qty  INT    NOT NULL DEFAULT 0,
    location_code VARCHAR(50) NULL,
    batch_number  VARCHAR(50) NULL,
    expiry_date   DATE        NULL,
    last_counted_at TIMESTAMP NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_stock_wh   FOREIGN KEY (warehouse_id) REFERENCES warehouses      (id) ON DELETE CASCADE,
    CONSTRAINT fk_stock_item FOREIGN KEY (item_id)      REFERENCES inventory_items (id) ON DELETE CASCADE,
    CONSTRAINT uq_wh_item    UNIQUE (warehouse_id, item_id),
    INDEX idx_stock_wh   (warehouse_id),
    INDEX idx_stock_item (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Stock Transactions (IN / OUT / TRANSFER / ADJUSTMENT)
CREATE TABLE IF NOT EXISTS stock_transactions (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_code VARCHAR(50) NOT NULL UNIQUE,
    warehouse_id     BIGINT      NOT NULL,
    item_id          BIGINT      NOT NULL,
    transaction_type VARCHAR(30) NOT NULL,
    quantity         INT         NOT NULL,
    unit_price       DECIMAL(12,4) NOT NULL DEFAULT 0.0000,
    total_value      DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    reference_type   VARCHAR(50) NULL,
    reference_id     BIGINT      NULL,
    work_order_id    BIGINT      NULL,
    to_warehouse_id  BIGINT      NULL,
    remarks          TEXT        NULL,
    transaction_date TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    performed_by_id  BIGINT      NULL,
    created_at       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by       VARCHAR(100) NULL,
    CONSTRAINT fk_txn_wh       FOREIGN KEY (warehouse_id)    REFERENCES warehouses      (id) ON DELETE RESTRICT,
    CONSTRAINT fk_txn_item     FOREIGN KEY (item_id)         REFERENCES inventory_items (id) ON DELETE RESTRICT,
    CONSTRAINT fk_txn_wo       FOREIGN KEY (work_order_id)   REFERENCES work_orders     (id) ON DELETE SET NULL,
    CONSTRAINT fk_txn_to_wh    FOREIGN KEY (to_warehouse_id) REFERENCES warehouses      (id) ON DELETE SET NULL,
    CONSTRAINT fk_txn_perf     FOREIGN KEY (performed_by_id) REFERENCES employees       (id) ON DELETE SET NULL,
    INDEX idx_txn_code    (transaction_code),
    INDEX idx_txn_wh      (warehouse_id),
    INDEX idx_txn_item    (item_id),
    INDEX idx_txn_wo      (work_order_id),
    INDEX idx_txn_type    (transaction_type),
    INDEX idx_txn_date    (transaction_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Work-Order Material Usage (items consumed by a work order)
CREATE TABLE IF NOT EXISTS work_order_material_usage (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    work_order_id BIGINT      NOT NULL,
    item_id       BIGINT      NOT NULL,
    warehouse_id  BIGINT      NOT NULL,
    quantity_used INT         NOT NULL,
    unit_price    DECIMAL(12,4) NOT NULL DEFAULT 0.0000,
    total_cost    DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    usage_notes   TEXT        NULL,
    recorded_by   VARCHAR(100) NULL,
    recorded_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mat_wo   FOREIGN KEY (work_order_id) REFERENCES work_orders     (id) ON DELETE CASCADE,
    CONSTRAINT fk_mat_item FOREIGN KEY (item_id)       REFERENCES inventory_items (id) ON DELETE RESTRICT,
    CONSTRAINT fk_mat_wh   FOREIGN KEY (warehouse_id)  REFERENCES warehouses      (id) ON DELETE RESTRICT,
    INDEX idx_mat_wo   (work_order_id),
    INDEX idx_mat_item (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Suppliers
CREATE TABLE IF NOT EXISTS suppliers (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_code  VARCHAR(30)  NOT NULL UNIQUE,
    name           VARCHAR(200) NOT NULL,
    contact_person VARCHAR(100) NULL,
    email          VARCHAR(150) NULL,
    phone          VARCHAR(25)  NULL,
    address        TEXT         NULL,
    gstin          VARCHAR(20)  NULL,
    pan            VARCHAR(15)  NULL,
    payment_terms  VARCHAR(100) NULL,
    lead_time_days INT          NULL DEFAULT 7,
    rating         DECIMAL(3,1) NULL DEFAULT 0.0,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by     VARCHAR(100) NULL,
    modified_by    VARCHAR(100) NULL,
    INDEX idx_supplier_code (supplier_code),
    INDEX idx_supplier_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Supplier-Item Catalog (which suppliers provide which items, at what price)
CREATE TABLE IF NOT EXISTS supplier_items (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_id  BIGINT        NOT NULL,
    item_id      BIGINT        NOT NULL,
    unit_price   DECIMAL(12,4) NOT NULL,
    lead_time_days INT          NULL,
    preferred    BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_si_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers      (id) ON DELETE CASCADE,
    CONSTRAINT fk_si_item     FOREIGN KEY (item_id)     REFERENCES inventory_items (id) ON DELETE CASCADE,
    CONSTRAINT uq_supplier_item UNIQUE (supplier_id, item_id),
    INDEX idx_si_supplier (supplier_id),
    INDEX idx_si_item     (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Purchase Requests
CREATE TABLE IF NOT EXISTS purchase_requests (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_code      VARCHAR(50)  NOT NULL UNIQUE,
    title             VARCHAR(200) NOT NULL,
    justification     TEXT         NULL,
    status            VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    priority          VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    department_id     BIGINT       NOT NULL,
    organization_id   BIGINT       NOT NULL,
    warehouse_id      BIGINT       NULL,
    requested_by_id   BIGINT       NULL,
    approved_by       VARCHAR(100) NULL,
    approved_at       TIMESTAMP    NULL,
    rejection_reason  TEXT         NULL,
    expected_delivery DATE         NULL,
    total_estimated_cost DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    work_order_id     BIGINT       NULL,
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by        VARCHAR(100) NULL,
    modified_by       VARCHAR(100) NULL,
    CONSTRAINT fk_pr_dept  FOREIGN KEY (department_id)   REFERENCES departments  (id) ON DELETE RESTRICT,
    CONSTRAINT fk_pr_org   FOREIGN KEY (organization_id) REFERENCES organizations (id) ON DELETE RESTRICT,
    CONSTRAINT fk_pr_wh    FOREIGN KEY (warehouse_id)    REFERENCES warehouses   (id) ON DELETE SET NULL,
    CONSTRAINT fk_pr_req   FOREIGN KEY (requested_by_id) REFERENCES employees    (id) ON DELETE SET NULL,
    CONSTRAINT fk_pr_wo    FOREIGN KEY (work_order_id)   REFERENCES work_orders  (id) ON DELETE SET NULL,
    INDEX idx_pr_code   (request_code),
    INDEX idx_pr_dept   (department_id),
    INDEX idx_pr_status (status),
    INDEX idx_pr_wo     (work_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Purchase Request Line Items
CREATE TABLE IF NOT EXISTS purchase_request_items (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    purchase_request_id  BIGINT        NOT NULL,
    item_id              BIGINT        NOT NULL,
    quantity_requested   INT           NOT NULL,
    quantity_approved    INT           NULL,
    unit_price_estimate  DECIMAL(12,4) NOT NULL DEFAULT 0.0000,
    total_estimated_cost DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    supplier_id          BIGINT        NULL,
    remarks              TEXT          NULL,
    created_at           TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pri_pr       FOREIGN KEY (purchase_request_id) REFERENCES purchase_requests (id) ON DELETE CASCADE,
    CONSTRAINT fk_pri_item     FOREIGN KEY (item_id)             REFERENCES inventory_items   (id) ON DELETE RESTRICT,
    CONSTRAINT fk_pri_supplier FOREIGN KEY (supplier_id)         REFERENCES suppliers         (id) ON DELETE SET NULL,
    INDEX idx_pri_pr   (purchase_request_id),
    INDEX idx_pri_item (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Seed Inventory Permissions
INSERT INTO permissions (name, description) VALUES
    ('INVENTORY_READ',       'View warehouse stock, item catalog, and transactions'),
    ('INVENTORY_MANAGE',     'Create and manage warehouses, items, and suppliers'),
    ('INVENTORY_TRANSACT',   'Record stock-in, stock-out, and transfers'),
    ('PURCHASE_REQUEST',     'Create and submit purchase requests'),
    ('PURCHASE_APPROVE',     'Approve or reject purchase requests')
ON DUPLICATE KEY UPDATE description = VALUES(description);
