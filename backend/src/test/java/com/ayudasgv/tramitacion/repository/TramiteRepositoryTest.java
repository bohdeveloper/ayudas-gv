package com.ayudasgv.tramitacion.repository;

import com.ayudasgv.tramitacion.dto.ExpedienteTramitacionInfo;
import com.ayudasgv.tramitacion.dto.TramiteDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de repositorio con SQL real (sin mocks) para {@link TramiteRepository}:
 * ejecuta las migraciones Flyway V1–V6 contra H2 en modo compatibilidad PostgreSQL
 * y comprueba las consultas, el INSERT con clave generada y los UPDATE de estado.
 *
 * Es la contrapartida de {@code TramitacionServiceTest}: allí se prueban las reglas
 * con el repositorio mockeado, aquí se prueba que el SQL de verdad hace lo que el
 * servicio da por hecho.
 */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:tramite_repo_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.flyway.enabled=true"
})
@Import(TramiteRepository.class)
class TramiteRepositoryTest {

    @Autowired
    private TramiteRepository tramiteRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    private Long idDe(String numeroExpediente) {
        return jdbc.queryForObject(
                "SELECT id FROM expediente WHERE numero_expediente = :n",
                Map.of("n", numeroExpediente), Long.class);
    }

    private String estadoDe(String numeroExpediente) {
        return jdbc.queryForObject("""
                SELECT ee.codigo FROM expediente e
                JOIN estado_expediente ee ON ee.id = e.estado_id
                WHERE e.numero_expediente = :n
                """, Map.of("n", numeroExpediente), String.class);
    }

    // ─── Consulta ───────────────────────────────────────────────────────────────

    @Test
    void buscarInfoTramitacion_devuelveEstadoEInstructorAsignado() {
        Optional<ExpedienteTramitacionInfo> info =
                tramiteRepository.buscarInfoTramitacion(idDe("EXP-2024-001"), "descripcion_es");

        assertThat(info).isPresent();
        assertThat(info.get().estado().codigo()).isEqualTo("ASIGNADO");
        assertThat(info.get().estado().descripcion()).isEqualTo("Asignado a instructor");
        assertThat(info.get().instructorNombre()).isEqualTo("Ane Etxeberria");
    }

    @Test
    void buscarInfoTramitacion_enEuskera_devuelveLaDescripcionEnEuskera() {
        Optional<ExpedienteTramitacionInfo> info =
                tramiteRepository.buscarInfoTramitacion(idDe("EXP-2024-001"), "descripcion_eu");

        assertThat(info).isPresent();
        assertThat(info.get().estado().descripcion()).isEqualTo("Instruktoreari esleituta");
    }

    @Test
    void buscarInfoTramitacion_expedienteSinInstructor_devuelveNull() {
        // EXP-2024-002 se creó en V2 sin instructor_id: el LEFT JOIN debe tolerarlo
        Optional<ExpedienteTramitacionInfo> info =
                tramiteRepository.buscarInfoTramitacion(idDe("EXP-2024-002"), "descripcion_es");

        assertThat(info).isPresent();
        assertThat(info.get().instructorId()).isNull();
        assertThat(info.get().instructorNombre()).isNull();
    }

    @Test
    void buscarInfoTramitacion_expedienteInexistente_devuelveEmpty() {
        assertThat(tramiteRepository.buscarInfoTramitacion(999_999L, "descripcion_es")).isEmpty();
    }

    @Test
    void buscarPorExpediente_devuelveElHistoricoDelMasRecienteAlMasAntiguo() {
        List<TramiteDto> tramites = tramiteRepository.buscarPorExpediente(idDe("EXP-2024-005"), "descripcion_es");

        // V6 dejó el flujo completo de EXP-2024-005: 6 trámites
        assertThat(tramites).hasSize(6);
        assertThat(tramites.get(0).tipoTramite().codigo()).isEqualTo("NOTIFICACION");
        assertThat(tramites).extracting(t -> t.fecha()).isSortedAccordingTo(java.util.Comparator.reverseOrder());
    }

    @Test
    void buscarPorExpediente_traeQuienCuandoYResultado() {
        List<TramiteDto> tramites = tramiteRepository.buscarPorExpediente(idDe("EXP-2024-005"), "descripcion_es");

        TramiteDto propuesta = tramites.stream()
                .filter(t -> "PROPUESTA".equals(t.tipoTramite().codigo()))
                .findFirst().orElseThrow();

        assertThat(propuesta.username()).isEqualTo("instructor1");
        assertThat(propuesta.usuarioNombre()).isEqualTo("Ane Etxeberria");
        assertThat(propuesta.resultado()).isEqualTo("FAVORABLE");
        assertThat(propuesta.observaciones()).contains("se propone la concesión");
    }

