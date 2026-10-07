package com.fitnessops.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Cấu trúc lỗi thống nhất của mọi API (docs/api/error-codes.md). */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(
        int status,
        String code,
        String message,
        Map<String, Object> details,
        List<FieldErrorResponse> fieldErrors,
        String path,
        Instant timestamp,
        String traceId) {
}
