package com.ayudasgv.auth.service;

import com.ayudasgv.auth.dto.LoginRequest;
import com.ayudasgv.auth.dto.LoginResponse;
import com.ayudasgv.auth.model.Usuario;
import com.ayudasgv.auth.repository.UsuarioRepository;
import com.ayudasgv.auth.security.JwtProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Lógica de negocio del proceso de autenticación.
 *
 * Esta capa de servicio orquesta el flujo de login:
 *   1. Buscar el usuario en BD por username (UsuarioRepository).
 *   2. Verificar la contraseña en claro contra el hash BCrypt almacenado.
 *   3. Si las credenciales son correctas, generar y devolver un JWT.
 *   4. Si fallan, lanzar BadCredentialsException → el controlador devuelve 401.
 *
 * Principio de seguridad importante:
 *   Tanto si el usuario no existe como si la contraseña es incorrecta, se lanza
 *   la misma excepción con el mismo mensaje. Esto impide que un atacante pueda
 *   distinguir entre "usuario inexistente" y "contraseña incorrecta" (user enumeration).
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    // Inyección por constructor (preferible a @Autowired en campo — más testeable)
    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtProvider jwtProvider) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    /**
     * Procesa un intento de login y devuelve el token JWT si las credenciales son válidas.
     *
     * @param request DTO con username y password en claro
     * @return LoginResponse con el JWT y datos del usuario
     * @throws BadCredentialsException si el usuario no existe o la contraseña no coincide
     */
    public LoginResponse login(LoginRequest request) {
        // 1. Buscar usuario en BD (devuelve Optional vacío si no existe o está inactivo)
        Usuario usuario = usuarioRepository.findByUsername(request.username())
                .orElseThrow(() -> {
                    // Logeamos a nivel WARN para detectar intentos de acceso inválidos
                    log.warn("Intento de login con usuario inexistente: {}", request.username());
                    return new BadCredentialsException("auth.login.error");
                });

        // 2. Verificar la contraseña: compara el texto plano con el hash BCrypt de BD
        //    passwordEncoder.matches() aplica el mismo salt que usó el hash original
        if (!passwordEncoder.matches(request.password(), usuario.passwordHash())) {
            log.warn("Contraseña incorrecta para el usuario: {}", request.username());
            throw new BadCredentialsException("auth.login.error");
        }

        // 3. Credenciales válidas → generar JWT firmado
        String token = jwtProvider.generateToken(
                usuario.username(),
                usuario.roles(),
                usuario.idiomaPreferido()
        );

        log.info("Login exitoso para usuario: {} con roles: {}", usuario.username(), usuario.roles());

        return new LoginResponse(
                token,
                usuario.username(),
                usuario.nombre() + " " + usuario.apellidos(),
                usuario.roles(),
                usuario.idiomaPreferido()
        );
    }
}
