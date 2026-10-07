package com.fitnessops.common.constant;

/** Hằng số dùng chung cho xác thực và phân quyền. */
public final class SecurityConstants {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String TOKEN_TYPE = "Bearer";

    /** Mã máy quầy lễ tân do trình duyệt của máy gửi kèm mọi yêu cầu. */
    public static final String DEVICE_TOKEN_HEADER = "X-Device-Token";

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String TRACE_ID_MDC_KEY = "traceId";

    public static final String ROLE_PREFIX = "ROLE_";

    private SecurityConstants() {
    }
}
