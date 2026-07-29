package com.ayudasgv.tramitacion.service;

import com.ayudasgv.commons.dto.CatalogoDto;
import com.ayudasgv.commons.exception.OperacionNoPermitidaException;
import com.ayudasgv.commons.exception.RecursoNoEncontradoException;
import com.ayudasgv.commons.exception.ReglaNegocioException;
import com.ayudasgv.tramitacion.dto.AsignacionInstructorRequest;
import com.ayudasgv.tramitacion.dto.ExpedienteTramitacionInfo;
import com.ayudasgv.tramitacion.dto.NuevoTramiteRequest;
import com.ayudasgv.tramitacion.dto.SeccionTramitacionDto;
import com.ayudasgv.tramitacion.dto.TramiteDto;
import com.ayudasgv.tramitacion.repository.TramiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas de la lógica de negocio de la tramitación (Fase 1), con el repositorio
 * mockeado: aquí se verifican las REGLAS, no el SQL (eso es {@code TramiteRepositoryTest}).
 *
 * La prueba central es {@link ReglaInstructorAsignado}: el invariante 4 de spec.md
 * ("un instructor solo puede tramitar expedientes asignados a él") se escribió antes
 * que el código, tal y como pide el plan de la Fase 1.
 */
@ExtendWith(MockitoExtension.class)
class TramitacionServiceTest {

    private static final Long EXPEDIENTE_ID = 10L;
    private static final Long ANE_ID = 2L;        // instructor1 — instructor asignado
    private static final Long MIREN_ID = 7L;      // instructor2 — otro instructor cualquiera
    private static final Locale ES = Locale.forLanguageTag("es");

    private static final List<String> ROLES_INSTRUCTOR = List.of("CONSULTOR", "INSTRUCTOR");
    private static final List<String> ROLES_RESPONSABLE = List.of("RESPONSABLE");
    private static final List<String> ROLES_CONSULTOR = List.of("CONSULTOR");

    @Mock
    private TramiteRepository tramiteRepository;

    @InjectMocks
    private TramitacionService tramitacionService;

    // ─── Utilidades de montaje ──────────────────────────────────────────────────

    private static ExpedienteTramitacionInfo expedienteEn(String estado, Long instructorId) {
        return new ExpedienteTramitacionInfo(
                EXPEDIENTE_ID,
                new CatalogoDto(estado, "descripción de " + estado),
                instructorId,
                instructorId == null ? null : "Ane Etxeberria");
    }

    private void dadoExpediente(ExpedienteTramitacionInfo info) {
        when(tramiteRepository.buscarInfoTramitacion(eq(EXPEDIENTE_ID), anyString()))
                .thenReturn(Optional.of(info));
    }

    private void dadoTipoTramiteValido() {
        when(tramiteRepository.existeTipoTramite(anyString())).thenReturn(true);
    }

    private void dadoUsuario(String username, Long id) {
        when(tramiteRepository.buscarIdUsuarioActivo(username)).thenReturn(Optional.of(id));
    }

    /** Simula que el trámite recién insertado se puede releer por su id. */
    private void dadoQueSeInsertaConId(Long tramiteId, String tipo) {
        when(tramiteRepository.insertar(anyLong(), anyString(), anyLong(), any(), any(), any()))
                .thenReturn(tramiteId);
        when(tramiteRepository.buscarPorId(eq(tramiteId), anyString()))
                .thenReturn(Optional.of(new TramiteDto(tramiteId, new CatalogoDto(tipo, tipo),
                        LocalDateTime.now(), "quien-sea", "Quien Sea", null, null)));
    }

    private static NuevoTramiteRequest tramite(String tipo) {
        return new NuevoTramiteRequest(tipo, null, null);
    }

    // ════════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Regla: un instructor solo tramita lo que tiene asignado (spec.md §3.4)")
    class ReglaInstructorAsignado {

