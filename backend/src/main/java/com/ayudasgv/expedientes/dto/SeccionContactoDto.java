package com.ayudasgv.expedientes.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Sección "Contacto y aviso" del detalle de un expediente.
 */
public record SeccionContactoDto(

    String via,

    String numero,

    String piso,

    String codigoPostal,

    String municipio,

    String provincia,

    String telefono,

    String email,

    @Schema(description = "EMAIL | SMS | POSTAL")
    String canalAviso,

    @Schema(description = "Idioma de las notificaciones (es/eu), independiente del idioma de interfaz")
    String idiomaNotificacion,

    boolean aceptaNotifTelematica

) {}
