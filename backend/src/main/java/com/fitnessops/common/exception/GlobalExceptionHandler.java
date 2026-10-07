package com.fitnessops.common.exception;

import com.fitnessops.common.dto.ErrorResponse;
import com.fitnessops.common.dto.FieldErrorResponse;
import com.fitnessops.common.enums.ErrorCode;
import com.fitnessops.common.logging.LogSanitizer;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** Chuyển mọi ngoại lệ thành {@link ErrorResponse}; không bao giờ lộ stack trace ra ngoài. */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    /** Khóa trong {@code details} được chép sang header {@code Retry-After}. */
    public static final String RETRY_AFTER_SECONDS = "retryAfterSeconds";

    private final ErrorResponseFactory errorResponseFactory;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest request) {
        ErrorCode code = ex.getErrorCode();
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(code.status());
        Object retryAfter = ex.getDetails().get(RETRY_AFTER_SECONDS);
        if (retryAfter != null) {
            builder.header(HttpHeaders.RETRY_AFTER, retryAfter.toString());
        }
        return builder.body(errorResponseFactory.build(code, ex.getMessage(), ex.getDetails(), List.of(), request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        List<FieldErrorResponse> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();
        return respond(ErrorCode.VALIDATION_ERROR, fieldErrors, request);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
        MissingRequestHeaderException.class, HttpMediaTypeNotSupportedException.class})
    public ResponseEntity<ErrorResponse> handleMalformed(Exception ex, HttpServletRequest request) {
        log.debug("Malformed request on {}: {}", LogSanitizer.sanitize(request.getRequestURI()),
                LogSanitizer.sanitize(ex.getMessage()));
        return respond(ErrorCode.MALFORMED_REQUEST, List.of(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(HttpServletRequest request) {
        return respond(ErrorCode.FORBIDDEN, List.of(), request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(HttpServletRequest request) {
        return respond(ErrorCode.NOT_FOUND, List.of(), request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpServletRequest request) {
        return respond(ErrorCode.METHOD_NOT_ALLOWED, List.of(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error on {} {}", LogSanitizer.sanitize(request.getMethod()),
                LogSanitizer.sanitize(request.getRequestURI()), ex);
        return respond(ErrorCode.INTERNAL_ERROR, List.of(), request);
    }

    private ResponseEntity<ErrorResponse> respond(ErrorCode code, List<FieldErrorResponse> fieldErrors,
                                                  HttpServletRequest request) {
        return ResponseEntity.status(code.status())
                .body(errorResponseFactory.build(code, code.defaultMessage(), Map.of(), fieldErrors, request));
    }

    private FieldErrorResponse toFieldError(FieldError error) {
        return new FieldErrorResponse(error.getField(), error.getDefaultMessage());
    }
}
