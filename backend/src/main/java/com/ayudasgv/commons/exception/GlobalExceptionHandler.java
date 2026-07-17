package com.ayudasgv.commons.exception;

import com.ayudasgv.commons.logging.CorrelationIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Manejador global de excepciones para toda la API REST.
 *
 * basePackages restringe el advice a nuestros propios controladores.
 * Sin esta restricción, el handler genérico interceptaría también las excepciones
 * de librerías de terceros (SpringDoc, etc.) ocultando su causa real en los logs.
 *
 * Todos los mensajes que devuelve al cliente se resuelven con {@link MessageSource}
 * contra {@code messages_es/eu.properties} según el idioma de la petición
 * (cabecera Accept-Language, vía {@link LocaleContextHolder}) — nunca texto en
 * español embebido directamente en el código (sección 8.4 del enunciado).
 */
@RestControllerAdvice(basePackages = "com.ayudasgv")
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /** Resuelve una clave de {@code messages_es/eu.properties} en el idioma de la petición actual. */
    private String resolve(String key, Object... args) {
        return messageSource.getMessage(key, args, key, LocaleContextHolder.getLocale());
    }

    /**
     * Captura errores de credenciales incorrectas → 401 Unauthorized.
     * Se lanza desde AuthService cuando el usuario no existe o la contraseña no coincide.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(401, "Unauthorized", resolve(ex.getMessage())));
    }

    /**
     * Captura la ausencia de un recurso solicitado (expediente, sección de detalle...) → 404 Not Found.
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, "Not Found", resolve(ex.getMessageKey(), ex.getArgs())));
    }

    /**
     * Captura errores de validación de @NotBlank, @NotNull, etc. → 400 Bad Request.
     *
     * MethodArgumentNotValidException se lanza cuando @Valid falla en el controlador.
     * Extraemos los mensajes de cada campo inválido para devolvérselos al cliente.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(400, "Bad Request", resolve("error.validation"),
                        java.time.Instant.now(), details, MDC.get(CorrelationIdFilter.MDC_KEY)));
    }

    /**
     * Captura el acceso a un recurso para el que el usuario autenticado no tiene
     * el rol requerido (p.ej. {@code @PreAuthorize}) → 403 Forbidden.
     *
     * Se distingue de 401: aquí el usuario SÍ está autenticado, pero su rol no
     * le permite esta operación concreta.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(403, "Forbidden", resolve("auth.access.denied")));
    }

    /**
     * Fallback genérico para cualquier excepción no capturada → 500 Internal Server Error.
     * En producción NO se expone el mensaje original (podría revelar detalles internos).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Excepción no controlada: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500, "Internal Server Error", resolve("error.internal")));
    }
}
