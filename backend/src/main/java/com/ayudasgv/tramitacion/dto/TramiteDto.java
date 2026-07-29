package com.ayudasgv.tramitacion.dto;

import com.ayudasgv.commons.dto.CatalogoDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Un trámite ya registrado en el histórico de un expediente.
 *
 * Responde a las tres preguntas que exige el enunciado (§7.2 y §8) para cada paso
 * de la tramitación: <b>quién</b> lo hizo, <b>cuándo</b> y con qué <b>resultado</b>.
 */
@Schema(description = "Trámite registrado sobre un expediente")
public record TramiteDto(

    @Schema(description = "Id del trámite", example = "1")
    Long id,

    @Schema(description = "Tipo de trámite, con su descripción en el idioma de la petición")
    CatalogoDto tipoTramite,

    @Schema(description = "Fecha y hora en que se registró el trámite")
    LocalDateTime fecha,

    @Schema(description = "Usuario que realizó el trámite", example = "instructor1")
    String username,

    @Schema(description = "Nombre y apellidos del usuario que realizó el trámite", example = "Ane Etxeberria")
    String usuarioNombre,

    @Schema(description = "Resultado del trámite cuando aplica (FAVORABLE | DESFAVORABLE)",
        example = "FAVORABLE", nullable = true)
    String resultado,

    @Schema(description = "Observaciones libres del instructor", nullable = true)
    String observaciones

) {}
