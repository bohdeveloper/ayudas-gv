package com.ayudasgv.tramitacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Un instructor al que se puede asignar un expediente.
 *
 * Es deliberadamente pobre: id, username y nombre. Nada de email, roles ni fechas —
 * quien asigna un expediente no necesita el perfil completo de la persona, y todo
 * dato que no se envía es un dato que no se puede filtrar (spec.md §3, invariante 9).
 */
@Schema(description = "Usuario con rol INSTRUCTOR al que se puede asignar un expediente")
public record InstructorDto(

    @Schema(description = "Id del usuario", example = "2")
    Long id,

    @Schema(description = "Nombre de usuario", example = "instructor1")
    String username,

    @Schema(description = "Nombre y apellidos", example = "Ane Etxeberria")
    String nombreCompleto

) {}
