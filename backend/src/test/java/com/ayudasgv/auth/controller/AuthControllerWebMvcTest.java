package com.ayudasgv.auth.controller;

import com.ayudasgv.auth.dto.LoginRequest;
import com.ayudasgv.auth.dto.LoginResponse;
import com.ayudasgv.auth.security.JwtProvider;
import com.ayudasgv.auth.service.AuthService;
import com.ayudasgv.commons.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de controlador ({@code @WebMvcTest}) para {@link AuthController}:
 * validación de entrada, credenciales incorrectas → 401 homogéneo, y login
 * correcto → 200 con el token. {@code /v1/auth/login} es ruta pública, así
 * que no hace falta la cadena de seguridad real aquí (a diferencia de
 * ExpedienteControllerWebMvcTest, que sí la ejercita). @AutoConfigureMockMvc(addFilters = false)
 * desactiva los filtros servlet para no depender del auto-config de seguridad por defecto.
 *
 * @Import(JwtProvider.class): @WebMvcTest detecta igualmente JwtAuthenticationFilter
 * (es un {@code Filter} — uno de los tipos que este slice registra siempre, aunque
 * los filtros estén desactivados), y ese filtro necesita JwtProvider en su constructor.
 *
 * @Import(MessageSourceAutoConfiguration.class): @WebMvcTest no carga la aplicación
 * completa, así que el MessageSource resuelto por spring.messages.basename no está
 * disponible por defecto — sin esto, GlobalExceptionHandler recibiría un MessageSource
 * "vacío" y devolvería la clave sin resolver en vez del texto de messages_es/eu.properties.
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, JwtProvider.class, MessageSourceAutoConfiguration.class})
class AuthControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void login_conCamposVacios_devuelve400ConDetalleDeValidacion() throws Exception {
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void login_conCredencialesIncorrectas_devuelve401Homogeneo() throws Exception {
        when(authService.login(any())).thenThrow(new BadCredentialsException("auth.login.error"));

        // Accept-Language explícito: el mensaje se resuelve vía MessageSource según el idioma
        // de la petición, no está hardcodeado — sin la cabecera, el locale por defecto de
        // MockHttpServletRequest no es determinista entre entornos (ver sección 8.4 del enunciado).
        mockMvc.perform(post("/v1/auth/login")
                        .header("Accept-Language", "es")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin", "mala-clave"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Credenciales incorrectas"));
    }

    @Test
    void login_conCredencialesCorrectas_devuelve200ConToken() throws Exception {
        when(authService.login(any())).thenReturn(new LoginResponse(
                "jwt-simulado", "admin", "Administrador Sistema", List.of("ADMIN"), "es"));

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin", "admin123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-simulado"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }
}
