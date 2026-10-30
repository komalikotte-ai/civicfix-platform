-- =============================================================================
-- CivicFix Platform - V9 Procurement & Vendor Management Schema (MySQL 8.0+)
-- Vendors verification & contracts, quotations, purchase orders, deliveries,
-- vendor evaluations/performance ratings, and procurement audit trail.
-- =============================================================================

-- 1. Alter suppliers table to support vendor verification & rating history
ALTER TABLE suppliers
    ADD COLUMN IF NOT EXISTS verification_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    ADD COLUMN IF NOT EXISTS verified_by VARCHAR(100) NULL,
    ADD COLUMN IF NOT EXISTS verified_at TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS verification_notes TEXT NULL,
    ADD COLUMN IF NOT EXISTS bank_account_no VARCHAR(50) NULL,
    ADD COLUMN IF NOT EXISTS bank_ifsc VARCHAR(20) NULL,
    ADD COLUMN IF NOT EXISTS vendor_type VARCHAR(50) NOT NULL DEFAULT 'SUPPLIER';

-- 2. Vendor Contracts
CREATE TABLE IF NOT EXISTS vendor_contracts (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    contract_number   VARCHAR(50)  NOT NULL UNIQUE,
    supplier_id       BIGINT       NOT NULL,
    department_id     BIGINT       NOT NULL,
    title             VARCHAR(200) NOT NULL,
    description       TEXT         NULL,
    contract_value    DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    start_date        DATE         NOT NULL,
    end_date          DATE         NOT NULL,
    status            VARCHAR(30)  NOT NULL DEFAULT 'DRAFT', -- DRAFT, ACTIVE, EXPIRED, TERMINATED
    terms_and_conditions TEXT      NULL,
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by        VARCHAR(100) NULL,
    modified_by       VARCHAR(100) NULL,
    CONSTRAINT fk_vc_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers (id) ON DELETE RESTRICT,
    CONSTRAINT fk_vc_dept     FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE RESTRICT,
    INDEX idx_vc_number (contract_number),
    INDEX idx_vc_supplier (supplier_id),
    INDEX idx_vc_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Quotations (Requests for Quotation and Vendor Bids)
CREATE TABLE IF NOT EXISTS quotations (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    quotation_number    VARCHAR(50)   NOT NULL UNIQUE,
    purchase_request_id BIGINT        NULL,
    supplier_id         BIGINT        NOT NULL,
    department_id       BIGINT        NOT NULL,
    total_amount        DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    status              VARCHAR(30)   NOT NULL DEFAULT 'SUBMITTED', -- SUBMITTED, ACCEPTED, REJECTED, EXPIRED
    valid_until         DATE          NULL,
    delivery_terms      TEXT          NULL,
    payment_terms       VARCHAR(100)  NULL,
    remarks             TEXT          NULL,
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by          VARCHAR(100)  NULL,
    modified_by         VARCHAR(100)  NULL,
    CONSTRAINT fk_q_pr   FOREIGN KEY (purchase_request_id) REFERENCES purchase_requests (id) ON DELETE SET NULL,
    CONSTRAINT fk_q_sup  FOREIGN KEY (supplier_id)         REFERENCES suppliers (id)         ON DELETE RESTRICT,
    CONSTRAINT fk_q_dept FOREIGN KEY (department_id)       REFERENCES departments (id)       ON DELETE RESTRICT,
    INDEX idx_q_number (quotation_number),
    INDEX idx_q_pr (purchase_request_id),
    INDEX idx_q_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Quotation Items
CREATE TABLE IF NOT EXISTS quotation_items (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    quotation_id  BIGINT        NOT NULL,
    item_id       BIGINT        NOT NULL,
    quantity      INT           NOT NULL,
    unit_price    DECIMAL(12,4) NOT NULL,
    total_price   DECIMAL(14,2) NOT NULL,
    specifications TEXT         NULL,
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_qi_quotation FOREIGN KEY (quotation_id) REFERENCES quotations (id) ON DELETE CASCADE,
    CONSTRAINT fk_qi_item      FOREIGN KEY (item_id)      REFERENCES inventory_items (id) ON DELETE RESTRICT,
    INDEX idx_qi_quotation (quotation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Purchase Orders
CREATE TABLE IF NOT EXISTS purchase_orders (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number        VARCHAR(50)   NOT NULL UNIQUE,
    purchase_request_id BIGINT        NULL,
    quotation_id        BIGINT        NULL,
    supplier_id         BIGINT        NOT NULL,
    department_id       BIGINT        NOT NULL,
    warehouse_id        BIGINT        NOT NULL,
    contract_id         BIGINT        NULL,
    order_date          DATE          NOT NULL,
    expected_delivery   DATE          NULL,
    total_amount        DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    status              VARCHAR(30)   NOT NULL DEFAULT 'DRAFT', -- DRAFT, PENDING_APPROVAL, APPROVED, REJECTED, ISSUED, PARTIALLY_DELIVERED, FULFILLED, CANCELLED
    approved_by         VARCHAR(100)  NULL,
    approved_at         TIMESTAMP     NULL,
    rejection_reason    TEXT          NULL,
    terms_and_conditions TEXT         NULL,
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by          VARCHAR(100)  NULL,
    modified_by         VARCHAR(100)  NULL,
    CONSTRAINT fk_po_pr   FOREIGN KEY (purchase_request_id) REFERENCES purchase_requests (id) ON DELETE SET NULL,
    CONSTRAINT fk_po_q    FOREIGN KEY (quotation_id)        REFERENCES quotations (id)        ON DELETE SET NULL,
    CONSTRAINT fk_po_sup  FOREIGN KEY (supplier_id)         REFERENCES suppliers (id)         ON DELETE RESTRICT,
    CONSTRAINT fk_po_dept FOREIGN KEY (department_id)       REFERENCES departments (id)       ON DELETE RESTRICT,
    CONSTRAINT fk_po_wh   FOREIGN KEY (warehouse_id)        REFERENCES warehouses (id)        ON DELETE RESTRICT,
    CONSTRAINT fk_po_vc   FOREIGN KEY (contract_id)         REFERENCES vendor_contracts (id)  ON DELETE SET NULL,
    INDEX idx_po_number (order_number),
    INDEX idx_po_sup (supplier_id),
    INDEX idx_po_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Purchase Order Items
CREATE TABLE IF NOT EXISTS purchase_order_items (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    purchase_order_id  BIGINT        NOT NULL,
    item_id            BIGINT        NOT NULL,
    quantity_ordered   INT           NOT NULL,
    quantity_received  INT           NOT NULL DEFAULT 0,
    unit_price         DECIMAL(12,4) NOT NULL,
    total_price        DECIMAL(14,2) NOT NULL,
    created_at         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_poi_po   FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_poi_item FOREIGN KEY (item_id)           REFERENCES inventory_items (id) ON DELETE RESTRICT,
    INDEX idx_poi_po (purchase_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Goods Receipt Deliveries
CREATE TABLE IF NOT EXISTS goods_receipt_deliveries (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    delivery_number     VARCHAR(50)  NOT NULL UNIQUE,
    purchase_order_id   BIGINT       NOT NULL,
    warehouse_id        BIGINT       NOT NULL,
    received_by         VARCHAR(100) NOT NULL,
    received_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delivery_challan_no VARCHAR(100) NULL,
    status              VARCHAR(30)  NOT NULL DEFAULT 'ACCEPTED', -- ACCEPTED, REJECTED, PARTIALLY_ACCEPTED
    inspection_notes    TEXT         NULL,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_grd_po FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders (id) ON DELETE RESTRICT,
    CONSTRAINT fk_grd_wh FOREIGN KEY (warehouse_id)      REFERENCES warehouses (id)      ON DELETE RESTRICT,
    INDEX idx_grd_number (delivery_number),
    INDEX idx_grd_po (purchase_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Goods Receipt Delivery Items
CREATE TABLE IF NOT EXISTS goods_receipt_delivery_items (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    delivery_id          BIGINT NOT NULL,
    po_item_id           BIGINT NOT NULL,
    item_id              BIGINT NOT NULL,
    quantity_delivered   INT    NOT NULL,
    quantity_accepted    INT    NOT NULL,
    quantity_rejected    INT    NOT NULL DEFAULT 0,
    rejection_reason     TEXT   NULL,
    batch_number         VARCHAR(50) NULL,
    created_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_grdi_del  FOREIGN KEY (delivery_id) REFERENCES goods_receipt_deliveries (id) ON DELETE CASCADE,
    CONSTRAINT fk_grdi_item FOREIGN KEY (item_id)     REFERENCES inventory_items (id) ON DELETE RESTRICT,
    INDEX idx_grdi_del (delivery_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Vendor Performance Evaluations
CREATE TABLE IF NOT EXISTS vendor_evaluations (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_id        BIGINT       NOT NULL,
    purchase_order_id  BIGINT       NULL,
    delivery_rating    DECIMAL(3,1) NOT NULL, -- 1.0 to 5.0
    quality_rating     DECIMAL(3,1) NOT NULL, -- 1.0 to 5.0
    pricing_rating     DECIMAL(3,1) NOT NULL, -- 1.0 to 5.0
    overall_rating     DECIMAL(3,1) NOT NULL, -- Average
    evaluator_comments TEXT         NULL,
    evaluated_by       VARCHAR(100) NOT NULL,
    evaluated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ve_sup FOREIGN KEY (supplier_id) REFERENCES suppliers (id) ON DELETE CASCADE,
    CONSTRAINT fk_ve_po  FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders (id) ON DELETE SET NULL,
    INDEX idx_ve_sup (supplier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Seed Procurement Permissions
INSERT INTO permissions (name, description) VALUES
    ('PROCUREMENT_READ',    'View vendor contracts, purchase orders, quotations, and vendor ratings'),
    ('PROCUREMENT_MANAGE',  'Register vendors, submit quotations, create purchase orders and contracts'),
    ('PROCUREMENT_APPROVE', 'Verify vendors, approve purchase orders and contracts'),
    ('DELIVERY_RECEIVE',    'Inspect and receive delivery goods receipts')
ON DUPLICATE KEY UPDATE description = VALUES(description);
