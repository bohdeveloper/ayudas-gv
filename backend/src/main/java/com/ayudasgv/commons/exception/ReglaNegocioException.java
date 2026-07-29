package com.ayudasgv.commons.exception;

/**
 * Se lanza cuando la petición es válida en forma (sintaxis, tipos, permisos) pero
 * choca con el estado actual del recurso → el {@code GlobalExceptionHandler} la
 * traduce a 409 Conflict.
 *
 * Se distingue del 400 (Bad Request) a propósito: en un 400 el cliente envió algo mal
 * y debe corregirlo; en un 409 lo que envió está bien, pero el recurso no está en un
 * estado que lo admita. Caso de la Fase 1: registrar un trámite sobre un expediente
 * ya RESUELTO — la petición es correcta, el expediente es el que está cerrado.
 */
public class ReglaNegocioException extends RuntimeException {

    private final Object[] args;

    public ReglaNegocioException(String messageKey, Object... args) {
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
