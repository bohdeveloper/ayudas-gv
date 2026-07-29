package com.ayudasgv.commons.i18n;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guardia de los ficheros de mensajes: los tres bundles deben tener EXACTAMENTE
 * las mismas claves.
 *
 * <p>Nace de un fallo real detectado el 2026-07-29 probando la Fase 1 en el clúster:
 * se añadieron claves nuevas a {@code messages_es} y {@code messages_eu} pero no al
 * bundle raíz {@code messages.properties}. El síntoma es silencioso y solo aparece en
 * ejecución: cuando la petición llega sin {@code Accept-Language} (o con uno que no es
 * es/eu), Spring cae al bundle raíz, no encuentra la clave y el usuario recibe
 * {@code "tramite.resolucion.rol"} en vez de un mensaje. Ninguna prueba de servicio lo
 * detectaba, porque las excepciones llevan la clave, no el texto.
 *
 * <p>El bundle raíz no es opcional: sin él Spring Boot ni siquiera registra el
 * MessageSource (ver la cabecera de {@code messages.properties}).
 */
class MessagesParidadTest {

    private static final String RAIZ = "i18n/messages.properties";
    private static final String ES = "i18n/messages_es.properties";
    private static final String EU = "i18n/messages_eu.properties";

    private static Set<String> clavesDe(String recurso) throws IOException {
        var properties = new Properties();
        try (InputStream in = MessagesParidadTest.class.getClassLoader().getResourceAsStream(recurso)) {
            assertThat(in).as("no se encontró %s en el classpath", recurso).isNotNull();
            properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        return new TreeSet<>(properties.stringPropertyNames());
    }

    @Test
    void elBundleEnEuskeraTieneLasMismasClavesQueElCastellano() throws IOException {
        assertThat(clavesDe(EU))
                .as("claves que faltan o sobran en messages_eu.properties")
                .isEqualTo(clavesDe(ES));
    }

    @Test
    void elBundleRaizTieneLasMismasClavesQueElCastellano() throws IOException {
        // El raíz es el fallback para cualquier Accept-Language que no sea es/eu
        // (y para las peticiones que no mandan la cabecera): si le falta una clave,
        // el usuario ve el identificador técnico en vez del mensaje.
        assertThat(clavesDe(RAIZ))
                .as("claves que faltan o sobran en messages.properties (bundle raíz)")
                .isEqualTo(clavesDe(ES));
    }

    @Test
    void ningunMensajeQuedaVacio() throws IOException {
        for (String recurso : new String[]{RAIZ, ES, EU}) {
            var properties = new Properties();
            try (InputStream in = MessagesParidadTest.class.getClassLoader().getResourceAsStream(recurso)) {
                properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
            for (String clave : properties.stringPropertyNames()) {
                assertThat(properties.getProperty(clave).trim())
                        .as("%s → la clave '%s' no tiene texto", recurso, clave)
                        .isNotEmpty();
            }
        }
    }
}
