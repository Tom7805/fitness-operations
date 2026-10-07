package com.fitnessops.modules.auth.enums;

/** Loại sự kiện trong nhật ký đăng nhập (mục R7 của tài liệu phân tích). */
public enum AuthEventType {
    LOGIN_SUCCEEDED,
    LOGIN_FAILED,
    ACCOUNT_TEMPORARILY_LOCKED,
    LOGIN_REJECTED,
    LOGOUT,
    SESSION_EXPIRED,
    DEVICE_REGISTERED
}
