package com.fitnessops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/** Tài khoản được xác thực bởi module auth, không dùng UserDetailsService mặc định của Spring Boot. */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@ConfigurationPropertiesScan
public class FitnessOperationsApplication {

    public static void main(String[] args) {
        SpringApplication.run(FitnessOperationsApplication.class, args);
    }
}
