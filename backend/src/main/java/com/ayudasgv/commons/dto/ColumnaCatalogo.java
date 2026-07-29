package com.ayudasgv.commons.dto;

import java.util.Locale;

/**
 * Traduce el idioma de la petición (cabecera {@code Accept-Language}) al nombre de
 * la columna de descripción que hay que leer en las tablas de catálogo
 * ({@code tipo_ayuda}, {@code estado_expediente}, {@code tipo_tramite}...).
 *
 * Vive aquí porque lo necesitan todos los módulos que devuelven catálogos
 * (expedientes y tramitación) y porque concentra en un solo sitio la garantía de
 * seguridad: el valor devuelto sale SIEMPRE de esta lista cerrada de dos constantes,
 * nunca de texto que venga del cliente. Es lo que permite interpolarlo en el SQL
 * sin abrir una vía de inyección (invariante 1 de spec.md).
 */
public final class ColumnaCatalogo {

    public static final String ES = "descripcion_es";
    public static final String EU = "descripcion_eu";

    private ColumnaCatalogo() {
    }

    /** {@code descripcion_eu} si la petición viene en euskera; {@code descripcion_es} en cualquier otro caso. */
    public static String segunIdioma(Locale locale) {
        return locale != null && "eu".equalsIgnoreCase(locale.getLanguage()) ? EU : ES;
    }
}
