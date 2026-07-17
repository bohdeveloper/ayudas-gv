package com.ayudasgv.commons.exception;

import com.ayudasgv.commons.logging.CorrelationIdFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.MDC;

import java.time.Instant;
import java.util.List;

/**
 * DTO de respuesta de error homogéneo para toda la API.
 *
 * Cuando ocurre un error (401, 403, 400, 500...), el cliente recibe siempre
 * la misma estructura JSON en lugar de la página HTML de error de Spring o
 * mensajes de excepción con stack traces expuestos.
 *
 * @JsonInclude(NON_NULL): los campos null no se incluyen en el JSON,
 * lo que mantiene la respuesta limpia cuando no hay detalles adicionales.
 *
 * "correlationId" es el mismo identificador que la cabecera de respuesta
 * "X-Correlation-Id" (ver {@code CorrelationIdFilter}): permite localizar en los
 * logs del servidor exactamente la petición que produjo este error.
 *
 * Ejemplo de respuesta 401:
 * {
 *   "status": 401,
 *   "error": "Unauthorized",
 *   "message": "Credenciales incorrectas",
 *   "timestamp": "2026-06-12T10:30:00Z",
 *   "correlationId": "3f1b1e2a-2e34-4b8a-9b0a-7e2b9a6f2a11"
 * }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    int status,
    String error,
    String message,
    Instant timestamp,
    List<String> details,  // para errores de validación: lista de campos inválidos
    String correlationId
) {
    // Constructor compacto para errores simples sin detalles de campo
    public ErrorResponse(int status, String error, String message) {
        this(status, error, message, Instant.now(), null, MDC.get(CorrelationIdFilter.MDC_KEY));
    }
}
