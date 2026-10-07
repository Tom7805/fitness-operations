-- =====================================================================
-- NCL-01-CN-001 Đăng nhập hệ thống: tài khoản, vai trò, phiên, nhật ký đăng nhập (MySQL 8.0)
-- Mọi thời điểm lưu theo UTC trong cột DATETIME(6).
-- =====================================================================

-- Vai trò nhân viên (VT-01 … VT-13). VT-14 "Nhân viên phòng tập" là vai trò chung,
-- mọi tài khoản nhân viên đều có nên không cần dòng riêng.
CREATE TABLE roles (
    id                      BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code                    VARCHAR(50)  NOT NULL,
    name                    VARCHAR(100) NOT NULL,
    requires_counter_device BOOLEAN      NOT NULL DEFAULT FALSE
        COMMENT 'Vai trò chỉ được đăng nhập trên máy quầy đã đăng ký với câu lạc bộ (lễ tân)',
    sort_order              INT          NOT NULL DEFAULT 0,
    CONSTRAINT uq_roles_code UNIQUE (code)
) ENGINE = InnoDB COMMENT = 'Vai trò nhân viên';

INSERT INTO roles (code, name, requires_counter_device, sort_order) VALUES
    ('CHAIN_OWNER',      'Chủ phòng tập',               FALSE, 10),
    ('CLUB_MANAGER',     'Quản lý câu lạc bộ',          FALSE, 20),
    ('SALES_CONSULTANT', 'Nhân viên tư vấn',            FALSE, 30),
    ('RECEPTIONIST',     'Lễ tân',                      TRUE,  40),
    ('PERSONAL_TRAINER', 'Huấn luyện viên cá nhân',     FALSE, 50),
    ('GROUP_TRAINER',    'Huấn luyện viên lớp nhóm',    FALSE, 60),
    ('ACCOUNTANT',       'Kế toán',                     FALSE, 70),
    ('TECHNICIAN',       'Nhân viên kỹ thuật',          FALSE, 80),
    ('MEMBER_CARE',      'Nhân viên chăm sóc hội viên', FALSE, 90),
    ('ADMIN',            'Quản trị viên',               FALSE, 100);

CREATE TABLE users (
    id                    BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username              VARCHAR(50)  NOT NULL
        COMMENT 'Tên đăng nhập, luôn lưu chữ thường để so khớp không phân biệt hoa thường',
    password_hash         VARCHAR(100) NOT NULL COMMENT 'Bản băm BCrypt, không bao giờ lưu mật khẩu rõ',
    full_name             VARCHAR(150) NOT NULL,
    job_title             VARCHAR(100),
    phone_number          VARCHAR(20),
    status                VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    must_change_password  BOOLEAN      NOT NULL DEFAULT FALSE,
    all_branches          BOOLEAN      NOT NULL DEFAULT FALSE
        COMMENT 'Phạm vi toàn chuỗi; FALSE thì dùng user_branch_scopes',
    failed_login_attempts INT          NOT NULL DEFAULT 0 COMMENT 'Số lần nhập sai mật khẩu liên tiếp',
    locked_until          DATETIME(6)  COMMENT 'Thời điểm hết tạm khóa đăng nhập do nhập sai nhiều lần (UTC)',
    last_login_at         DATETIME(6),
    created_at            DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at            DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    version               BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT ck_users_username_format CHECK (REGEXP_LIKE(username, '^[a-z0-9._-]{3,50}$', 'c')),
    CONSTRAINT ck_users_status CHECK (status IN ('ACTIVE', 'DISABLED')),
    CONSTRAINT ck_users_failed_login_attempts CHECK (failed_login_attempts >= 0)
) ENGINE = InnoDB COMMENT = 'Tài khoản nhân viên';

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE = InnoDB;

-- Phiên làm việc lưu ở máy chủ để hết phiên khi không thao tác và chấm dứt phiên ngay khi cần.
-- Khóa ngoại tới counter_devices / branches được thêm ở V2.
CREATE TABLE user_sessions (
    id               CHAR(36)     NOT NULL PRIMARY KEY,
    user_id          BIGINT       NOT NULL,
    client_type      VARCHAR(20)  NOT NULL,
    device_id        BIGINT,
    branch_id        BIGINT,
    ip_address       VARCHAR(45),
    user_agent       VARCHAR(512),
    created_at       DATETIME(6)  NOT NULL,
    last_activity_at DATETIME(6)  NOT NULL,
    expires_at       DATETIME(6)  NOT NULL,
    ended_at         DATETIME(6),
    end_reason       VARCHAR(30),
    CONSTRAINT fk_user_sessions_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT ck_user_sessions_client_type CHECK (client_type IN ('COUNTER', 'OFFICE', 'MOBILE')),
    CONSTRAINT ck_user_sessions_counter_device CHECK ((client_type = 'COUNTER') = (device_id IS NOT NULL)),
    CONSTRAINT ck_user_sessions_end_reason
        CHECK (end_reason IN ('LOGOUT', 'IDLE_TIMEOUT', 'ABSOLUTE_TIMEOUT', 'REVOKED')),
    CONSTRAINT ck_user_sessions_ended CHECK ((ended_at IS NULL) = (end_reason IS NULL))
) ENGINE = InnoDB COMMENT = 'Phiên làm việc';

CREATE INDEX ix_user_sessions_user_open ON user_sessions (user_id, ended_at);

-- Nhật ký đăng nhập: chỉ thêm mới (QTN-02).
CREATE TABLE auth_audit_logs (
    id          BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    event_type  VARCHAR(40)   NOT NULL,
    reason_code VARCHAR(40),
    user_id     BIGINT,
    username    VARCHAR(100)  COMMENT 'Tên đăng nhập người dùng đã nhập, kể cả khi không tồn tại',
    session_id  CHAR(36),
    client_type VARCHAR(20),
    device_id   BIGINT,
    branch_id   BIGINT,
    ip_address  VARCHAR(45),
    user_agent  VARCHAR(512),
    detail      VARCHAR(1000) NOT NULL COMMENT 'Nội dung sự kiện bằng tiếng Việt',
    occurred_at DATETIME(6)   NOT NULL,
    CONSTRAINT fk_auth_audit_logs_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT ck_auth_audit_logs_event_type CHECK (event_type IN (
        'LOGIN_SUCCEEDED', 'LOGIN_FAILED', 'ACCOUNT_TEMPORARILY_LOCKED', 'LOGIN_REJECTED',
        'LOGOUT', 'SESSION_EXPIRED', 'DEVICE_REGISTERED'))
) ENGINE = InnoDB COMMENT = 'Nhật ký đăng nhập và phiên, chỉ thêm mới, không sửa xóa (QTN-02)';

CREATE INDEX ix_auth_audit_logs_user_time ON auth_audit_logs (user_id, occurred_at);
CREATE INDEX ix_auth_audit_logs_username_time ON auth_audit_logs (username, occurred_at);
CREATE INDEX ix_auth_audit_logs_time ON auth_audit_logs (occurred_at);

CREATE TRIGGER trg_auth_audit_logs_no_update BEFORE UPDATE ON auth_audit_logs FOR EACH ROW
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'auth_audit_logs chỉ cho phép thêm mới, không được UPDATE (QTN-02)';

CREATE TRIGGER trg_auth_audit_logs_no_delete BEFORE DELETE ON auth_audit_logs FOR EACH ROW
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'auth_audit_logs chỉ cho phép thêm mới, không được DELETE (QTN-02)';
