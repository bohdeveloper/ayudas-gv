package com.ayudasgv.commons.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Asigna un identificador de correlación a cada petición y lo propaga en dos sitios:
 *   1. MDC ("correlationId") — aparece en todas las líneas de log de la petición
 *      gracias al patrón "%X{correlationId}" configurado en application.yml.
 *   2. Cabecera de respuesta "X-Correlation-Id" — permite que el cliente (o soporte)
 *      correlacione una respuesta concreta con las líneas de log del servidor.
 *
 * Si el cliente ya envía "X-Correlation-Id" (por ejemplo, un proxy o gateway
 * delante de la API) se reutiliza en lugar de generar uno nuevo, para que la
 * traza sea consistente de extremo a extremo.
 *
 * @Order(HIGHEST_PRECEDENCE): debe ejecutarse antes que la cadena de Spring
 * Security, así los logs de JwtAuthenticationFilter también quedan correlacionados.
 * MDC es ThreadLocal, así que se limpia siempre en el finally para no filtrar
 * el valor a la siguiente petición que reutilice el mismo hilo del pool.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String HEADER_NAME = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String correlationId = request.getHeader(HEADER_NAME);
        if (!StringUtils.hasText(correlationId)) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put(MDC_KEY, correlationId);
        response.setHeader(HEADER_NAME, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
