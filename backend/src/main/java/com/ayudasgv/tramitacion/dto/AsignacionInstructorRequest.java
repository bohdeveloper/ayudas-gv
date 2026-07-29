package com.ayudasgv.tramitacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Petición para asignar un instructor a un expediente (operación de rol RESPONSABLE).
 *
 * Identifica al instructor por su id de usuario, no por su nombre: el nombre no es
 * único ni estable, el id sí.
 */
@Schema(description = "Instructor al que se asigna el expediente")
public record AsignacionInstructorRequest(

    @Schema(description = "Id del usuario con rol INSTRUCTOR al que se asigna el expediente",
        example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{validation.instructor.required}")
    Long instructorId

) {}
