package com.fitnessops.modules.auth.service;

import com.fitnessops.common.web.ClientRequestInfo;
import com.fitnessops.modules.auth.enums.AuthEventReason;
import com.fitnessops.modules.auth.enums.AuthEventType;
import com.fitnessops.modules.auth.enums.ClientType;
import java.util.UUID;
import lombok.Builder;

/**
 * Một sự kiện cần ghi vào nhật ký đăng nhập: loại sự kiện và mã lý do, người thực hiện (mã tài khoản nếu xác định
 * được, tên đăng nhập đã nhập), phiên, loại thiết bị, máy quầy, câu lạc bộ, địa chỉ IP và trình duyệt
 * ({@code client}) cùng nội dung tiếng Việt ({@code detail}).
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
