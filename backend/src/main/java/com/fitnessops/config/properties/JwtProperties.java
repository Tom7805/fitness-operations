package com.fitnessops.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Cấu hình ký mã truy cập.
 *
 * @param secret khóa HMAC-SHA256 mã hóa Base64, tối thiểu 256 bit (kiểm tra khi khởi động)
 * @param issuer giá trị {@code iss} của mã truy cập
 */
@Validated
@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
        @NotBlank(message = "app.security.jwt.secret (JWT_SECRET) phải được cấu hình") String secret,
        @NotBlank String issuer) {
}
