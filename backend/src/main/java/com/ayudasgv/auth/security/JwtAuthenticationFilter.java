package com.ayudasgv.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro JWT que intercepta cada petición HTTP para validar el token de autenticación.
 *
 * Extiende OncePerRequestFilter para garantizar que la lógica se ejecuta exactamente
 * una vez por petición (evita problemas con forwards internos de Spring MVC).
 *
 * Flujo de cada petición:
 *   1. Leer la cabecera "Authorization: Bearer <token>".
 *   2. Si no hay token o no empieza por "Bearer ", continuar sin autenticar
 *      (Spring Security denegará el acceso si la ruta es protegida).
 *   3. Validar el token con JwtProvider (firma, expiración, formato).
 *   4. Si es válido: extraer username y roles, construir el objeto Authentication
 *      y guardarlo en SecurityContextHolder.
 *   5. Pasar la petición al siguiente filtro/controlador.
 *
 * SecurityContextHolder es el mecanismo de Spring Security para propagar
 * la identidad del usuario autenticado a lo largo de toda la petición.
 * Es por-hilo (ThreadLocal), por lo que es seguro en entornos multihilo.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        // Si hay token y es válido, autenticamos al usuario en el contexto
        if (token != null && jwtProvider.isTokenValid(token)) {
            String username = jwtProvider.getUsernameFromToken(token);
            List<String> roles = jwtProvider.getRolesFromToken(token);

            // Convertimos los roles a SimpleGrantedAuthority (formato que entiende Spring Security)
            // El prefijo "ROLE_" es convenio de Spring Security para roles
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol))
                    .toList();

            // UsernamePasswordAuthenticationToken con 3 args = autenticación completada
            // (con 2 args = pendiente de autenticar, que no es nuestro caso)
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

            // Guardamos la autenticación en el contexto del hilo actual
            // A partir de aquí, @PreAuthorize y hasRole() funcionan correctamente
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // Siempre continuamos la cadena de filtros, aunque no haya token
        // Spring Security denegará el acceso en el siguiente paso si la ruta lo requiere
        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token JWT de la cabecera Authorization.
     * Devuelve null si la cabecera no existe o no tiene el prefijo "Bearer ".
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            // Eliminamos el prefijo "Bearer " para quedarnos solo con el token
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