    @Test
    void buscarPorExpediente_sinTramites_devuelveListaVacia() {
        assertThat(tramiteRepository.buscarPorExpediente(idDe("EXP-2024-002"), "descripcion_es")).isEmpty();
    }

    // ─── Escritura ──────────────────────────────────────────────────────────────

    @Test
    void insertar_devuelveLaClaveGeneradaYElTramiteEsRecuperable() {
        Long expedienteId = idDe("EXP-2024-004");
        Long usuarioId = jdbc.queryForObject(
                "SELECT id FROM usuario WHERE username = 'instructor1'", Map.of(), Long.class);

        Long nuevoId = tramiteRepository.insertar(expedienteId, "REVISION", usuarioId,
                LocalDateTime.of(2026, 7, 29, 10, 0), null, "Prueba de inserción");

        assertThat(nuevoId).isNotNull();

        List<TramiteDto> tramites = tramiteRepository.buscarPorExpediente(expedienteId, "descripcion_es");
        assertThat(tramites).hasSize(1);
        assertThat(tramites.get(0).id()).isEqualTo(nuevoId);
        assertThat(tramites.get(0).observaciones()).isEqualTo("Prueba de inserción");
    }

    @Test
    void actualizarEstado_cambiaElEstadoPorCodigoDeCatalogo() {
        Long expedienteId = idDe("EXP-2024-002");

        int filas = tramiteRepository.actualizarEstado(expedienteId, "EN_REVISION");

        assertThat(filas).isEqualTo(1);
        assertThat(estadoDe("EXP-2024-002")).isEqualTo("EN_REVISION");
    }

    @Test
    void actualizarEstado_expedienteInexistente_noCambiaNada() {
        assertThat(tramiteRepository.actualizarEstado(999_999L, "EN_REVISION")).isZero();
    }

    @Test
    void marcarResuelto_poneEstadoYFechaDeResolucionEnLaMismaSentencia() {
        Long expedienteId = idDe("EXP-2024-003");

        int filas = tramiteRepository.marcarResuelto(expedienteId, LocalDate.of(2026, 7, 29));

        assertThat(filas).isEqualTo(1);
        assertThat(estadoDe("EXP-2024-003")).isEqualTo("RESUELTO");
        assertThat(jdbc.queryForObject("SELECT fecha_resolucion FROM expediente WHERE id = :id",
                Map.of("id", expedienteId), LocalDate.class))
                .isEqualTo(LocalDate.of(2026, 7, 29));
    }

    @Test
    void asignarInstructor_grabaElInstructorEnElExpediente() {
        Long expedienteId = idDe("EXP-2024-004");
        Long instructorId = jdbc.queryForObject(
                "SELECT id FROM usuario WHERE username = 'instructor1'", Map.of(), Long.class);

        int filas = tramiteRepository.asignarInstructor(expedienteId, instructorId);

        assertThat(filas).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT instructor_id FROM expediente WHERE id = :id",
                Map.of("id", expedienteId), Long.class)).isEqualTo(instructorId);
    }

    // ─── Comprobaciones de catálogo y usuarios ──────────────────────────────────

    @Test
    void esInstructorActivo_soloEsCiertoParaQuienTieneElRolInstructor() {
        Long ane = jdbc.queryForObject(
                "SELECT id FROM usuario WHERE username = 'instructor1'", Map.of(), Long.class);
        Long mikel = jdbc.queryForObject(
                "SELECT id FROM usuario WHERE username = 'consultor1'", Map.of(), Long.class);

        assertThat(tramiteRepository.esInstructorActivo(ane)).isTrue();
        assertThat(tramiteRepository.esInstructorActivo(mikel)).isFalse();
        assertThat(tramiteRepository.esInstructorActivo(999_999L)).isFalse();
    }

    @Test
    void buscarIdUsuarioActivo_resuelveElUsernameDelJwt() {
        assertThat(tramiteRepository.buscarIdUsuarioActivo("instructor1")).isPresent();
        assertThat(tramiteRepository.buscarIdUsuarioActivo("no-existe")).isEmpty();
    }

    @Test
    void listarInstructoresActivos_devuelveSoloInstructoresYSinRepetir() {
        var instructores = tramiteRepository.listarInstructoresActivos();

        // V1 dio de alta a instructor1 (Ane Etxeberria) y V6 a instructor2 (Miren Agirre)
        assertThat(instructores).extracting(i -> i.username())
                .containsExactly("instructor2", "instructor1");   // ordenados por apellidos: Agirre, Etxeberria
        assertThat(instructores).extracting(i -> i.nombreCompleto())
                .containsExactly("Miren Agirre", "Ane Etxeberria");
    }

    @Test
    void existeTipoTramite_validaContraElCatalogo() {
        assertThat(tramiteRepository.existeTipoTramite("RESOLUCION")).isTrue();
        assertThat(tramiteRepository.existeTipoTramite("INVENTADO")).isFalse();
    }
}
