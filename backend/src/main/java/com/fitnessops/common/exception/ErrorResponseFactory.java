package com.fitnessops.common.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitnessops.common.constant.SecurityConstants;
import com.fitnessops.common.dto.ErrorResponse;
import com.fitnessops.common.dto.FieldErrorResponse;
import com.fitnessops.common.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/** Tạo phản hồi lỗi thống nhất cho cả tầng controller lẫn tầng bộ lọc bảo mật. */
@Component
@RequiredArgsConstructor
public class ErrorResponseFactory {

    private final ObjectMapper objectMapper;
    private final Clock clock;

    public ErrorResponse build(ErrorCode code, String message, Map<String, Object> details,
                               List<FieldErrorResponse> fieldErrors, HttpServletRequest request) {
        return new ErrorResponse(
                code.status().value(),
                code.name(),
                message,
                details,
                fieldErrors,
                request.getRequestURI(),
                Instant.now(clock),
                MDC.get(SecurityConstants.TRACE_ID_MDC_KEY));
    }

    /** Ghi lỗi trực tiếp vào phản hồi, dùng ở nơi chưa vào tới controller (bộ lọc, entry point). */
    public void write(HttpServletRequest request, HttpServletResponse response, ErrorCode code, String message)
            throws IOException {
        response.setStatus(code.status().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getOutputStream(), build(code, message, Map.of(), List.of(), request));
    }
}
