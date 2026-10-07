package com.fitnessops.modules.auth.entity;

import com.fitnessops.modules.auth.enums.ClientType;
import com.fitnessops.modules.auth.enums.SessionEndReason;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Phiên làm việc lưu ở máy chủ. Mã truy cập chỉ mang mã phiên; máy chủ quyết định phiên còn hiệu lực hay không. */
@Entity
@Table(name = "user_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSession {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "client_type", nullable = false, updatable = false, length = 20)
    private ClientType clientType;

    /** Máy quầy mà phiên gắn với; chỉ có ở phiên {@link ClientType#COUNTER}. */
    @Column(name = "device_id", updatable = false)
    private Long deviceId;

    /** Câu lạc bộ đang làm việc; trống khi người dùng có nhiều câu lạc bộ hoặc phạm vi toàn chuỗi. */
    @Column(name = "branch_id", updatable = false)
    private Long branchId;

    @Column(name = "ip_address", updatable = false, length = 45)
    private String ipAddress;

    @Column(name = "user_agent", updatable = false, length = 512)
    private String userAgent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "last_activity_at", nullable = false)
    private Instant lastActivityAt;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "end_reason", length = 30)
    private SessionEndReason endReason;

    /** Mở phiên mới. Phiên máy quầy bắt buộc có {@code deviceId}, các loại khác thì không. */
    public static UserSession open(Long userId, ClientType clientType, Long deviceId, Long branchId,
                                   String ipAddress, String userAgent, Instant now, Duration absoluteTimeout) {
        if ((clientType == ClientType.COUNTER) != (deviceId != null)) {
            throw new IllegalArgumentException("Counter sessions must, and only they may, be bound to a device");
        }
        UserSession session = new UserSession();
        session.id = UUID.randomUUID();
        session.userId = userId;
        session.clientType = clientType;
        session.deviceId = deviceId;
        session.branchId = branchId;
        session.ipAddress = ipAddress;
        session.userAgent = userAgent;
        session.createdAt = now;
        session.lastActivityAt = now;
        session.expiresAt = now.plus(absoluteTimeout);
        return session;
    }

    public boolean isEnded() {
        return endedAt != null;
    }

    /** Không có thao tác nào trong khoảng {@code idleTimeout} kể từ lần thao tác cuối. */
    public boolean isIdleExpired(Instant now, Duration idleTimeout) {
        return !now.isBefore(lastActivityAt.plus(idleTimeout));
    }

    public boolean isAbsoluteExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    public void touch(Instant now) {
        if (now.isAfter(lastActivityAt)) {
            lastActivityAt = now;
        }
    }

    public void end(SessionEndReason reason, Instant now) {
        if (endedAt == null) {
            endedAt = now;
            endReason = reason;
        }
    }
}
