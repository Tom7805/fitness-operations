package com.fitnessops.modules.auth.entity;

import com.fitnessops.modules.auth.enums.DeviceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Máy quầy lễ tân đã đăng ký với một câu lạc bộ. Máy chủ chỉ giữ bản băm SHA-256 của mã máy quầy. */
@Entity
@Table(name = "counter_devices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CounterDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "branch_id", nullable = false, updatable = false)
    private Long branchId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "token_hash", nullable = false, unique = true, updatable = false, length = 64)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeviceStatus status;

    @Column(name = "registered_by_user_id", nullable = false, updatable = false)
    private Long registeredByUserId;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private Instant registeredAt;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    public static CounterDevice register(Long branchId, String name, String tokenHash, Long registeredByUserId,
                                         Instant now) {
        CounterDevice device = new CounterDevice();
        device.branchId = branchId;
        device.name = name;
        device.tokenHash = tokenHash;
        device.status = DeviceStatus.ACTIVE;
        device.registeredByUserId = registeredByUserId;
        device.registeredAt = now;
        return device;
    }

    public boolean isActive() {
        return status == DeviceStatus.ACTIVE;
    }

    public void markSeen(Instant now) {
        lastSeenAt = now;
    }

    public void revoke(Instant now) {
        if (isActive()) {
            status = DeviceStatus.REVOKED;
            revokedAt = now;
        }
    }
}
