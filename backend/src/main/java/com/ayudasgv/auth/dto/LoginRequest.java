package com.ayudasgv.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO con las credenciales que el cliente envía en el cuerpo del POST /auth/login.
 *
 * @NotBlank: valida que el campo no sea null ni vacío/espacios.
 * Si la validación falla (campo vacío), Spring devuelve 400 Bad Request
 * antes incluso de que el método del controlador se ejecute.
 *
 * @Schema: anotaciones de Swagger para que la documentación muestre
 * un ejemplo claro de cómo usar el endpoint.
 */
public record LoginRequest(

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Schema(description = "Nombre de usuario", example = "admin")
    String username,

    @NotBlank(message = "La contraseña es obligatoria")
    @Schema(description = "Contraseña en claro (se compara contra el hash BCrypt en BD)", example = "admin123")
    String password

) {}
