package com.fitnessops.modules.auth.dto.response;

import com.fitnessops.modules.auth.enums.ClientType;
import com.fitnessops.modules.branch.dto.response.BranchResponse;
import java.time.Instant;
import java.util.UUID;

/**
 * Phiên làm việc hiện tại.
 *
 * @param idleTimeoutSeconds phiên hết hạn sau ngần này giây không thao tác
 * @param expiresAt          thời hạn tuyệt đối của phiên
 * @param activeBranch       câu lạc bộ đang làm việc, trống khi người dùng có nhiều câu lạc bộ
 * @param device             máy quầy mà phiên gắn với, chỉ có ở phiên máy quầy
 */
public record SessionResponse(
        UUID sessionId,
        ClientType clientType,
        Instant startedAt,
        Instant expiresAt,
        long idleTimeoutSeconds,
        SessionUserResponse user,
        BranchResponse activeBranch,
        CounterDeviceResponse device) {
}