        @Test
        void instructorNoAsignado_noPuedeTramitar() {
            // El expediente está asignado a Ane (id 2); quien tramita es instructor2 (id 7)
            dadoExpediente(expedienteEn("ASIGNADO", ANE_ID));
            dadoTipoTramiteValido();
            dadoUsuario("instructor2", MIREN_ID);

            assertThatThrownBy(() -> tramitacionService.registrarTramite(
                    EXPEDIENTE_ID, tramite("REVISION"), "instructor2", ROLES_INSTRUCTOR, ES))
                    .isInstanceOf(OperacionNoPermitidaException.class)
                    .hasMessage("tramite.no.permiso");

            // Lo importante no es solo que falle: es que NO haya escrito nada
            verify(tramiteRepository, never()).insertar(anyLong(), anyString(), anyLong(), any(), any(), any());
            verify(tramiteRepository, never()).actualizarEstado(anyLong(), anyString());
        }

        @Test
        void instructorAsignado_siPuedeTramitar() {
            dadoExpediente(expedienteEn("ASIGNADO", ANE_ID));
            dadoTipoTramiteValido();
            dadoUsuario("instructor1", ANE_ID);
            dadoQueSeInsertaConId(99L, "REVISION");

            TramiteDto creado = tramitacionService.registrarTramite(
                    EXPEDIENTE_ID, tramite("REVISION"), "instructor1", ROLES_INSTRUCTOR, ES);

            assertThat(creado.id()).isEqualTo(99L);
            verify(tramiteRepository).actualizarEstado(EXPEDIENTE_ID, "EN_REVISION");
        }

        @Test
        void responsable_tramitaCualquierExpedienteAunqueNoSeaSuyo() {
            dadoExpediente(expedienteEn("ASIGNADO", ANE_ID));
            dadoTipoTramiteValido();
            dadoUsuario("admin", 1L);
            dadoQueSeInsertaConId(100L, "REVISION");

            TramiteDto creado = tramitacionService.registrarTramite(
                    EXPEDIENTE_ID, tramite("REVISION"), "admin", ROLES_RESPONSABLE, ES);

            assertThat(creado.id()).isEqualTo(100L);
        }

        @Test
        void consultor_noPuedeTramitarAunqueElExpedienteEsteAsignado() {
            // Defensa en profundidad: el @PreAuthorize del controlador ya lo cortaría,
            // pero el servicio no da por hecho que alguien lo haya filtrado antes.
            dadoExpediente(expedienteEn("ASIGNADO", ANE_ID));
            dadoTipoTramiteValido();
            dadoUsuario("consultor1", 3L);

            assertThatThrownBy(() -> tramitacionService.registrarTramite(
                    EXPEDIENTE_ID, tramite("REVISION"), "consultor1", ROLES_CONSULTOR, ES))
                    .isInstanceOf(OperacionNoPermitidaException.class)
                    .hasMessage("tramite.no.permiso");
        }
    }

    @Nested
    @DisplayName("Reglas de estado del expediente")
    class ReglasDeEstado {

        @Test
        void expedienteResuelto_noAdmiteMasTramites() {
            dadoExpediente(expedienteEn("RESUELTO", ANE_ID));
            dadoTipoTramiteValido();

            assertThatThrownBy(() -> tramitacionService.registrarTramite(
                    EXPEDIENTE_ID, tramite("REVISION"), "admin", ROLES_RESPONSABLE, ES))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessage("tramite.estado.invalido");
        }

        @Test
        void expedienteResuelto_siAdmiteLaNotificacion() {
            dadoExpediente(expedienteEn("RESUELTO", ANE_ID));
            dadoTipoTramiteValido();
            dadoUsuario("admin", 1L);
            dadoQueSeInsertaConId(101L, "NOTIFICACION");

            tramitacionService.registrarTramite(
                    EXPEDIENTE_ID, tramite("NOTIFICACION"), "admin", ROLES_RESPONSABLE, ES);

            // Notificar no mueve el estado: el expediente ya estaba resuelto
            verify(tramiteRepository, never()).actualizarEstado(anyLong(), anyString());
            verify(tramiteRepository, never()).marcarResuelto(anyLong(), any());
        }

