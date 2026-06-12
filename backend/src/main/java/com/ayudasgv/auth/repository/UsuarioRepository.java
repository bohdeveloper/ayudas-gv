package com.ayudasgv.auth.repository;

import com.ayudasgv.auth.model.Usuario;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Acceso a datos de usuarios usando SQL explícito con NamedParameterJdbcTemplate.
 *
 * El proyecto usa JDBC directo (sin JPA/Hibernate) para aprender SQL real.
 * NamedParameterJdbcTemplate es preferible a JdbcTemplate básico porque los
 * parámetros se identifican por nombre (:username) en lugar de por posición (?),
 * lo que hace el código más legible y menos propenso a errores.
 *
 * Patrón de agrupación de roles:
 *   El JOIN usuario → usuario_rol → rol devuelve una fila POR ROL que tiene el usuario.
 *   Si el usuario tiene 2 roles, obtenemos 2 filas con los mismos datos de usuario
 *   pero distinto valor en 'rol_codigo'. Usamos un Map<Long, Usuario> para agrupar.
 */
@Repository
public class UsuarioRepository {

    // SQL que obtiene usuario + todos sus roles en una sola consulta con JOIN
    private static final String SQL_FIND_BY_USERNAME = """
            SELECT u.id,
                   u.username,
                   u.password_hash,
                   u.nombre,
                   u.apellidos,
                   u.email,
                   u.idioma_preferido,
                   u.activo,
                   r.codigo AS rol_codigo
            FROM   usuario u
            LEFT JOIN usuario_rol ur ON ur.usuario_id = u.id
            LEFT JOIN rol          r  ON r.id = ur.rol_id
            WHERE  u.username = :username
              AND  u.activo   = true
            """;

    private final NamedParameterJdbcTemplate jdbc;

    // Spring inyecta el NamedParameterJdbcTemplate automáticamente (autoconfigured con spring-boot-starter-jdbc)
    public UsuarioRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Busca un usuario activo por su nombre de usuario, cargando también sus roles.
     *
     * Devuelve Optional.empty() si el usuario no existe o está inactivo (activo = false),
     * lo que permite al servicio devolver 401 sin revelar si el usuario existe o no.
     *
     * @param username nombre de usuario a buscar
     * @return Optional con el usuario y sus roles, o vacío si no se encuentra
     */
    public Optional<Usuario> findByUsername(String username) {
        var params = new MapSqlParameterSource("username", username);

        // Mapa auxiliar para agrupar: id_usuario → datos del usuario
        // LinkedHashMap mantiene el orden de inserción (buena práctica aunque aquí no importa)
        Map<Long, UsuarioBuilder> builders = new LinkedHashMap<>();

        jdbc.query(SQL_FIND_BY_USERNAME, params, rs -> {
            // Extraemos todos los valores del ResultSet ANTES del computeIfAbsent.
            // Esto es necesario porque los métodos de ResultSet lanzan SQLException
            // (checked exception) y la interfaz Function que usa computeIfAbsent
            // no la declara en su firma — causaría error de compilación si se llamaran dentro.
            long id       = rs.getLong("id");
            String uname  = rs.getString("username");
            String pwHash = rs.getString("password_hash");
            String nombre = rs.getString("nombre");
            String apells = rs.getString("apellidos");
            String email  = rs.getString("email");
            String idioma = rs.getString("idioma_preferido");
            boolean activo = rs.getBoolean("activo");
            String rol    = rs.getString("rol_codigo");

            // Primera vez que vemos este usuario: creamos su builder con los valores ya leídos
            builders.computeIfAbsent(id, k -> new UsuarioBuilder(
                    id, uname, pwHash, nombre, apells, email, idioma, activo
            ));

            // Añadimos el rol de esta fila (puede ser null si el usuario no tiene roles)
            if (rol != null) {
                builders.get(id).roles().add(rol);
            }
        });

        return builders.values().stream()
                .findFirst()
                .map(UsuarioBuilder::build);
    }

    /**
     * Clase auxiliar interna para construir el Usuario acumulando roles
     * mientras se procesan las filas del ResultSet.
     *
     * Es privada porque es un detalle de implementación del repositorio.
     */
    private record UsuarioBuilder(
            Long id,
            String username,
            String passwordHash,
            String nombre,
            String apellidos,
            String email,
            String idiomaPreferido,
            boolean activo,
            List<String> roles
    ) {
        // Constructor alternativo que inicializa la lista de roles vacía
        UsuarioBuilder(Long id, String username, String passwordHash,
                       String nombre, String apellidos, String email,
                       String idiomaPreferido, boolean activo) {
            this(id, username, passwordHash, nombre, apellidos, email,
                 idiomaPreferido, activo, new ArrayList<>());
        }

        Usuario build() {
            return new Usuario(id, username, passwordHash, nombre, apellidos,
                               email, idiomaPreferido, activo, List.copyOf(roles));
        }
    }
}
