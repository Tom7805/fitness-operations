package com.fitnessops.config.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Chính sách đăng nhập và phiên làm việc (NCL-01-CN-001, mục 3 của tài liệu phân tích).
 *
 * @param maxFailedAttempts số lần nhập sai mật khẩu liên tiếp trước khi tạm khóa
 * @param lockDuration      thời gian tạm khóa đăng nhập
 * @param idleTimeout       phiên hết hạn khi không thao tác trong khoảng này
 * @param absoluteTimeout   thời hạn tối đa của một phiên kể từ lúc đăng nhập
 */
@Validated
@ConfigurationProperties(prefix = "app.security.auth")
public record AuthProperties(
        @Min(1) int maxFailedAttempts,
        @NotNull Duration lockDuration,
        @NotNull Duration idleTimeout,
        @NotNull Duration absoluteTimeout) {
}
