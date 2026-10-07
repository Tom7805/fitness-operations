package com.fitnessops.modules.auth.service;

import com.fitnessops.common.enums.ErrorCode;
import com.fitnessops.security.userdetails.CustomUserDetails;

/**
 * Kết quả kiểm tra phiên của một yêu cầu: hoặc người dùng đã xác thực, hoặc mã lỗi kèm thông báo.
 */
public record SessionAuthentication(CustomUserDetails principal, ErrorCode errorCode, String message) {

    public static SessionAuthentication authenticated(CustomUserDetails principal) {
        return new SessionAuthentication(principal, null, null);
    }

    public static SessionAuthentication rejected(ErrorCode errorCode, String message) {
        return new SessionAuthentication(null, errorCode, message);
    }

    public boolean isAuthenticated() {
        return principal != null;
    }
}
