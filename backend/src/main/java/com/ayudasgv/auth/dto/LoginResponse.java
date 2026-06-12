package com.ayudasgv.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * DTO con los datos que el servidor devuelve tras un login exitoso.
 *
 * El cliente recibe:
 *   - token: el JWT firmado que debe incluir en la cabecera Authorization
 *             de cada petición posterior: "Authorization: Bearer <token>"
 *   - Datos del usuario para personalizar la interfaz (nombre, roles, idioma).
 *
 * IMPORTANTE: nunca se devuelve el passwordHash ni ningún dato sensible.
 * El tipo del token es siempre "Bearer" (estándar OAuth 2.0 / RFC 6750).
 */
public record LoginResponse(

    @Schema(description = "JWT firmado para incluir en cabeceras Authorization", example = "eyJhbGci...")
    String token,

    @Schema(description = "Tipo del token (siempre Bearer)", example = "Bearer")
    String tokenType,

    @Schema(description = "Nombre de usuario autenticado", example = "admin")
    String username,

    @Schema(description = "Nombre completo del usuario", example = "Administrador del Sistema")
    String nombre,

    @Schema(description = "Roles asignados al usuario", example = "[\"ADMIN\", \"RESPONSABLE\"]")
    List<String> roles,

    @Schema(description = "Idioma preferido del usuario: es o eu", example = "es")
    String idiomaPreferido

) {
    // Constructor compacto que fija el tipo de token siempre a "Bearer"
    public LoginResponse(String token, String username, String nombre,
                         List<String> roles, String idiomaPreferido) {
        this(token, "Bearer", username, nombre, roles, idiomaPreferido);
    }
}
