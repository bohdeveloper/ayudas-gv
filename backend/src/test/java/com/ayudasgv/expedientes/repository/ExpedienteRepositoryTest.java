package com.ayudasgv.expedientes.repository;

import com.ayudasgv.expedientes.dto.DetalleAsignacionDiscapacidadDto;
import com.ayudasgv.expedientes.dto.DetalleNacimientoAdopcionDto;
import com.ayudasgv.expedientes.dto.DetallePartoMultipleDto;
import com.ayudasgv.expedientes.dto.ExpedienteBusquedaCriteria;
import com.ayudasgv.expedientes.dto.ExpedienteResumenDto;
import com.ayudasgv.expedientes.dto.SeccionAyudaDto;
import com.ayudasgv.expedientes.dto.SeccionContactoDto;
import com.ayudasgv.expedientes.dto.SeccionGeneralesDto;
import com.ayudasgv.expedientes.dto.SeccionInteresadosDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de repositorio con SQL real (sin mocks) para {@link ExpedienteRepository}:
 * ejecuta las migraciones Flyway (V1-V5, con sus datos de ejemplo) contra una BD H2
 * en memoria en modo compatibilidad PostgreSQL, y comprueba que las consultas dinámicas
 * (filtros, JOINs, paginación, detalle tipado por tipo de ayuda) devuelven lo esperado —
 * exactamente lo que pide la sección 10 del enunciado como alternativa a Testcontainers.
 */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:expediente_repo_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.flyway.enabled=true"
})
@Import(ExpedienteRepository.class)
class ExpedienteRepositoryTest {

    @Autowired
    private ExpedienteRepository expedienteRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    /** Los expedientes de ejemplo tienen id autogenerado — se busca por numero_expediente (estable). */
    private Long idDe(String numeroExpediente) {
        return jdbc.queryForObject(
                "SELECT id FROM expediente WHERE numero_expediente = :n",
                Map.of("n", numeroExpediente), Long.class);
    }

    private static ExpedienteBusquedaCriteria criteriaVacio() {
        return new ExpedienteBusquedaCriteria(null, null, null, null, null, null, 0, 10, null);
    }

    @Test
    void buscar_sinFiltros_devuelveLosCincoExpedientesDeEjemplo() {
        List<ExpedienteResumenDto> resultado = expedienteRepository.buscar(criteriaVacio(), "descripcion_es");

        assertThat(resultado).hasSize(5);
        assertThat(expedienteRepository.contar(criteriaVacio())).isEqualTo(5);
    }

