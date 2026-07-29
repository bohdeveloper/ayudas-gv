package com.ayudasgv.commons.exception;

/**
 * Se lanza cuando el usuario está correctamente autenticado y tiene el rol necesario,
 * pero una regla de negocio le impide esta operación concreta sobre este recurso
 * concreto → el {@code GlobalExceptionHandler} la traduce a 403 Forbidden.
 *
 * Caso de uso de la Fase 1 (invariante 4 de spec.md): un usuario con rol INSTRUCTOR
 * puede tramitar — pero solo los expedientes que tiene asignados. El rol lo autoriza
 * a la operación; la asignación lo autoriza a este expediente. Por eso no basta con
 * {@code @PreAuthorize}: la comprobación necesita mirar el dato.
 *
 * Igual que {@link RecursoNoEncontradoException}, no lleva el mensaje ya resuelto sino
 * la clave de {@code messages_es/eu.properties}, para que el handler la traduzca al
 * idioma de la petición (Accept-Language).
 */
public class OperacionNoPermitidaException extends RuntimeException {

    private final Object[] args;

    public OperacionNoPermitidaException(String messageKey, Object... args) {
        super(messageKey);
        this.args = args;
    }

    public String getMessageKey() {
        return getMessage();
    }

    public Object[] getArgs() {
        return args;
    }
}
