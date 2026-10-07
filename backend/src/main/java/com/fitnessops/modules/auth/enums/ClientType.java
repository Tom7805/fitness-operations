package com.fitnessops.modules.auth.enums;

/** Loại thiết bị của một phiên làm việc. */
public enum ClientType {

    /** Máy quầy lễ tân đã đăng ký với một câu lạc bộ; máy chủ tự xác định qua mã máy quầy. */
    COUNTER("máy quầy"),
    /** Máy tính văn phòng. */
    OFFICE("máy tính văn phòng"),
    /** Điện thoại. */
    MOBILE("điện thoại");

    private final String label;

    ClientType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
