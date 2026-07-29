package com.ayudasgv.config;

import com.ayudasgv.auth.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración completa de Spring Security con autenticación JWT.
 *
 * La cadena de filtros de Spring Security es una serie de objetos Filter que
 * se ejecutan en orden para cada petición HTTP. Aquí definimos:
 *   1. Qué rutas son públicas (no requieren token).
 *   2. Nuestro filtro JWT (JwtAuthenticationFilter) que valida el token antes
 *      de que Spring Security compruebe si el usuario está autenticado.
 *   3. Política de sesión STATELESS: sin HttpSession, sin cookies de sesión.
 *      El estado del usuario vive solo en el JWT de cada petición.
 *
 * Orden de los filtros relevantes:
 *   ... → JwtAuthenticationFilter → UsernamePasswordAuthenticationFilter → ...
 *   Nuestro filtro va ANTES porque necesita poblar el SecurityContext antes
 *   de que Spring compruebe si la petición está autenticada.
 *
 * @EnableMethodSecurity activa las anotaciones @PreAuthorize/@PostAuthorize a
 * nivel de método. En la Fase 0 todos los roles (CONSULTOR, INSTRUCTOR,
 * RESPONSABLE, ADMIN) tienen los mismos permisos de solo lectura, así que hoy
 * no hay ningún @PreAuthorize en los controladores — pero la infraestructura
 * queda lista para la Fase 1, donde la tramitación se restringe por rol.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // Rutas públicas: no requieren token JWT
    private static final String[] PUBLIC_PATHS = {
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v1/auth/login",          // el endpoint de login debe ser público — aquí es donde se obtiene el token
        // Probes de Kubernetes: las lanza el kubelet del nodo, que no tiene ni puede tener un JWT.
        // Solo se abren los dos grupos de salud (liveness/readiness) y devuelven {"status":"UP"}
        // sin detalles (management.endpoint.health.show-details=never), así que no filtran nada.
        "/actuator/health",
        "/actuator/health/**"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Cadena de filtros HTTP de Spring Security:
     *
     * - csrf disable: sin cookies de sesión, CSRF no aplica.
     * - sessionManagement STATELESS: Spring no crea ni usa HttpSession.
     *   Cada petición es independiente; la autenticación viene en el JWT.
     * - authorizeHttpRequests: rutas públicas libres, el resto requiere autenticación.
     * - addFilterBefore: inserta nuestro filtro JWT antes del filtro de formulario
     *   de Spring (UsernamePasswordAuthenticationFilter), que no usamos pero es el
     *   punto de referencia estándar en la cadena.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_PATHS).permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            // Sin token o con token inválido → 401 (no 403).
            // Spring Security 6 devuelve 403 para peticiones anónimas por defecto.
            // exceptionHandling.authenticationEntryPoint intercepta esos casos y devuelve 401.
            .exceptionHandling(ex -> ex.authenticationEntryPoint(
                (request, response, authException) ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No autenticado")
            ));

        return http.build();
    }

    /**
     * Registra BCryptPasswordEncoder como implementación de PasswordEncoder.
     *
     * Este bean es necesario para:
     *   1. Que AuthService pueda inyectar PasswordEncoder y llamar a matches().
     *   2. Que el factor de coste sea consistente en toda la aplicación (strength 10).
     *
     * BCrypt es el estándar actual para contraseñas: incluye salt automático y
     * el número de iteraciones se puede aumentar sin necesidad de re-hashear.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Strength 10 = 2^10 iteraciones de hashing. Balance seguridad/rendimiento (recomendado).
        return new BCryptPasswordEncoder(10);
    }
}
