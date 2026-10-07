package com.fitnessops.support;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/** Cấu hình chung cho kiểm thử tích hợp trên MySQL thật. */
@TestConfiguration(proxyBeanMethods = false)
public class IntegrationTestConfig {

    /**
     * Xóa sạch database kiểm thử rồi chạy lại toàn bộ migration ở đầu mỗi lần chạy, để kiểm thử luôn bắt đầu từ
     * lược đồ mới nhất và không phụ thuộc lần chạy trước. Chỉ áp dụng cho profile test (clean-disabled=false).
     */
    @Bean
    FlywayMigrationStrategy cleanMigrateStrategy() {
        return flyway -> {
            flyway.clean();
            flyway.migrate();
        };
    }

    @Bean
    @Primary
    MutableClock mutableClock() {
        return new MutableClock();
    }
}
