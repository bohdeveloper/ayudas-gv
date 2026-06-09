package com.ayudasgv.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Ayudas GV API",
        description = "Sistema de Gestión y Tramitación de Expedientes de Ayudas — Gobierno Vasco",
        version = "1.0.0"
    )
)
public class OpenApiConfig {
}
