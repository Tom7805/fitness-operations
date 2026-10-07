package com.fitnessops.modules.auth.dto.response;

import java.time.Instant;

/**
 * Kết quả đăng nhập thành công.
 *
 * @param accessToken mã truy cập gửi kèm header {@code Authorization: Bearer}
 * @param tokenType   luôn là {@code Bearer}
 * @param expiresAt   thời hạn tuyệt đối của mã; phiên vẫn hết sớm hơn nếu không thao tác
 * @param session     phiên vừa mở
 */
public record AuthResponse(String accessToken, String tokenType, Instant expiresAt, SessionResponse session) {

    @Override
    public String toString() {
        return "AuthResponse[accessToken=***, tokenType=" + tokenType + ", expiresAt=" + expiresAt
                + ", session=" + session + "]";
    }
}
