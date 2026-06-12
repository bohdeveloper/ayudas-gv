package com.ayudasgv.commons.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
 */
@RestControllerAdvice(basePackages = "com.ayudasgv")
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Captura errores de credenciales incorrectas → 401 Unauthorized.
     * Se lanza desde AuthService cuando el usuario no existe o la contraseña no coincide.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(401, "Unauthorized", ex.getMessage()));
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
                .body(new ErrorResponse(400, "Bad Request", "Error de validación en los datos enviados",
                        java.time.Instant.now(), details));
    }

    /**
     * Fallback genérico para cualquier excepción no capturada → 500 Internal Server Error.
     * En producción NO se expone el mensaje original (podría revelar detalles internos).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Excepción no controlada: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500, "Internal Server Error", "Error interno del servidor"));
    }
}
