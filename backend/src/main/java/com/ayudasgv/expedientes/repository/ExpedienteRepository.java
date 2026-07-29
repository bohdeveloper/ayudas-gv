package com.ayudasgv.expedientes.repository;

import com.ayudasgv.commons.dto.CatalogoDto;
import com.ayudasgv.expedientes.dto.DetalleAsignacionDiscapacidadDto;
import com.ayudasgv.expedientes.dto.DetalleAyudaDto;
import com.ayudasgv.expedientes.dto.DetalleNacimientoAdopcionDto;
import com.ayudasgv.expedientes.dto.DetallePartoMultipleDto;
import com.ayudasgv.expedientes.dto.ExpedienteBusquedaCriteria;
import com.ayudasgv.expedientes.dto.ExpedienteResumenDto;
import com.ayudasgv.expedientes.dto.PersonaDto;
import com.ayudasgv.expedientes.dto.SeccionAyudaDto;
import com.ayudasgv.expedientes.dto.SeccionContactoDto;
import com.ayudasgv.expedientes.dto.SeccionGeneralesDto;
import com.ayudasgv.expedientes.dto.SeccionInteresadosDto;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    /** Comprueba si existe un expediente con ese id — usado para devolver 404 en las secciones de detalle. */
    public boolean existeExpediente(Long id) {
        Boolean existe = jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM expediente WHERE id = :id)",
                new MapSqlParameterSource("id", id),
                Boolean.class);
        return Boolean.TRUE.equals(existe);
    }

    /** Sección "Datos generales" de un expediente concreto. */
    public Optional<SeccionGeneralesDto> buscarGenerales(Long id, String descColumn) {
        String sql = """
                SELECT e.id,
                       e.numero_expediente,
                       ta.codigo AS tipo_ayuda_codigo, ta.%1$s AS tipo_ayuda_desc,
                       ee.codigo AS estado_codigo,     ee.%1$s AS estado_desc,
                       e.fecha_registro,
                       e.fecha_resolucion,
                       ins.nombre AS instructor_nombre, ins.apellidos AS instructor_apellidos
                """.formatted(descColumn) + FROM_JOIN + "WHERE e.id = :id\n";

        List<SeccionGeneralesDto> resultado = jdbc.query(sql, new MapSqlParameterSource("id", id), (rs, rowNum) -> {
            String instructorNombre = rs.getString("instructor_nombre");
            String instructorCompleto = instructorNombre == null
                    ? null
                    : instructorNombre + " " + rs.getString("instructor_apellidos");

            return new SeccionGeneralesDto(
                    rs.getLong("id"),
                    rs.getString("numero_expediente"),
                    new CatalogoDto(rs.getString("tipo_ayuda_codigo"), rs.getString("tipo_ayuda_desc")),
                    new CatalogoDto(rs.getString("estado_codigo"), rs.getString("estado_desc")),
                    toLocalDateTime(rs.getTimestamp("fecha_registro")),
                    rs.getObject("fecha_resolucion", LocalDate.class),
                    instructorCompleto
            );
        });

        return resultado.stream().findFirst();
    }

    /** Sección "Interesados" de un expediente concreto: solicitante, representante (opcional) y causantes. */
    public SeccionInteresadosDto buscarInteresados(Long id) {
        String sql = """
                SELECT ei.rol_interesado,
                       p.id, p.tipo_documento, p.numero_documento, p.nombre, p.apellido1, p.apellido2, p.fecha_nacimiento
                FROM   expediente_interesado ei
                JOIN   persona p ON p.id = ei.persona_id
                WHERE  ei.expediente_id = :id
                """;

        PersonaDto[] solicitante = new PersonaDto[1];
        PersonaDto[] representante = new PersonaDto[1];
        List<PersonaDto> causantes = new java.util.ArrayList<>();

        jdbc.query(sql, new MapSqlParameterSource("id", id), rs -> {
            PersonaDto persona = mapPersona(rs);
            switch (rs.getString("rol_interesado")) {
                case "SOLICITANTE" -> solicitante[0] = persona;
                case "REPRESENTANTE" -> representante[0] = persona;
                case "CAUSANTE" -> causantes.add(persona);
                default -> { /* rol desconocido: se ignora */ }
            }
        });

        return new SeccionInteresadosDto(solicitante[0], representante[0], causantes);
    }

    /** Sección "Contacto y aviso" de un expediente concreto (relación 1:1 opcional). */
    public Optional<SeccionContactoDto> buscarContacto(Long id) {
        String sql = """
                SELECT via, numero, piso, codigo_postal, municipio, provincia, telefono, email,
                       canal_aviso, idioma_notificacion, acepta_notif_telematica
                FROM   datos_contacto
                WHERE  expediente_id = :id
                """;

        List<SeccionContactoDto> resultado = jdbc.query(sql, new MapSqlParameterSource("id", id), (rs, rowNum) ->
                new SeccionContactoDto(
                        rs.getString("via"),
                        rs.getString("numero"),
                        rs.getString("piso"),
                        rs.getString("codigo_postal"),
                        rs.getString("municipio"),
                        rs.getString("provincia"),
                        rs.getString("telefono"),
                        rs.getString("email"),
                        rs.getString("canal_aviso"),
                        rs.getString("idioma_notificacion"),
                        rs.getBoolean("acepta_notif_telematica")
                ));

        return resultado.stream().findFirst();
    }

    /** Sección "Datos de la ayuda" de un expediente concreto (relación 1:1 opcional con detalle por tipo). */
    public Optional<SeccionAyudaDto> buscarAyuda(Long id) {
        String sql = """
                SELECT sa.id AS solicitud_id, sa.fecha_solicitud, sa.importe_solicitado, sa.observaciones,
                       ta.codigo AS tipo_ayuda_codigo
                FROM   solicitud_ayuda sa
                JOIN   expediente e  ON e.id = sa.expediente_id
                JOIN   tipo_ayuda ta ON ta.id = e.tipo_ayuda_id
                WHERE  sa.expediente_id = :id
                """;

        List<Map<String, Object>> filas = jdbc.queryForList(sql, new MapSqlParameterSource("id", id));
        if (filas.isEmpty()) {
            return Optional.empty();
        }

        Map<String, Object> fila = filas.get(0);
        long solicitudId = ((Number) fila.get("solicitud_id")).longValue();
        String tipoAyudaCodigo = (String) fila.get("tipo_ayuda_codigo");

        DetalleAyudaDto detalle = buscarDetalleAyuda(solicitudId, tipoAyudaCodigo);

        return Optional.of(new SeccionAyudaDto(
                ((java.sql.Date) fila.get("fecha_solicitud")).toLocalDate(),
                (java.math.BigDecimal) fila.get("importe_solicitado"),
                (String) fila.get("observaciones"),
                detalle
        ));
    }

    private DetalleAyudaDto buscarDetalleAyuda(long solicitudId, String tipoAyudaCodigo) {
        var params = new MapSqlParameterSource("solicitudId", solicitudId);

        return switch (tipoAyudaCodigo) {
            case "ASIG_DISCAP" -> {
                String sql = """
                        SELECT d.grado_discapacidad, d.necesita_tercera_persona,
                               p.id, p.tipo_documento, p.numero_documento, p.nombre, p.apellido1, p.apellido2, p.fecha_nacimiento
                        FROM   detalle_asignacion_discapacidad d
                        JOIN   persona p ON p.id = d.causante_persona_id
                        WHERE  d.solicitud_id = :solicitudId
                        """;
                yield jdbc.query(sql, params, (rs, rowNum) -> (DetalleAyudaDto) new DetalleAsignacionDiscapacidadDto(
                        "ASIG_DISCAP",
                        rs.getInt("grado_discapacidad"),
                        rs.getBoolean("necesita_tercera_persona"),
                        mapPersona(rs)
                )).stream().findFirst().orElse(null);
            }
            case "PAGO_NAC" -> {
                String sql = """
                        SELECT supuesto, fecha_hecho, ingresos_anuales, numero_hijos_cargo
                        FROM   detalle_nacimiento_adopcion
                        WHERE  solicitud_id = :solicitudId
                        """;
                yield jdbc.query(sql, params, (rs, rowNum) -> (DetalleAyudaDto) new DetalleNacimientoAdopcionDto(
                        "PAGO_NAC",
                        rs.getString("supuesto"),
                        rs.getObject("fecha_hecho", LocalDate.class),
                        rs.getBigDecimal("ingresos_anuales"),
                        rs.getObject("numero_hijos_cargo", Integer.class)
                )).stream().findFirst().orElse(null);
            }
            case "PARTO_MULT" -> {
                String sql = """
                        SELECT numero_nacidos, fecha_hecho
                        FROM   detalle_parto_multiple
                        WHERE  solicitud_id = :solicitudId
                        """;
                yield jdbc.query(sql, params, (rs, rowNum) -> (DetalleAyudaDto) new DetallePartoMultipleDto(
                        "PARTO_MULT",
                        rs.getObject("numero_nacidos", Integer.class),
                        rs.getObject("fecha_hecho", LocalDate.class)
                )).stream().findFirst().orElse(null);
            }
            default -> null;
        };
    }

    private static PersonaDto mapPersona(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new PersonaDto(
                rs.getLong("id"),
                rs.getString("tipo_documento"),
                rs.getString("numero_documento"),
                rs.getString("nombre"),
                rs.getString("apellido1"),
                rs.getString("apellido2"),
                rs.getObject("fecha_nacimiento", LocalDate.class)
        );
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
