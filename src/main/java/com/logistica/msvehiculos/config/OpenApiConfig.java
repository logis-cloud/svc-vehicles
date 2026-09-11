package com.logistica.msvehiculos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("MS Vehículos y Flota - API")
                .description("Microservicio de gestión de vehículos y conductores para el sistema de logística y entregas")
                .version("1.0.0"));
    }
}
