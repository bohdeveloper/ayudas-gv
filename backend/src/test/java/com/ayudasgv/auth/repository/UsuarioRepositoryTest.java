package com.ayudasgv.auth.repository;

import com.ayudasgv.auth.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de repositorio con SQL real: ejecuta las migraciones Flyway contra una BD H2
 * en memoria en modo compatibilidad PostgreSQL (alternativa a Testcontainers que pide
 * la sección 10 del enunciado) y ejercita {@link UsuarioRepository} sin mocks.
 *
 * @JdbcTest configura un DataSource embebido + NamedParameterJdbcTemplate y, por
 * defecto, también ejecuta Flyway contra él — así el esquema y los datos semilla
 * (V1__security_catalogs.sql) son exactamente los mismos que en producción.
 */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:usuario_repo_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.flyway.enabled=true"
})
@Import(UsuarioRepository.class)
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void findByUsername_conUsuarioConDosRoles_losAgrupaEnUnSoloUsuario() {
        // Dato semilla (V1): 'admin' tiene ADMIN + RESPONSABLE — el JOIN con usuario_rol/rol
        // devuelve 2 filas para este usuario; el repositorio debe agruparlas en un único Usuario.
        Optional<Usuario> resultado = usuarioRepository.findByUsername("admin");

        assertThat(resultado).isPresent();
        Usuario admin = resultado.get();
        assertThat(admin.username()).isEqualTo("admin");
        assertThat(admin.nombre()).isEqualTo("Administrador");
        assertThat(admin.idiomaPreferido()).isEqualTo("es");
        assertThat(admin.roles()).containsExactlyInAnyOrder("ADMIN", "RESPONSABLE");
    }

    @Test
    void findByUsername_conUsuarioDeUnSoloRol_devuelveEseRol() {
        // Dato semilla (V1): 'consultor1' tiene un único rol, CONSULTOR.
        Optional<Usuario> resultado = usuarioRepository.findByUsername("consultor1");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().roles()).containsExactly("CONSULTOR");
        assertThat(resultado.get().idiomaPreferido()).isEqualTo("es");
    }

    @Test
    void findByUsername_conUsuarioInexistente_devuelveOptionalVacio() {
        Optional<Usuario> resultado = usuarioRepository.findByUsername("no-existe");

        assertThat(resultado).isEmpty();
    }

    @Test
    void findByUsername_devuelveElHashBcryptParaQueAuthServiceLoCompare() {
        // El repositorio no valida la contraseña (eso es responsabilidad de AuthService);
        // solo debe devolver el hash tal cual está en BD.
        Optional<Usuario> resultado = usuarioRepository.findByUsername("instructor1");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().passwordHash()).startsWith("$2a$10$");
    }
}
