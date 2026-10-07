package com.fitnessops.config;

import com.fitnessops.common.constant.SecurityConstants;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Tài liệu Swagger UI có sẵn ô nhập mã truy cập và mã máy quầy. */
@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    private static final String BEARER = "bearerAuth";
    private static final String DEVICE = "counterDevice";

    @Bean
    public OpenAPI fitnessOperationsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Fitness Operations API")
                        .description("Vận hành phòng tập thể hình từ buổi tập thử tới hội viên gắn bó lâu dài")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes(BEARER, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"))
                        .addSecuritySchemes(DEVICE, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name(SecurityConstants.DEVICE_TOKEN_HEADER)))
                .addSecurityItem(new SecurityRequirement().addList(BEARER).addList(DEVICE));
    }
}
