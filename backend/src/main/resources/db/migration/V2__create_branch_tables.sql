-- =====================================================================
-- Câu lạc bộ (bản tối thiểu cho đăng nhập), phạm vi câu lạc bộ của tài khoản, máy quầy lễ tân (MySQL 8.0).
-- NCL-02-CN-001 bổ sung địa chỉ, giờ hoạt động, khu tập bằng migration mới.
-- =====================================================================

CREATE TABLE branches (
    id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code       VARCHAR(20)  NOT NULL,
    name       VARCHAR(150) NOT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT uq_branches_code UNIQUE (code),
    -- Collation utf8mb4_0900_ai_ci: tên trùng không phân biệt hoa thường.
    CONSTRAINT uq_branches_name UNIQUE (name),
    CONSTRAINT ck_branches_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
) ENGINE = InnoDB COMMENT = 'Câu lạc bộ';

-- Câu lạc bộ được giao cho tài khoản (khi users.all_branches = FALSE). NCL-01-CN-004 hoàn thiện phân quyền.
CREATE TABLE user_branch_scopes (
    user_id   BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, branch_id),
    CONSTRAINT fk_user_branch_scopes_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_branch_scopes_branch FOREIGN KEY (branch_id) REFERENCES branches (id)
) ENGINE = InnoDB;

-- Máy quầy lễ tân đã đăng ký với một câu lạc bộ. Chỉ lưu SHA-256 của mã máy quầy.
CREATE TABLE counter_devices (
    id                     BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    branch_id              BIGINT       NOT NULL,
    name                   VARCHAR(100) NOT NULL,
    token_hash             VARCHAR(64)  NOT NULL,
    status                 VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    registered_by_user_id  BIGINT       NOT NULL,
    registered_at          DATETIME(6)  NOT NULL,
    last_seen_at           DATETIME(6),
    revoked_at             DATETIME(6),
    CONSTRAINT fk_counter_devices_branch FOREIGN KEY (branch_id) REFERENCES branches (id),
    CONSTRAINT fk_counter_devices_registered_by FOREIGN KEY (registered_by_user_id) REFERENCES users (id),
    CONSTRAINT uq_counter_devices_token_hash UNIQUE (token_hash),
    CONSTRAINT ck_counter_devices_token_hash CHECK (REGEXP_LIKE(token_hash, '^[0-9a-f]{64}$', 'c')),
    CONSTRAINT ck_counter_devices_status CHECK (status IN ('ACTIVE', 'REVOKED')),
    CONSTRAINT ck_counter_devices_revoked CHECK ((status = 'REVOKED') = (revoked_at IS NOT NULL))
) ENGINE = InnoDB COMMENT = 'Máy quầy lễ tân';

-- Tên máy duy nhất trong câu lạc bộ, chỉ xét các máy đang hoạt động (máy đã thu hồi cho giá trị NULL).
CREATE UNIQUE INDEX uq_counter_devices_branch_name_active
    ON counter_devices (branch_id, (CASE WHEN status = 'ACTIVE' THEN name END));

ALTER TABLE user_sessions
    ADD CONSTRAINT fk_user_sessions_device FOREIGN KEY (device_id) REFERENCES counter_devices (id),
    ADD CONSTRAINT fk_user_sessions_branch FOREIGN KEY (branch_id) REFERENCES branches (id);

ALTER TABLE auth_audit_logs
    ADD CONSTRAINT fk_auth_audit_logs_device FOREIGN KEY (device_id) REFERENCES counter_devices (id),
    ADD CONSTRAINT fk_auth_audit_logs_branch FOREIGN KEY (branch_id) REFERENCES branches (id);
