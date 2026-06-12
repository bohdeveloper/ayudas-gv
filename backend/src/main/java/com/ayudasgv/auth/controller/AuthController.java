package com.ayudasgv.auth.controller;

import com.ayudasgv.auth.dto.LoginRequest;
import com.ayudasgv.auth.dto.LoginResponse;
import com.ayudasgv.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para las operaciones de autenticación.
 *
 * @RestController = @Controller + @ResponseBody: serializa automáticamente
 * los objetos devueltos a JSON con Jackson.
 *
 * @RequestMapping("/v1/auth") establece el prefijo de ruta para todos los
 * métodos de este controlador. La ruta completa será /api/v1/auth/login
 * porque en application.yml el context-path es /api.
 *
 * La anotación @Tag de Swagger agrupa este endpoint en la sección "Autenticación"
 * de la documentación interactiva.
 */
@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Autenticación", description = "Obtención y renovación de tokens JWT")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Endpoint de login: valida credenciales y devuelve un JWT.
     *
     * Flujo completo:
     *   1. Spring deserializa el JSON del cuerpo a LoginRequest.
     *   2. @Valid activa la validación de @NotBlank — si falla, devuelve 400.
     *   3. AuthService verifica credenciales y genera el token.
     *   4. Si las credenciales son inválidas, GlobalExceptionHandler captura
     *      BadCredentialsException y devuelve 401.
     *
     * El token recibido debe usarse en peticiones posteriores así:
     *   Authorization: Bearer eyJhbGci...
     *
     * @param request JSON con { "username": "...", "password": "..." }
     * @return 200 OK con LoginResponse (token + datos del usuario)
     */
    @Operation(
        summary = "Iniciar sesión",
        description = "Valida las credenciales del usuario y devuelve un JWT para autenticar peticiones posteriores"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login correcto — devuelve el JWT",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "400", description = "Campos obligatorios ausentes o inválidos"),
        @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