        @Test
        void sinInstructorAsignado_noSePuedeRevisar() {
            dadoExpediente(expedienteEn("REGISTRADO", null));
            dadoTipoTramiteValido();

            assertThatThrownBy(() -> tramitacionService.registrarTramite(
                    EXPEDIENTE_ID, tramite("REVISION"), "admin", ROLES_RESPONSABLE, ES))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessage("tramite.sin.instructor");
        }

        @Test
        void resolucion_sellaLaFechaDeResolucion() {
            dadoExpediente(expedienteEn("EN_REVISION", ANE_ID));
            dadoTipoTramiteValido();
            dadoUsuario("admin", 1L);
            dadoQueSeInsertaConId(102L, "RESOLUCION");

            tramitacionService.registrarTramite(EXPEDIENTE_ID,
                    new NuevoTramiteRequest("RESOLUCION", "FAVORABLE", null),
                    "admin", ROLES_RESPONSABLE, ES);

            verify(tramiteRepository).marcarResuelto(eq(EXPEDIENTE_ID), any());
            verify(tramiteRepository, never()).actualizarEstado(anyLong(), anyString());
        }

        @Test
        void propuestaSinResultado_seRechaza() {
            dadoExpediente(expedienteEn("EN_REVISION", ANE_ID));
            dadoTipoTramiteValido();
            dadoUsuario("instructor1", ANE_ID);

            assertThatThrownBy(() -> tramitacionService.registrarTramite(
                    EXPEDIENTE_ID, tramite("PROPUESTA"), "instructor1", ROLES_INSTRUCTOR, ES))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessage("tramite.resultado.required");
        }

        @Test
        void instructor_noPuedeResolver_aunqueElExpedienteSeaSuyo() {
            dadoExpediente(expedienteEn("EN_REVISION", ANE_ID));
            dadoTipoTramiteValido();

            assertThatThrownBy(() -> tramitacionService.registrarTramite(
                    EXPEDIENTE_ID, new NuevoTramiteRequest("RESOLUCION", "FAVORABLE", null),
                    "instructor1", ROLES_INSTRUCTOR, ES))
                    .isInstanceOf(OperacionNoPermitidaException.class)
                    .hasMessage("tramite.resolucion.rol");
        }

        @Test
        void asignacion_noSeAceptaComoTramiteSuelto() {
            dadoExpediente(expedienteEn("REGISTRADO", null));
            dadoTipoTramiteValido();

            assertThatThrownBy(() -> tramitacionService.registrarTramite(
                    EXPEDIENTE_ID, tramite("ASIGNACION"), "admin", ROLES_RESPONSABLE, ES))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessage("tramite.asignacion.endpoint");
        }

        @Test
        void tipoDeTramiteInexistente_devuelve404() {
            dadoExpediente(expedienteEn("REGISTRADO", null));
            when(tramiteRepository.existeTipoTramite("INVENTADO")).thenReturn(false);

            assertThatThrownBy(() -> tramitacionService.registrarTramite(
                    EXPEDIENTE_ID, tramite("INVENTADO"), "admin", ROLES_RESPONSABLE, ES))
                    .isInstanceOf(RecursoNoEncontradoException.class)
                    .hasMessage("tramite.tipo.notfound");
        }

        @Test
        void tipoDeTramite_seNormalizaAMayusculas() {
            dadoExpediente(expedienteEn("ASIGNADO", ANE_ID));
            dadoTipoTramiteValido();
            dadoUsuario("instructor1", ANE_ID);
            dadoQueSeInsertaConId(103L, "REVISION");

            tramitacionService.registrarTramite(
                    EXPEDIENTE_ID, tramite("  revision  "), "instructor1", ROLES_INSTRUCTOR, ES);

            ArgumentCaptor<String> tipo = ArgumentCaptor.forClass(String.class);
            verify(tramiteRepository).insertar(anyLong(), tipo.capture(), anyLong(), any(), any(), any());
            assertThat(tipo.getValue()).isEqualTo("REVISION");
        }
    }

