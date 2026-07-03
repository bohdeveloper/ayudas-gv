package com.ayudasgv.commons.exception;

/**
 * Se lanza cuando se solicita un recurso (expediente, sección de detalle...) que
 * no existe en la base de datos → el {@code GlobalExceptionHandler} la traduce a 404.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String message) {
        super(message);
    }
}
