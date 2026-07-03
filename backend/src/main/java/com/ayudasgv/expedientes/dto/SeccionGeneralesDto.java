package com.ayudasgv.expedientes.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Sección "Datos generales" del detalle de un expediente.
 */
public record SeccionGeneralesDto(

    Long id,

    @Schema(example = "EXP-2024-001")
    String numeroExpediente,

    CatalogoDto tipoAyuda,

    CatalogoDto estado,

    LocalDateTime fechaRegistro,

    @Schema(description = "Fecha de resolución, o null si el expediente aún no se ha resuelto")
    LocalDate fechaResolucion,

    @Schema(description = "Nombre completo del instructor asignado, o null si aún no se ha asignado")
    String instructorNombre

) {}
