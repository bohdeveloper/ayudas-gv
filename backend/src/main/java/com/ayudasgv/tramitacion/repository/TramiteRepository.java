package com.ayudasgv.tramitacion.repository;

import com.ayudasgv.commons.dto.CatalogoDto;
import com.ayudasgv.tramitacion.dto.ExpedienteTramitacionInfo;
import com.ayudasgv.tramitacion.dto.InstructorDto;
import com.ayudasgv.tramitacion.dto.TramiteDto;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de la tramitación con SQL explícito (sin JPA), igual que
 * {@code ExpedienteRepository} — invariante 1 de spec.md.
 *
 * Todo el SQL va parametrizado con parámetros nombrados. La única parte del SQL que
 * se compone en Java es el nombre de la columna de descripción del catálogo
 * ("descripcion_es" / "descripcion_eu"), elegido en código a partir de una lista
 * cerrada de dos valores — nunca a partir de texto que venga del cliente.
 */
@Repository
public class TramiteRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public TramiteRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Estado e instructor actuales del expediente — lo que el servicio necesita para
     * decidir si el trámite se puede registrar. {@code Optional.empty()} si no existe.
     */
    public Optional<ExpedienteTramitacionInfo> buscarInfoTramitacion(Long expedienteId, String descColumn) {
        String sql = """
                SELECT e.id,
                       ee.codigo AS estado_codigo, ee.%s AS estado_desc,
                       e.instructor_id,
                       ins.nombre AS instructor_nombre, ins.apellidos AS instructor_apellidos
                FROM   expediente e
                JOIN   estado_expediente ee ON ee.id = e.estado_id
                LEFT JOIN usuario ins       ON ins.id = e.instructor_id
                WHERE  e.id = :id
                """.formatted(descColumn);

        List<ExpedienteTramitacionInfo> resultado = jdbc.query(sql, new MapSqlParameterSource("id", expedienteId),
                (rs, rowNum) -> new ExpedienteTramitacionInfo(
                        rs.getLong("id"),
                        new CatalogoDto(rs.getString("estado_codigo"), rs.getString("estado_desc")),
                        rs.getObject("instructor_id", Long.class),
                        nombreCompleto(rs.getString("instructor_nombre"), rs.getString("instructor_apellidos"))
                ));

        return resultado.stream().findFirst();
    }

    private static final String SELECT_TRAMITE = """
            SELECT t.id,
                   tt.codigo AS tipo_codigo, tt.%s AS tipo_desc,
                   t.fecha, t.resultado, t.observaciones,
                   u.username, u.nombre AS usuario_nombre, u.apellidos AS usuario_apellidos
            FROM   tramite t
            JOIN   tipo_tramite tt ON tt.id = t.tipo_tramite_id
            JOIN   usuario u       ON u.id = t.usuario_id
            """;

    private static final RowMapper<TramiteDto> TRAMITE_MAPPER = (rs, rowNum) -> new TramiteDto(
            rs.getLong("id"),
            new CatalogoDto(rs.getString("tipo_codigo"), rs.getString("tipo_desc")),
            rs.getTimestamp("fecha").toLocalDateTime(),
            rs.getString("username"),
            nombreCompleto(rs.getString("usuario_nombre"), rs.getString("usuario_apellidos")),
            rs.getString("resultado"),
            rs.getString("observaciones"));

    /** Histórico de trámites del expediente, del más reciente al más antiguo. */
    public List<TramiteDto> buscarPorExpediente(Long expedienteId, String descColumn) {
        String sql = SELECT_TRAMITE.formatted(descColumn) + """
                WHERE  t.expediente_id = :id
                ORDER BY t.fecha DESC, t.id DESC
                """;

        return jdbc.query(sql, new MapSqlParameterSource("id", expedienteId), TRAMITE_MAPPER);
    }

    /** Un trámite concreto — para devolver el recién creado sin releer todo el histórico. */
    public Optional<TramiteDto> buscarPorId(Long tramiteId, String descColumn) {
        String sql = SELECT_TRAMITE.formatted(descColumn) + "WHERE t.id = :id\n";

        return jdbc.query(sql, new MapSqlParameterSource("id", tramiteId), TRAMITE_MAPPER)
                .stream().findFirst();
    }

    /** Inserta el trámite y devuelve su id generado. */
    public Long insertar(Long expedienteId, String codigoTipoTramite, Long usuarioId,
                         LocalDateTime fecha, String resultado, String observaciones) {

        String sql = """
                INSERT INTO tramite (expediente_id, tipo_tramite_id, fecha, usuario_id, resultado, observaciones)
                VALUES (:expedienteId,
                        (SELECT id FROM tipo_tramite WHERE codigo = :codigoTipo),
                        :fecha, :usuarioId, :resultado, :observaciones)
                """;

        var params = new MapSqlParameterSource()
                .addValue("expedienteId", expedienteId)
                .addValue("codigoTipo", codigoTipoTramite)
                .addValue("fecha", fecha)
                .addValue("usuarioId", usuarioId)
                .addValue("resultado", resultado)
                .addValue("observaciones", observaciones);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, params, keyHolder, new String[]{"id"});

        Number id = keyHolder.getKey();
        return id == null ? null : id.longValue();
    }

    /**
     * Cambia el estado del expediente por código de catálogo. Devuelve cuántas filas
     * cambiaron (0 = el expediente no existe).
     */
    public int actualizarEstado(Long expedienteId, String codigoEstado) {
        String sql = """
                UPDATE expediente
                SET    estado_id = (SELECT id FROM estado_expediente WHERE codigo = :codigoEstado)
                WHERE  id = :id
                """;

        return jdbc.update(sql, new MapSqlParameterSource()
                .addValue("codigoEstado", codigoEstado)
                .addValue("id", expedienteId));
    }

    /** Cambia el estado a RESUELTO y sella la fecha de resolución en la misma sentencia. */
    public int marcarResuelto(Long expedienteId, LocalDate fechaResolucion) {
        String sql = """
                UPDATE expediente
                SET    estado_id = (SELECT id FROM estado_expediente WHERE codigo = 'RESUELTO'),
                       fecha_resolucion = :fechaResolucion
                WHERE  id = :id
                """;

        return jdbc.update(sql, new MapSqlParameterSource()
                .addValue("fechaResolucion", fechaResolucion)
                .addValue("id", expedienteId));
    }

    /** Asigna el instructor al expediente. Devuelve cuántas filas cambiaron. */
    public int asignarInstructor(Long expedienteId, Long instructorId) {
        String sql = "UPDATE expediente SET instructor_id = :instructorId WHERE id = :id";

        return jdbc.update(sql, new MapSqlParameterSource()
                .addValue("instructorId", instructorId)
                .addValue("id", expedienteId));
    }

    /** Id del usuario a partir del username del JWT. {@code empty} si no existe o está inactivo. */
    public Optional<Long> buscarIdUsuarioActivo(String username) {
        List<Long> ids = jdbc.queryForList(
                "SELECT id FROM usuario WHERE username = :username AND activo = TRUE",
                new MapSqlParameterSource("username", username), Long.class);

        return ids.stream().findFirst();
    }

    /**
     * Comprueba que el usuario existe, está activo y tiene rol INSTRUCTOR — no vale
     * asignar un expediente a un consultor o a un usuario dado de baja.
     */
    public boolean esInstructorActivo(Long usuarioId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM   usuario u
                    JOIN   usuario_rol ur ON ur.usuario_id = u.id
                    JOIN   rol r          ON r.id = ur.rol_id
                    WHERE  u.id = :id AND u.activo = TRUE AND r.codigo = 'INSTRUCTOR'
                )
                """;

        Boolean existe = jdbc.queryForObject(sql, new MapSqlParameterSource("id", usuarioId), Boolean.class);
        return Boolean.TRUE.equals(existe);
    }

    /**
     * Instructores activos a los que se puede asignar un expediente, por orden alfabético.
     *
     * DISTINCT porque un usuario puede tener varios roles: sin él, alguien que fuera
     * INSTRUCTOR y RESPONSABLE a la vez saldría repetido en la lista.
     */
    public List<InstructorDto> listarInstructoresActivos() {
        String sql = """
                SELECT DISTINCT u.id, u.username, u.nombre, u.apellidos
                FROM   usuario u
                JOIN   usuario_rol ur ON ur.usuario_id = u.id
                JOIN   rol r          ON r.id = ur.rol_id
                WHERE  u.activo = TRUE AND r.codigo = 'INSTRUCTOR'
                ORDER BY u.apellidos, u.nombre
                """;

        return jdbc.query(sql, new MapSqlParameterSource(), (rs, rowNum) -> new InstructorDto(
                rs.getLong("id"),
                rs.getString("username"),
                nombreCompleto(rs.getString("nombre"), rs.getString("apellidos"))
        ));
    }

    /** Comprueba que el código de tipo de trámite existe en el catálogo. */
    public boolean existeTipoTramite(String codigo) {
        Boolean existe = jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM tipo_tramite WHERE codigo = :codigo)",
                new MapSqlParameterSource("codigo", codigo), Boolean.class);

        return Boolean.TRUE.equals(existe);
    }

    private static String nombreCompleto(String nombre, String apellidos) {
        return nombre == null ? null : nombre + " " + apellidos;
    }
}
