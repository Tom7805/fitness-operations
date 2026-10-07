package com.fitnessops.modules.auth.enums;

/** Lý do một phiên làm việc kết thúc. */
public enum SessionEndReason {
    LOGOUT,
    IDLE_TIMEOUT,
    ABSOLUTE_TIMEOUT,
    REVOKED
}
