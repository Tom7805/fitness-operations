package com.fitnessops.config.properties;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Nguồn gốc được phép gọi API từ trình duyệt.
 *
 * @param allowedOrigins danh sách origin, phân tách bằng dấu phẩy trong biến môi trường
 */
@ConfigurationProperties(prefix = "app.security.cors")
public record CorsProperties(List<String> allowedOrigins) {

    public CorsProperties {
        allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
    }
}
