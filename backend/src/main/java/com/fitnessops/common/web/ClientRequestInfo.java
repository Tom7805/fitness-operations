package com.fitnessops.common.web;

import com.fitnessops.common.constant.SecurityConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * Thông tin về nơi gửi yêu cầu, dùng cho phiên làm việc và nhật ký.
 *
 * @param ipAddress   địa chỉ IP của máy gửi (đã qua cấu hình proxy tin cậy của máy chủ)
 * @param userAgent   chuỗi nhận diện trình duyệt, cắt tối đa {@value #MAX_USER_AGENT_LENGTH} ký tự
 * @param deviceToken mã máy quầy lễ tân nếu trình duyệt gửi kèm, có thể {@code null}
 */
public record ClientRequestInfo(String ipAddress, String userAgent, String deviceToken) {

    public static final int MAX_USER_AGENT_LENGTH = 512;
    private static final int MAX_DEVICE_TOKEN_LENGTH = 128;

    public static ClientRequestInfo from(HttpServletRequest request) {
        return new ClientRequestInfo(
                request.getRemoteAddr(),
                truncate(request.getHeader("User-Agent"), MAX_USER_AGENT_LENGTH),
                sanitizeDeviceToken(request.getHeader(SecurityConstants.DEVICE_TOKEN_HEADER)));
    }

    private static String sanitizeDeviceToken(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String token = raw.trim();
        return token.length() > MAX_DEVICE_TOKEN_LENGTH ? null : token;
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
