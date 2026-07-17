package com.ayudasgv.commons.audit;

import com.ayudasgv.auth.dto.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Aspecto transversal (AOP) que registra en la tabla {@code auditoria} cada
 * llamada exitosa a un método anotado con {@link Auditable}.
 *
 * Usa {@code @AfterReturning} en vez de {@code @Around}: solo interesa auditar
 * lo que realmente ocurrió (login válido, consulta que devolvió datos), nunca
 * los intentos fallidos — esos ya quedan como WARN en los logs de cada servicio.
 *
 * Un fallo al escribir la auditoría NUNCA debe tumbar la petición del usuario:
 * se captura y se loguea como ERROR, pero la respuesta ya se envió igualmente.
 */
@Aspect
@Component
public class AuditoriaAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaAspect.class);

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaAspect(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @AfterReturning(pointcut = "@annotation(auditable)", returning = "resultado")
    public void auditar(JoinPoint joinPoint, Auditable auditable, Object resultado) {
        try {
            String username = resolverUsername(joinPoint.getArgs());
            String entidadId = resolverEntidadId(joinPoint.getArgs());

            auditoriaRepository.registrar(
                    username,
                    auditable.accion(),
                    auditable.entidad(),
                    entidadId,
                    joinPoint.getSignature().toShortString(),
                    resolverIpOrigen()
            );
        } catch (Exception ex) {
            // La auditoría es un efecto secundario: si falla, no debe afectar a la respuesta ya generada.
            log.error("No se pudo registrar la auditoría de {}: {}", joinPoint.getSignature(), ex.getMessage(), ex);
        }
    }

    /**
     * El login todavía no ha poblado el SecurityContext (el JWT se genera en esta
     * misma llamada), así que el username se lee del propio LoginRequest. El resto
     * de acciones ya pasan por JwtAuthenticationFilter y sí tienen SecurityContext.
     */
    private String resolverUsername(Object[] args) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        for (Object arg : args) {
            if (arg instanceof LoginRequest loginRequest) {
                return loginRequest.username();
            }
        }
        return null;
    }

    /** Busca el primer argumento Long del método anotado (el id de la ruta, p.ej. {id} de expediente). */
    private String resolverEntidadId(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof Long id) {
                return String.valueOf(id);
            }
        }
        return null;
    }

    private String resolverIpOrigen() {
        var attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        return request.getRemoteAddr();
    }
}
