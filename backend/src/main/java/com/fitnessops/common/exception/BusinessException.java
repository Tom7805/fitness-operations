package com.fitnessops.common.exception;

import com.fitnessops.common.enums.ErrorCode;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;

/** Lỗi nghiệp vụ có mã ổn định, được {@link GlobalExceptionHandler} chuyển thành phản hồi JSON. */
@Getter
public class BusinessException extends RuntimeException {

    private final transient ErrorCode errorCode;
    private final transient Map<String, Object> details;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, errorCode.defaultMessage(), Map.of());
    }

    public BusinessException(ErrorCode errorCode, String message) {
        this(errorCode, message, Map.of());
    }

    public BusinessException(ErrorCode errorCode, String message, Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.details = Collections.unmodifiableMap(new LinkedHashMap<>(details));
    }
}
