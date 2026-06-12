package com.ayudasgv.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Configura Swagger / OpenAPI para la documentación interactiva de la API.
 *
 * Swagger UI queda accesible en /api/swagger-ui.html y permite probar los
 * endpoints directamente desde el navegador sin necesidad de Postman.
 *
 * Semana 2: se añade el esquema de seguridad "bearerAuth" (JWT Bearer Token).
 *   - @SecurityScheme define cómo se llama el esquema y de qué tipo es.
 *   - Los endpoints que requieren token deben anotar @SecurityRequirement(name = "bearerAuth").
 *   - Swagger muestra un botón "Authorize" en la UI para pegar el token JWT.
 *
 * Uso en Swagger UI:
 *   1. Hacer POST /v1/auth/login para obtener el token.
 *   2. Clicar "Authorize" → pegar el token (sin "Bearer ", Swagger lo añade).
 *   3. Las peticiones posteriores incluirán automáticamente: Authorization: Bearer <token>
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Ayudas GV API",
        description = "Sistema de Gestión y Tramitación de Expedientes de Ayudas — Gobierno Vasco",
        version = "1.0.0"
    )
)
@SecurityScheme(
    name = "bearerAuth",                   // nombre de referencia usado en @SecurityRequirement
    type = SecuritySchemeType.HTTP,        // tipo HTTP (frente a apiKey u oauth2)
    scheme = "bearer",                     // esquema HTTP: "bearer" (RFC 7617)
    bearerFormat = "JWT"                   // documentación: el token es un JWT
)
public class OpenApiConfig {
}
