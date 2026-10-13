package com.ayudasgv.tramitacion.controller;

import com.ayudasgv.auth.security.JwtAuthenticationFilter;
import com.ayudasgv.auth.security.JwtProvider;
import com.ayudasgv.commons.dto.CatalogoDto;
import com.ayudasgv.commons.exception.GlobalExceptionHandler;
import com.ayudasgv.commons.exception.OperacionNoPermitidaException;
import com.ayudasgv.commons.exception.RecursoNoEncontradoException;
import com.ayudasgv.commons.exception.ReglaNegocioException;
import com.ayudasgv.config.SecurityConfig;
import com.ayudasgv.tramitacion.dto.SeccionTramitacionDto;
import com.ayudasgv.tramitacion.dto.TramiteDto;
import com.ayudasgv.tramitacion.service.TramitacionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrato HTTP de {@link TramiteController} con la cadena de seguridad real
 * (JwtAuthenticationFilter + SecurityConfig) y el servicio mockeado.
 *
 * Lo que se verifica aquí y no en {@code TramitacionServiceTest}: que cada regla de
 * negocio sale por el código de estado correcto (403 / 404 / 409), que
 * {@code @PreAuthorize} corta por rol antes de llegar al servicio, y que el mensaje
 * de error se traduce según {@code Accept-Language}.
 */
@WebMvcTest(TramiteController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtProvider.class, GlobalExceptionHandler.class,
        MessageSourceAutoConfiguration.class})
@TestPropertySource(properties = {
        "app.jwt.secret=clave-de-pruebas-hs256-de-al-menos-32-caracteres",
        "app.jwt.expiration-ms=60000"
})
class TramiteControllerWebMvcTest {

    private static final String TRAMITES = "/v1/expedientes/1/tramites";
    private static final String INSTRUCTOR = "/v1/expedientes/1/instructor";
    private static final String CUERPO_REVISION = """
            {"tipoTramite":"REVISION","observaciones":"Documentación completa"}""";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProvider jwtProvider;

    @MockitoBean
    private TramitacionService tramitacionService;

    private String tokenCon(String... roles) {
        return jwtProvider.generateToken("usuario-prueba", List.of(roles), "es");
    }

    private static SeccionTramitacionDto seccionVacia() {
        return new SeccionTramitacionDto(new CatalogoDto("ASIGNADO", "Asignado a instructor"),
                2L, "Ane Etxeberria", List.of());
    }

    // ─── Lectura ────────────────────────────────────────────────────────────────

