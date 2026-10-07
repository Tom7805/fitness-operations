package com.fitnessops.security.jwt;

import java.time.Instant;
import java.util.UUID;

/**
 * Nội dung đã kiểm chứng chữ ký của một mã truy cập.
 *
 * @param sessionId mã phiên ở máy chủ
 * @param userId    mã tài khoản
 * @param issuedAt  thời điểm cấp
 * @param expiresAt thời hạn tuyệt đối
 */
public record JwtClaims(UUID sessionId, Long userId, Instant issuedAt, Instant expiresAt) {
}
