package com.fitnessops.modules.auth.service;

import com.fitnessops.common.web.ClientRequestInfo;
import com.fitnessops.modules.auth.enums.AuthEventReason;
import com.fitnessops.modules.auth.enums.AuthEventType;
import com.fitnessops.modules.auth.enums.ClientType;
import java.util.UUID;
import lombok.Builder;

/**
 * Một sự kiện cần ghi vào nhật ký đăng nhập.
 *
 * @param type       loại sự kiện
 * @param reason     mã lý do, có thể {@code null}
 * @param userId     người thực hiện nếu xác định được
 * @param username   tên đăng nhập đã nhập
 * @param sessionId  phiên liên quan
 * @param clientType loại thiết bị
 * @param deviceId   máy quầy liên quan
 * @param branchId   câu lạc bộ liên quan
 * @param client     địa chỉ IP và trình duyệt
 * @param detail     nội dung tiếng Việt
 */
@Builder
public record AuthAuditEvent(
        AuthEventType type,
        AuthEventReason reason,
        Long userId,
        String username,
        UUID sessionId,
        ClientType clientType,
        Long deviceId,
        Long branchId,
        ClientRequestInfo client,
        String detail) {
}
