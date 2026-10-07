package com.fitnessops.modules.auth.entity;

import com.fitnessops.modules.auth.enums.AuthEventReason;
import com.fitnessops.modules.auth.enums.AuthEventType;
import com.fitnessops.modules.auth.enums.ClientType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Một dòng nhật ký đăng nhập. Chỉ thêm mới: thực thể bất biến ở tầng ứng dụng và cơ sở dữ liệu chặn UPDATE, DELETE
 * bằng trigger (QTN-02). Tài khoản cơ sở dữ liệu ở môi trường thật không được có quyền DROP để chặn TRUNCATE.
 */
@Entity
@Immutable
@Table(name = "auth_audit_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, updatable = false, length = 40)
    private AuthEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_code", updatable = false, length = 40)
    private AuthEventReason reasonCode;

    /** Người thực hiện, nếu xác định được tài khoản. */
    @Column(name = "user_id", updatable = false)
    private Long userId;

    /** Tên đăng nhập người dùng đã nhập. */
    @Column(updatable = false, length = 100)
    private String username;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "session_id", updatable = false, length = 36)
    private UUID sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "client_type", updatable = false, length = 20)
    private ClientType clientType;

    @Column(name = "device_id", updatable = false)
    private Long deviceId;

    @Column(name = "branch_id", updatable = false)
    private Long branchId;

    @Column(name = "ip_address", updatable = false, length = 45)
    private String ipAddress;

    @Column(name = "user_agent", updatable = false, length = 512)
    private String userAgent;

    /** Nội dung sự kiện bằng tiếng Việt. */
    @Column(nullable = false, updatable = false, length = 1000)
    private String detail;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @Builder
    private AuthAuditLog(AuthEventType eventType, AuthEventReason reasonCode, Long userId, String username,
                         UUID sessionId, ClientType clientType, Long deviceId, Long branchId, String ipAddress,
                         String userAgent, String detail, Instant occurredAt) {
        this.eventType = eventType;
        this.reasonCode = reasonCode;
        this.userId = userId;
        this.username = username;
        this.sessionId = sessionId;
        this.clientType = clientType;
        this.deviceId = deviceId;
        this.branchId = branchId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.detail = detail;
        this.occurredAt = occurredAt;
    }
}
