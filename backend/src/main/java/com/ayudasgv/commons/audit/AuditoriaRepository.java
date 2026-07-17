package com.ayudasgv.commons.audit;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * Inserción de registros de auditoría (tabla {@code auditoria}, creada en
 * V3__auditoria_indices.sql). Solo escritura: no hay caso de uso todavía para
 * consultar el histórico desde la aplicación.
 */
@Repository
public class AuditoriaRepository {

    private static final String SQL_INSERT = """
            INSERT INTO auditoria (fecha_hora, username, accion, entidad, entidad_id, detalle, ip_origen)
            VALUES (:fechaHora, :username, :accion, :entidad, :entidadId, :detalle, :ipOrigen)
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public AuditoriaRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void registrar(String username, String accion, String entidad,
                           String entidadId, String detalle, String ipOrigen) {
        var params = new MapSqlParameterSource()
                .addValue("fechaHora", Timestamp.from(Instant.now()))
                .addValue("username", username)
                .addValue("accion", accion)
                .addValue("entidad", entidad)
                .addValue("entidadId", entidadId)
                .addValue("detalle", detalle)
                .addValue("ipOrigen", ipOrigen);

        jdbc.update(SQL_INSERT, params);
    }
}
