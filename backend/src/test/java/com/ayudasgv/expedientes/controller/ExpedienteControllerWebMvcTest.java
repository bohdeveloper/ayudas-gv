package com.ayudasgv.expedientes.controller;

import com.ayudasgv.auth.security.JwtAuthenticationFilter;
import com.ayudasgv.auth.security.JwtProvider;
import com.ayudasgv.commons.exception.GlobalExceptionHandler;
import com.ayudasgv.commons.exception.RecursoNoEncontradoException;
import com.ayudasgv.config.SecurityConfig;
import com.ayudasgv.expedientes.dto.PageResponseDto;
import com.ayudasgv.expedientes.dto.SeccionGeneralesDto;
import com.ayudasgv.expedientes.service.ExpedienteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de controlador + seguridad ({@code @WebMvcTest} con MockMvc) para
 * {@link ExpedienteController}: verifica el contrato HTTP (códigos de estado,
 * cuerpo de error homogéneo) con la cadena de seguridad real (JwtAuthenticationFilter
 * + SecurityConfig), pero sin base de datos — {@link ExpedienteService} va mockeado
 * (su lógica ya está cubierta en ExpedienteServiceTest).
 *
 * Repaso de autorización por rol: en Fase 0 los cuatro roles seed (CONSULTOR,
 * INSTRUCTOR, RESPONSABLE, ADMIN) tienen los mismos permisos de lectura, así que
 * el criterio de "autorizado" es simplemente "trae un JWT válido, sea cual sea
 * su rol" — se verifica con {@code accesoPermitido_paraCualquierRolSeed}.
 */
@WebMvcTest(ExpedienteController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtProvider.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {
        "app.jwt.secret=clave-de-pruebas-hs256-de-al-menos-32-caracteres",
        "app.jwt.expiration-ms=60000"
})
class ExpedienteControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProvider jwtProvider;

    @MockBean
    private ExpedienteService expedienteService;

    @Test
    void buscar_sinToken_devuelve401() throws Exception {
        mockMvc.perform(get("/v1/expedientes"))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CONSULTOR", "INSTRUCTOR", "RESPONSABLE", "ADMIN"})
    void accesoPermitido_paraCualquierRolSeed(String rol) throws Exception {
        when(expedienteService.buscar(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(PageResponseDto.of(List.of(), 0, 0, 10));

        String token = jwtProvider.generateToken("usuario-" + rol.toLowerCase(), List.of(rol), "es");

        mockMvc.perform(get("/v1/expedientes").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void generales_conIdInexistente_devuelve404ConCuerpoHomogeneo() throws Exception {
        when(expedienteService.obtenerGenerales(anyLong(), any()))
                .thenThrow(new RecursoNoEncontradoException("No existe un expediente con id 99"));

        String token = jwtProvider.generateToken("admin", List.of("ADMIN"), "es");

        mockMvc.perform(get("/v1/expedientes/99/generales").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("No existe un expediente con id 99"));
    }

    @Test
    void generales_conTokenValido_devuelve200ConElCuerpoDelServicio() throws Exception {
        var dto = new SeccionGeneralesDto(1L, "EXP-2024-001", null, null, null, null, null);
        // any() para el Locale: el resuelto por Spring a partir de la petición (sin
        // Accept-Language) no es necesariamente igual a Locale.forLanguageTag("es").
        when(expedienteService.obtenerGenerales(eq(1L), any())).thenReturn(dto);

        String token = jwtProvider.generateToken("admin", List.of("ADMIN"), "es");

        mockMvc.perform(get("/v1/expedientes/1/generales").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroExpediente").value("EXP-2024-001"));
    }

    @Test
    void generales_conTokenInvalido_devuelve401() throws Exception {
        mockMvc.perform(get("/v1/expedientes/1/generales").header("Authorization", "Bearer token-basura"))
                .andExpect(status().isUnauthorized());
    }
}
