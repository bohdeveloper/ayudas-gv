package com.ayudasgv.tramitacion.dto;

import com.ayudasgv.commons.dto.CatalogoDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Sección "Tramitación" del detalle del expediente: en qué punto está, quién lo
 * lleva y todo lo que se ha hecho hasta ahora.
 *
 * Sigue el mismo patrón que las cuatro secciones de la Fase 0 (generales,
 * interesados, contacto, ayuda): una llamada independiente que devuelve todo lo
 * que necesita su pestaña, sin obligar al frontend a componer varias respuestas.
 */
@Schema(description = "Sección de tramitación: estado, instructor asignado e histórico de trámites")
public record SeccionTramitacionDto(

    @Schema(description = "Estado actual del expediente, con su descripción en el idioma de la petición")
    CatalogoDto estado,

    @Schema(description = "Id del usuario instructor asignado; null si todavía no se ha asignado",
        example = "2", nullable = true)
    Long instructorId,

    @Schema(description = "Nombre y apellidos del instructor asignado; null si todavía no se ha asignado",
        example = "Ane Etxeberria", nullable = true)
    String instructorNombre,

    @Schema(description = "Histórico de trámites, del más reciente al más antiguo")
    List<TramiteDto> tramites

) {}
