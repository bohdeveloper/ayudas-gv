package com.ayudasgv.expedientes.repository;

import com.ayudasgv.expedientes.dto.CatalogoDto;
import com.ayudasgv.expedientes.dto.ExpedienteBusquedaCriteria;
import com.ayudasgv.expedientes.dto.ExpedienteResumenDto;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Acceso a datos de la búsqueda de expedientes con SQL explícito (sin JPA), igual
 * que {@code UsuarioRepository}.
 *
 * La consulta se construye dinámicamente: cada filtro solo aparece en el WHERE si
 * el usuario lo ha indicado, y siempre como parámetro nombrado (nunca concatenando
 * el valor recibido en el SQL) para evitar inyección SQL.
 *
 * La columna de descripción del catálogo (es/eu) se selecciona con un nombre de
 * columna fijo elegido en código Java a partir de una lista cerrada de dos valores
 * ("descripcion_es" / "descripcion_eu") — nunca a partir de una cadena que venga
 * directamente del cliente, así que no hay riesgo de inyección por ese lado.
 */
@Repository
public class ExpedienteRepository {

    // Columnas de orden permitidas: cualquier otro valor de "sort" se ignora y se usa el por defecto.
    private static final Map<String, String> SORT_COLUMNS = Map.of(
            "numeroExpediente", "e.numero_expediente",
            "fechaRegistro", "e.fecha_registro",
            "estado", "ee.orden",
            "tipoAyuda", "ta.codigo"
    );
    private static final String DEFAULT_SORT_COLUMN = "e.fecha_registro";
    private static final String DEFAULT_SORT_DIRECTION = "DESC";

    private static final String FROM_JOIN = """
            FROM   expediente e
            JOIN   tipo_ayuda ta          ON ta.id = e.tipo_ayuda_id
            JOIN   estado_expediente ee   ON ee.id = e.estado_id
            LEFT JOIN usuario ins         ON ins.id = e.instructor_id
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public ExpedienteRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Busca expedientes aplicando los filtros indicados y devuelve solo la página pedida.
     *
     * @param criteria   filtros, paginación y orden solicitados
     * @param descColumn "descripcion_es" o "descripcion_eu" — columna de catálogo a devolver
     */
    public List<ExpedienteResumenDto> buscar(ExpedienteBusquedaCriteria criteria, String descColumn) {
        var params = new MapSqlParameterSource();
        String where = buildWhereClause(criteria, params);
        String orderBy = buildOrderByClause(criteria.sort());

        String sql = """
                SELECT e.id,
                       e.numero_expediente,
                       ta.codigo AS tipo_ayuda_codigo, ta.%1$s AS tipo_ayuda_desc,
                       ee.codigo AS estado_codigo,     ee.%1$s AS estado_desc,
                       e.fecha_registro,
                       ins.nombre AS instructor_nombre, ins.apellidos AS instructor_apellidos
                """.formatted(descColumn) + FROM_JOIN + where + orderBy + """
                LIMIT :limit OFFSET :offset
                """;

        params.addValue("limit", criteria.size());
        params.addValue("offset", (long) criteria.page() * criteria.size());

        return jdbc.query(sql, params, (rs, rowNum) -> {
            String instructorNombre = rs.getString("instructor_nombre");
            String instructorCompleto = instructorNombre == null
                    ? null
                    : instructorNombre + " " + rs.getString("instructor_apellidos");

            return new ExpedienteResumenDto(
                    rs.getLong("id"),
                    rs.getString("numero_expediente"),
                    new CatalogoDto(rs.getString("tipo_ayuda_codigo"), rs.getString("tipo_ayuda_desc")),
                    new CatalogoDto(rs.getString("estado_codigo"), rs.getString("estado_desc")),
                    toLocalDateTime(rs.getTimestamp("fecha_registro")),
                    instructorCompleto
            );
        });
    }

    /** Cuenta el total de expedientes que cumplen los mismos filtros, para calcular la paginación. */
    public long contar(ExpedienteBusquedaCriteria criteria) {
        var params = new MapSqlParameterSource();
        String where = buildWhereClause(criteria, params);
        String sql = "SELECT COUNT(*) " + FROM_JOIN + where;

        Long total = jdbc.queryForObject(sql, params, Long.class);
        return total == null ? 0L : total;
    }

    private String buildWhereClause(ExpedienteBusquedaCriteria c, MapSqlParameterSource params) {
        var conditions = new java.util.ArrayList<String>();

        if (hasText(c.numeroExpediente())) {
            conditions.add("e.numero_expediente ILIKE :numeroExpediente");
            params.addValue("numeroExpediente", "%" + c.numeroExpediente().trim() + "%");
        }
        if (hasText(c.tipoAyuda())) {
            conditions.add("ta.codigo = :tipoAyuda");
            params.addValue("tipoAyuda", c.tipoAyuda().trim());
        }
        if (hasText(c.estado())) {
            conditions.add("ee.codigo = :estado");
            params.addValue("estado", c.estado().trim());
        }
        if (hasText(c.documentoSolicitante())) {
            conditions.add("""
                    EXISTS (
                        SELECT 1 FROM expediente_interesado ei
                        JOIN persona p ON p.id = ei.persona_id
                        WHERE ei.expediente_id = e.id
                          AND ei.rol_interesado = 'SOLICITANTE'
                          AND p.numero_documento ILIKE :documentoSolicitante
                    )
                    """);
            params.addValue("documentoSolicitante", "%" + c.documentoSolicitante().trim() + "%");
        }
        if (c.fechaDesde() != null) {
            conditions.add("e.fecha_registro >= :fechaDesde");
            params.addValue("fechaDesde", c.fechaDesde().atStartOfDay());
        }
        if (c.fechaHasta() != null) {
            conditions.add("e.fecha_registro < :fechaHasta");
            params.addValue("fechaHasta", c.fechaHasta().plusDays(1).atStartOfDay());
        }

        return conditions.isEmpty() ? "" : "WHERE " + String.join(" AND ", conditions) + "\n";
    }

    private String buildOrderByClause(String sort) {
        String column = DEFAULT_SORT_COLUMN;
        String direction = DEFAULT_SORT_DIRECTION;

        if (hasText(sort)) {
            String[] parts = sort.split(",", 2);
            String mapped = SORT_COLUMNS.get(parts[0].trim());
            if (mapped != null) {
                column = mapped;
                if (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim())) {
                    direction = "ASC";
                }
            }
        }

        return "ORDER BY " + column + " " + direction + "\n";
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}
