package com.ayudasgv.commons.audit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.sql.Timestamp;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de repositorio con SQL real para {@link AuditoriaRepository}: inserta un
 * registro contra una BD H2 (modo compatibilidad PostgreSQL, esquema de la tabla
 * {@code auditoria} creado por V3__auditoria_indices.sql) y comprueba, leyendo
 * directamente la fila insertada, que todos los campos requeridos por la sección 8.3
 * del enunciado (usuario, acción, entidad, entidad_id, fecha/hora, IP) llegan a BD.
 */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:auditoria_repo_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.flyway.enabled=true"
})
@Import(AuditoriaRepository.class)
class AuditoriaRepositoryTest {

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    @Test
    void registrar_insertaUnaFilaConTodosLosCamposDeTrazabilidad() {
        auditoriaRepository.registrar("admin", "READ", "EXPEDIENTE", "1", null, "127.0.0.1");

        Map<String, Object> fila = jdbc.queryForMap(
                "SELECT * FROM auditoria WHERE username = :u AND accion = :a",
                Map.of("u", "admin", "a", "READ"));

        assertThat(fila.get("entidad")).isEqualTo("EXPEDIENTE");
        assertThat(fila.get("entidad_id")).isEqualTo("1");
        assertThat(fila.get("ip_origen")).isEqualTo("127.0.0.1");
        assertThat(fila.get("fecha_hora")).isInstanceOf(Timestamp.class);
    }

    @Test
    void registrar_conEntidadIdNulo_seUsaParaAccionesSinIdConcreto() {
        // Caso LOGIN: no hay una entidad_id concreta (no se audita "un" usuario por id).
        auditoriaRepository.registrar("consultor1", "LOGIN", "USUARIO", null, null, "10.0.0.5");

        Map<String, Object> fila = jdbc.queryForMap(
                "SELECT * FROM auditoria WHERE username = :u AND accion = 'LOGIN'",
                Map.of("u", "consultor1"));

        assertThat(fila.get("entidad_id")).isNull();
        assertThat(fila.get("entidad")).isEqualTo("USUARIO");
    }

    @Test
    void registrar_dosVeces_creaDosFilasIndependientes() {
        long antes = contarFilas();

        auditoriaRepository.registrar("admin", "READ", "EXPEDIENTE", "1", null, "127.0.0.1");
        auditoriaRepository.registrar("admin", "READ", "EXPEDIENTE", "2", null, "127.0.0.1");

        assertThat(contarFilas()).isEqualTo(antes + 2);
    }

    private long contarFilas() {
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM auditoria", Map.of(), Long.class);
        return total == null ? 0 : total;
    }
}
