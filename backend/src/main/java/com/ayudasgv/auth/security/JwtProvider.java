package com.ayudasgv.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * Proveedor de JSON Web Tokens (JWT) para la autenticación sin estado (stateless).
 *
 * ¿Por qué JWT en lugar de sesiones HTTP?
 *   Las sesiones HTTP guardan el estado en el servidor (memoria o BD), lo que
 *   complica el escalado horizontal. JWT guarda todo en el token firmado que
 *   viaja en cada petición — el servidor solo necesita la clave para verificarlo.
 *
 * Algoritmo: HMAC-SHA256 (HS256) — firma simétrica.
 *   La misma clave secreta firma y verifica. Suficiente para una API interna.
 *   Si se necesitara verificación externa sin compartir la clave, se usaría RS256.
 *
 * Estructura del token generado:
 *   - subject: username del usuario autenticado
 *   - roles: lista de códigos de rol (claim personalizado)
 *   - idiomaPreferido: para que el frontend sepa el idioma al cargar
 *   - issuedAt: momento de emisión
 *   - expiration: momento de caducidad (issuedAt + expirationMs)
 *
 * Librería: jjwt 0.12.5 (io.jsonwebtoken)
 */
@Component
public class JwtProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtProvider.class);

    // Nombre del claim personalizado que almacena los roles dentro del token
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_IDIOMA = "idioma";

    private final SecretKey secretKey;
    private final long expirationMs;

    /**
     * Constructor: convierte el secreto textual de application.yml en una clave
     * criptográfica HMAC-SHA256 (mínimo 256 bits = 32 bytes).
     *
     * @Value inyecta los valores desde application.yml o variables de entorno.
     * En producción, JWT_SECRET debe ser una cadena de al menos 32 caracteres aleatoria.
     */
    public JwtProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        // Keys.hmacShaKeyFor valida que la clave tenga el tamaño mínimo para HS256 (256 bits)
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Genera un JWT firmado con los datos del usuario.
     *
     * El token es compacto (Base64URL sin espacios), listo para enviarse
     * en la cabecera HTTP: Authorization: Bearer <token>
     *
     * @param username        nombre de usuario que va en el subject del token
     * @param roles           lista de roles que se incluyen como claim
     * @param idiomaPreferido idioma del usuario para que el frontend lo recupere sin BD
     * @return token JWT firmado en formato compacto
     */
    public String generateToken(String username, List<String> roles, String idiomaPreferido) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)               // identifica al usuario
                .claim(CLAIM_ROLES, roles)       // roles como array JSON en el payload
                .claim(CLAIM_IDIOMA, idiomaPreferido)
                .issuedAt(now)                   // timestamp de emisión (iat)
                .expiration(expiry)              // timestamp de caducidad (exp)
                .signWith(secretKey)             // firma con HS256 (jjwt elige el algoritmo por el tipo de clave)
                .compact();                       // serializa a String Base64URL
    }

    /**
     * Extrae el username (subject) de un token válido.
     * Lanza JwtException si el token está expirado, malformado o con firma inválida.
     *
     * @param token JWT en formato compacto
     * @return username contenido en el subject
     */
    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extrae la lista de roles del claim personalizado del token.
     *
     * @param token JWT en formato compacto
     * @return lista de códigos de rol (puede estar vacía pero no null)
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Object roles = parseClaims(token).get(CLAIM_ROLES);
        if (roles instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }
        return List.of();
    }

    /**
     * Valida que el token sea sintácticamente correcto, tenga firma válida
     * y no haya expirado.
     *
     * @param token JWT a validar
     * @return true si el token es válido, false si es inválido o ha expirado
     */
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token); // lanza excepción si algo falla
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Logeamos a nivel DEBUG para no llenar los logs con intentos normales
            log.debug("Token JWT inválido: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Parsea el token y devuelve el cuerpo (Claims).
     * verifyWith garantiza que verificamos la firma antes de confiar en el contenido.
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)   // DEBE verificarse la firma antes de leer el payload
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