    @Nested
    @DisplayName("Asignación de instructor")
    class Asignacion {

        @Test
        void asignar_cambiaEstadoYDejaTrazaEnElHistorico() {
            dadoExpediente(expedienteEn("REGISTRADO", null));
            when(tramiteRepository.esInstructorActivo(ANE_ID)).thenReturn(true);
            dadoUsuario("admin", 1L);
            when(tramiteRepository.buscarPorExpediente(eq(EXPEDIENTE_ID), anyString()))
                    .thenReturn(List.of());

            SeccionTramitacionDto seccion = tramitacionService.asignarInstructor(
                    EXPEDIENTE_ID, new AsignacionInstructorRequest(ANE_ID), "admin", ES);

            assertThat(seccion).isNotNull();
            verify(tramiteRepository).asignarInstructor(EXPEDIENTE_ID, ANE_ID);
            verify(tramiteRepository).actualizarEstado(EXPEDIENTE_ID, "ASIGNADO");
            verify(tramiteRepository).insertar(eq(EXPEDIENTE_ID), eq("ASIGNACION"), eq(1L), any(), any(), any());
        }

        @Test
        void asignarAQuienNoEsInstructor_seRechazaSinTocarNada() {
            dadoExpediente(expedienteEn("REGISTRADO", null));
            when(tramiteRepository.esInstructorActivo(99L)).thenReturn(false);

            assertThatThrownBy(() -> tramitacionService.asignarInstructor(
                    EXPEDIENTE_ID, new AsignacionInstructorRequest(99L), "admin", ES))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessage("tramite.instructor.invalido");

            verify(tramiteRepository, never()).asignarInstructor(anyLong(), anyLong());
            verify(tramiteRepository, never()).actualizarEstado(anyLong(), anyString());
        }
    }

    @Nested
    @DisplayName("Consulta de la sección")
    class Consulta {

        @Test
        void expedienteInexistente_devuelve404() {
            when(tramiteRepository.buscarInfoTramitacion(eq(999L), anyString())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tramitacionService.obtenerTramitacion(999L, ES))
                    .isInstanceOf(RecursoNoEncontradoException.class)
                    .hasMessage("expediente.notfound");
        }

        @Test
        void devuelveEstadoInstructorEHistorico() {
            dadoExpediente(expedienteEn("ASIGNADO", ANE_ID));
            when(tramiteRepository.buscarPorExpediente(eq(EXPEDIENTE_ID), anyString()))
                    .thenReturn(List.of(new TramiteDto(1L, new CatalogoDto("REGISTRO", "Registro de entrada"),
                            LocalDateTime.now(), "admin", "Administrador Sistema", null, null)));

            SeccionTramitacionDto seccion = tramitacionService.obtenerTramitacion(EXPEDIENTE_ID, ES);

            assertThat(seccion.estado().codigo()).isEqualTo("ASIGNADO");
            assertThat(seccion.instructorId()).isEqualTo(ANE_ID);
            assertThat(seccion.instructorNombre()).isEqualTo("Ane Etxeberria");
            assertThat(seccion.tramites()).hasSize(1);
        }

        @Test
        void euskera_pideLaColumnaDeDescripcionEnEuskera() {
            when(tramiteRepository.buscarInfoTramitacion(EXPEDIENTE_ID, "descripcion_eu"))
                    .thenReturn(Optional.of(expedienteEn("ASIGNADO", ANE_ID)));
            when(tramiteRepository.buscarPorExpediente(EXPEDIENTE_ID, "descripcion_eu")).thenReturn(List.of());

            tramitacionService.obtenerTramitacion(EXPEDIENTE_ID, Locale.forLanguageTag("eu"));

            verify(tramiteRepository).buscarPorExpediente(EXPEDIENTE_ID, "descripcion_eu");
        }
    }
}
