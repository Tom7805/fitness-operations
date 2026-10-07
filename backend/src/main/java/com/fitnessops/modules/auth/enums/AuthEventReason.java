package com.fitnessops.modules.auth.enums;

/** Mã lý do kèm theo sự kiện đăng nhập không thành công hoặc kết thúc phiên. */
public enum AuthEventReason {
    UNKNOWN_USERNAME,
    INVALID_PASSWORD,
    TOO_MANY_FAILED_ATTEMPTS,
    ACCOUNT_LOCKED,
    ACCOUNT_DISABLED,
    DEVICE_NOT_REGISTERED,
    DEVICE_BRANCH_NOT_ASSIGNED,
    IDLE_TIMEOUT
}
