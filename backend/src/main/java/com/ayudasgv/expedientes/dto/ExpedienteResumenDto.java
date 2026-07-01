package com.ayudasgv.expedientes.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Fila de resultado de la búsqueda de expedientes (listado paginado).
 * No incluye las secciones de detalle — esas se cargan en la Semana 5
 * al abrir un expediente concreto.
 */
public record ExpedienteResumenDto(

    Long id,

    @Schema(example = "EXP-2024-001")
    String numeroExpediente,

    CatalogoDto tipoAyuda,

    CatalogoDto estado,

    LocalDateTime fechaRegistro,

    @Schema(description = "Nombre completo del instructor asignado, o null si aún no se ha asignado")
    String instructorNombre

) {}