    @Test
    void buscar_filtrandoPorNumeroExpedienteParcial_usaIlikeCaseInsensitive() {
        // ILIKE: coincidencia parcial e insensible a mayúsculas — filtro en minúsculas
        // sobre un número guardado en mayúsculas (EXP-2024-001).
        var criteria = new ExpedienteBusquedaCriteria("exp-2024-001", null, null, null, null, null, 0, 10, null);

        List<ExpedienteResumenDto> resultado = expedienteRepository.buscar(criteria, "descripcion_es");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).numeroExpediente()).isEqualTo("EXP-2024-001");
    }

    @Test
    void buscar_filtrandoPorTipoAyudaYEstado_combinaAmbasCondiciones() {
        // EXP-2024-001 y EXP-2024-004 son ASIG_DISCAP; solo EXP-2024-004 está REGISTRADO.
        var criteria = new ExpedienteBusquedaCriteria(null, "ASIG_DISCAP", "REGISTRADO", null, null, null, 0, 10, null);

        List<ExpedienteResumenDto> resultado = expedienteRepository.buscar(criteria, "descripcion_es");

        assertThat(resultado).extracting(ExpedienteResumenDto::numeroExpediente)
                .containsExactly("EXP-2024-004");
    }

    @Test
    void buscar_filtrandoPorDocumentoSolicitante_recorreElJoinConPersonaEInteresado() {
        // 12345678A (María García) es SOLICITANTE de EXP-2024-001 y EXP-2024-004.
        var criteria = new ExpedienteBusquedaCriteria(null, null, null, "12345678A", null, null, 0, 10, null);

        List<ExpedienteResumenDto> resultado = expedienteRepository.buscar(criteria, "descripcion_es");

        assertThat(resultado).extracting(ExpedienteResumenDto::numeroExpediente)
                .containsExactlyInAnyOrder("EXP-2024-001", "EXP-2024-004");
    }

    @Test
    void buscar_conDescColumnEuskera_devuelveLaDescripcionEnEuskera() {
        var criteria = new ExpedienteBusquedaCriteria("EXP-2024-001", null, null, null, null, null, 0, 10, null);

        List<ExpedienteResumenDto> resultado = expedienteRepository.buscar(criteria, "descripcion_eu");

        assertThat(resultado.get(0).tipoAyuda().descripcion())
                .isEqualTo("Desgaitasuna duen seme-alaba edo ardurapeko adingaberako diru-laguntza");
    }

    @Test
    void buscar_conPaginacion_respetaSizeYOffset() {
        var primeraPagina = new ExpedienteBusquedaCriteria(null, null, null, null, null, null, 0, 2, "numeroExpediente,asc");
        var segundaPagina  = new ExpedienteBusquedaCriteria(null, null, null, null, null, null, 1, 2, "numeroExpediente,asc");

        List<ExpedienteResumenDto> pagina1 = expedienteRepository.buscar(primeraPagina, "descripcion_es");
        List<ExpedienteResumenDto> pagina2 = expedienteRepository.buscar(segundaPagina, "descripcion_es");

        assertThat(pagina1).hasSize(2);
        assertThat(pagina2).hasSize(2);
        assertThat(pagina1).extracting(ExpedienteResumenDto::numeroExpediente)
                .containsExactly("EXP-2024-001", "EXP-2024-002");
        assertThat(pagina2).extracting(ExpedienteResumenDto::numeroExpediente)
                .containsExactly("EXP-2024-003", "EXP-2024-004");
    }

    @Test
    void existeExpediente_conIdDeUnoDeEjemplo_devuelveTrue_yConIdInexistente_devuelveFalse() {
        assertThat(expedienteRepository.existeExpediente(idDe("EXP-2024-001"))).isTrue();
        assertThat(expedienteRepository.existeExpediente(999_999L)).isFalse();
    }

    @Test
    void buscarGenerales_conInstructorAsignado_devuelveNombreCompletoDelInstructor() {
        // EXP-2024-001 tiene instructor_id = instructor1 (Ane Etxeberria) en el dato de ejemplo.
        Optional<SeccionGeneralesDto> resultado =
                expedienteRepository.buscarGenerales(idDe("EXP-2024-001"), "descripcion_es");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().instructorNombre()).isEqualTo("Ane Etxeberria");
        assertThat(resultado.get().estado().codigo()).isEqualTo("ASIGNADO");
    }

    @Test
    void buscarGenerales_sinInstructorAsignado_devuelveInstructorNuloYNoFalla() {
        // EXP-2024-002 se creó con instructor_id = NULL en el dato de ejemplo.
        Optional<SeccionGeneralesDto> resultado =
                expedienteRepository.buscarGenerales(idDe("EXP-2024-002"), "descripcion_es");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().instructorNombre()).isNull();
    }

    @Test
    void buscarGenerales_conIdInexistente_devuelveOptionalVacio() {
        assertThat(expedienteRepository.buscarGenerales(999_999L, "descripcion_es")).isEmpty();
    }

    @Test
    void buscarInteresados_devuelveSolicitanteRepresentanteYCausantesSeparados() {
        // EXP-2024-004 (V5) tiene los 3 roles: solicitante, representante y causante.
        SeccionInteresadosDto resultado = expedienteRepository.buscarInteresados(idDe("EXP-2024-004"));

        assertThat(resultado.solicitante().numeroDocumento()).isEqualTo("12345678A");
        assertThat(resultado.representante()).isNotNull();
        assertThat(resultado.representante().numeroDocumento()).isEqualTo("X1234567Z");
        assertThat(resultado.causantes()).hasSize(1);
        assertThat(resultado.causantes().get(0).numeroDocumento()).isEqualTo("44332211D");
    }

    @Test
    void buscarInteresados_sinRepresentante_devuelveRepresentanteNulo() {
        // EXP-2024-001 solo tiene solicitante + causante, sin representante.
        SeccionInteresadosDto resultado = expedienteRepository.buscarInteresados(idDe("EXP-2024-001"));

        assertThat(resultado.solicitante()).isNotNull();
        assertThat(resultado.representante()).isNull();
        assertThat(resultado.causantes()).hasSize(1);
    }

    @Test
    void buscarContacto_conDatosRegistrados_devuelvePresente() {
        Optional<SeccionContactoDto> resultado = expedienteRepository.buscarContacto(idDe("EXP-2024-001"));

        assertThat(resultado).isPresent();
        assertThat(resultado.get().municipio()).isEqualTo("Errenteria");
        assertThat(resultado.get().idiomaNotificacion()).isEqualTo("es");
    }

    @Test
    void buscarContacto_sinDatosRegistrados_devuelveOptionalVacio() {
        // Los 5 expedientes de ejemplo ya tienen datos_contacto (V2 + V5), así que se
        // prueba el caso vacío real con un expediente inexistente.
        assertThat(expedienteRepository.buscarContacto(999_999L)).isEmpty();
    }

    @Test
    void buscarAyuda_conAsignacionDiscapacidad_devuelveDetalleTipadoConElCausante() {
        Optional<SeccionAyudaDto> resultado = expedienteRepository.buscarAyuda(idDe("EXP-2024-001"));

        assertThat(resultado).isPresent();
        var detalle = (DetalleAsignacionDiscapacidadDto) resultado.get().detalle();
        assertThat(detalle.gradoDiscapacidad()).isEqualTo(45);
        assertThat(detalle.necesitaTerceraPersona()).isFalse();
        assertThat(detalle.causante().numeroDocumento()).isEqualTo("11223344C");
    }

    @Test
    void buscarAyuda_conPartoMultiple_devuelveNumeroDeNacidos() {
        Optional<SeccionAyudaDto> resultado = expedienteRepository.buscarAyuda(idDe("EXP-2024-003"));

        assertThat(resultado).isPresent();
        var detalle = (DetallePartoMultipleDto) resultado.get().detalle();
        assertThat(detalle.numeroNacidos()).isEqualTo(3);
    }

    @Test
    void buscarAyuda_conNacimientoAdopcion_devuelveSupuestoYFamiliaANumerosos() {
        // EXP-2024-002 (V5 completa su detalle_nacimiento_adopcion).
        Optional<SeccionAyudaDto> resultado = expedienteRepository.buscarAyuda(idDe("EXP-2024-002"));

        assertThat(resultado).isPresent();
        var detalle = (DetalleNacimientoAdopcionDto) resultado.get().detalle();
        assertThat(detalle.supuesto()).isEqualTo("FAMILIA_NUMEROSA");
        assertThat(detalle.numeroHijosCargo()).isEqualTo(3);
    }

    @Test
    void buscarAyuda_sinSolicitudRegistrada_devuelveOptionalVacio() {
        assertThat(expedienteRepository.buscarAyuda(999_999L)).isEmpty();
    }
}