    @Test
    void consultarTramites_sinToken_devuelve401() throws Exception {
        mockMvc.perform(get(TRAMITES))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CONSULTOR", "INSTRUCTOR", "RESPONSABLE", "ADMIN"})
    void consultarTramites_lopuedeVerCualquierRol(String rol) throws Exception {
        when(tramitacionService.obtenerTramitacion(anyLong(), any())).thenReturn(seccionVacia());

        mockMvc.perform(get(TRAMITES).header("Authorization", "Bearer " + tokenCon(rol)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado.codigo").value("ASIGNADO"))
                .andExpect(jsonPath("$.instructorNombre").value("Ane Etxeberria"));
    }

    @Test
    void consultarTramites_expedienteInexistente_devuelve404() throws Exception {
        when(tramitacionService.obtenerTramitacion(anyLong(), any()))
                .thenThrow(new RecursoNoEncontradoException("expediente.notfound", 1L));

        mockMvc.perform(get(TRAMITES).header("Authorization", "Bearer " + tokenCon("CONSULTOR")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Expediente no encontrado: 1"));
    }

    // ─── Registro de trámites ───────────────────────────────────────────────────

    @Test
    void registrarTramite_conRolConsultor_devuelve403SinLlegarAlServicio() throws Exception {
        mockMvc.perform(post(TRAMITES)
                        .header("Authorization", "Bearer " + tokenCon("CONSULTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_REVISION))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrarTramite_conRolInstructor_devuelve201() throws Exception {
        when(tramitacionService.registrarTramite(anyLong(), any(), anyString(), any(), any()))
                .thenReturn(new TramiteDto(5L, new CatalogoDto("REVISION", "Revisión / subsanación"),
                        LocalDateTime.now(), "usuario-prueba", "Usuario Prueba", null, "Documentación completa"));

        mockMvc.perform(post(TRAMITES)
                        .header("Authorization", "Bearer " + tokenCon("INSTRUCTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_REVISION))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.tipoTramite.codigo").value("REVISION"));
    }

    @Test
    void registrarTramite_instructorNoAsignado_devuelve403ConElMotivo() throws Exception {
        when(tramitacionService.registrarTramite(anyLong(), any(), anyString(), any(), any()))
                .thenThrow(new OperacionNoPermitidaException("tramite.no.permiso"));

        mockMvc.perform(post(TRAMITES)
                        .header("Authorization", "Bearer " + tokenCon("INSTRUCTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_REVISION))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Solo puede tramitar los expedientes que tiene asignados"));
    }

    @Test
    void registrarTramite_instructorNoAsignado_devuelveElMotivoEnEuskera() throws Exception {
        when(tramitacionService.registrarTramite(anyLong(), any(), anyString(), any(), any()))
                .thenThrow(new OperacionNoPermitidaException("tramite.no.permiso"));

        mockMvc.perform(post(TRAMITES)
                        .header("Authorization", "Bearer " + tokenCon("INSTRUCTOR"))
                        .header("Accept-Language", "eu")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_REVISION))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Esleitutako espedienteak soilik izapide ditzakezu"));
    }

    @Test
    void registrarTramite_expedienteResuelto_devuelve409() throws Exception {
        when(tramitacionService.registrarTramite(anyLong(), any(), anyString(), any(), any()))
                .thenThrow(new ReglaNegocioException("tramite.estado.invalido"));

        mockMvc.perform(post(TRAMITES)
                        .header("Authorization", "Bearer " + tokenCon("RESPONSABLE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_REVISION))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("El estado del expediente no permite este trámite"));
    }

    @Test
    void registrarTramite_sinTipoDeTramite_devuelve400() throws Exception {
        mockMvc.perform(post(TRAMITES)
                        .header("Authorization", "Bearer " + tokenCon("INSTRUCTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"observaciones\":\"sin tipo\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrarTramite_conResultadoInvalido_devuelve400() throws Exception {
        mockMvc.perform(post(TRAMITES)
                        .header("Authorization", "Bearer " + tokenCon("INSTRUCTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoTramite\":\"PROPUESTA\",\"resultado\":\"QUIZAS\"}"))
                .andExpect(status().isBadRequest());
    }

    // ─── Asignación de instructor ───────────────────────────────────────────────

    @ParameterizedTest
    @ValueSource(strings = {"CONSULTOR", "INSTRUCTOR"})
    void asignarInstructor_sinRolResponsable_devuelve403(String rol) throws Exception {
        mockMvc.perform(put(INSTRUCTOR)
                        .header("Authorization", "Bearer " + tokenCon(rol))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"instructorId\":2}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void asignarInstructor_conRolResponsable_devuelve200() throws Exception {
        when(tramitacionService.asignarInstructor(anyLong(), any(), anyString(), any()))
                .thenReturn(seccionVacia());

        mockMvc.perform(put(INSTRUCTOR)
                        .header("Authorization", "Bearer " + tokenCon("RESPONSABLE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"instructorId\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.instructorId").value(2));
    }

    @Test
    void asignarInstructor_sinInstructorId_devuelve400() throws Exception {
        mockMvc.perform(put(INSTRUCTOR)
                        .header("Authorization", "Bearer " + tokenCon("RESPONSABLE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void asignarInstructor_aQuienNoEsInstructor_devuelve409() throws Exception {
        when(tramitacionService.asignarInstructor(anyLong(), any(), anyString(), any()))
                .thenThrow(new ReglaNegocioException("tramite.instructor.invalido", 99L));

        mockMvc.perform(put(INSTRUCTOR)
                        .header("Authorization", "Bearer " + tokenCon("RESPONSABLE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"instructorId\":99}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("El usuario 99 no es un instructor activo"));
    }
}
