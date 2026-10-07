package com.fitnessops.config;

import com.fitnessops.common.constant.SecurityConstants;
import com.fitnessops.config.properties.CorsProperties;
import java.time.Duration;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/** CORS cho giao diện chạy khác origin; ở môi trường triển khai giao diện và API cùng origin qua Nginx. */
@Configuration(proxyBeanMethods = false)
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties properties) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(properties.allowedOrigins());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT,
                SecurityConstants.DEVICE_TOKEN_HEADER, SecurityConstants.REQUEST_ID_HEADER));
        config.setExposedHeaders(List.of(SecurityConstants.REQUEST_ID_HEADER, HttpHeaders.RETRY_AFTER));
        config.setAllowCredentials(false);
        config.setMaxAge(Duration.ofHours(1));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
