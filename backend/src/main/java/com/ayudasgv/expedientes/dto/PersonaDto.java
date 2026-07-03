package com.ayudasgv.expedientes.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Persona física vinculada a un expediente (solicitante, representante o causante).
 */
public record PersonaDto(

    Long id,

    @Schema(example = "DNI")
    String tipoDocumento,

    @Schema(example = "12345678A")
    String numeroDocumento,

    String nombre,

    String apellido1,

    String apellido2,

    LocalDate fechaNacimiento

) {}
