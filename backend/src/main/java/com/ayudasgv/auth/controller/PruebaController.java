package com.ayudasgv.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint temporal de verificación de autenticación.
 *
 * Sirve exclusivamente para demostrar que el sistema JWT funciona:
 *   - Con token válido en cabecera Authorization → 200 OK con datos del usuario.
 *   - Sin token o con token inválido → 401 Unauthorized (Spring Security lo rechaza).
 *
 * Este endpoint se puede eliminar o mover al módulo de expedientes cuando
 * existan endpoints reales protegidos (Semana 4).
 *
 * @SecurityRequirement(name = "bearerAuth") indica a Swagger que este endpoint
 * requiere el esquema Bearer, mostrando el botón "Authorize" en la UI.
 */
@RestController
@RequestMapping("/v1")
@Tag(name = "Prueba", description = "Endpoint de verificación de autenticación JWT")
public class PruebaController {

    /**
     * Devuelve información del usuario autenticado.
     *
     * Spring Security inyecta el objeto Authentication con los datos que
     * JwtAuthenticationFilter guardó en el SecurityContextHolder.
     * Si no hay autenticación válida, Spring devuelve 401 antes de llegar aquí.
     *
     * @param authentication objeto de autenticación inyectado por Spring Security
     * @return 200 con username y roles del usuario autenticado
     */
    @Operation(
        summary = "Verificar autenticación",
        description = "Devuelve los datos del usuario autenticado. Requiere token JWT válido en cabecera Authorization.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/prueba/me")
    public ResponseEntity<Map<String, Object>> me(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
                "username", authentication.getName(),
                "roles", authentication.getAuthorities().stream()
                        .map(a -> a.getAuthority().replace("ROLE_", ""))
                        .toList(),
                "mensaje", "Token JWT válido. Autenticación correcta."
        ));
    }
}
