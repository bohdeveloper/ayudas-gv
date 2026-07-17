package com.ayudasgv.commons.exception;

/**
 * Se lanza cuando se solicita un recurso (expediente, sección de detalle...) que
 * no existe en la base de datos → el {@code GlobalExceptionHandler} la traduce a 404.
 *
 * No lleva el mensaje ya resuelto: lleva la clave de {@code messages_es/eu.properties}
 * y sus argumentos, para que el handler la resuelva en el idioma de la petición
 * (Accept-Language) a través de {@code MessageSource}.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    private final Object[] args;

    public RecursoNoEncontradoException(String messageKey, Object... args) {
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
