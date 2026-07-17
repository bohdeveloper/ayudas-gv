package com.ayudasgv.auth.service;

import com.ayudasgv.auth.dto.LoginRequest;
import com.ayudasgv.auth.dto.LoginResponse;
import com.ayudasgv.auth.model.Usuario;
import com.ayudasgv.auth.repository.UsuarioRepository;
import com.ayudasgv.auth.security.JwtProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de {@link AuthService}: los tres caminos que documenta su
 * propio Javadoc (login correcto, usuario inexistente, contraseña incorrecta).
 * Los colaboradores (repositorio, encoder, proveedor JWT) se simulan con Mockito
 * para probar solo la lógica de negocio, sin base de datos real.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private AuthService authService;

    private static final Usuario USUARIO = new Usuario(
            1L, "admin", "hash-bcrypt", "Administrador", "Sistema",
            "admin@ayudasgv.eus", "es", true, List.of("ADMIN", "RESPONSABLE"));

    @Test
    void login_conCredencialesValidas_devuelveTokenYDatosDeUsuario() {
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(USUARIO));
        when(passwordEncoder.matches("admin123", "hash-bcrypt")).thenReturn(true);
        when(jwtProvider.generateToken("admin", List.of("ADMIN", "RESPONSABLE"), "es"))
                .thenReturn("jwt-generado");

        LoginResponse response = authService.login(new LoginRequest("admin", "admin123"));

        assertThat(response.token()).isEqualTo("jwt-generado");
        assertThat(response.username()).isEqualTo("admin");
        assertThat(response.nombre()).isEqualTo("Administrador Sistema");
        assertThat(response.roles()).containsExactly("ADMIN", "RESPONSABLE");
        assertThat(response.idiomaPreferido()).isEqualTo("es");
    }

    @Test
    void login_conUsuarioInexistente_lanzaBadCredentials() {
        when(usuarioRepository.findByUsername("fantasma")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("fantasma", "cualquiera")))
                .isInstanceOf(BadCredentialsException.class);

        verifyNoInteractions(jwtProvider);
    }

    @Test
    void login_conContrasenaIncorrecta_lanzaBadCredentialsYNoGeneraToken() {
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(USUARIO));
        when(passwordEncoder.matches(eq("mala-clave"), any())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin", "mala-clave")))
                .isInstanceOf(BadCredentialsException.class);

        verifyNoInteractions(jwtProvider);
    }
}
