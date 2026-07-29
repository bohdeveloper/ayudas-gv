package com.ayudasgv.tramitacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Petición para registrar un trámite sobre un expediente.
 *
 * No incluye ni el usuario ni la fecha: los pone el servidor. El usuario sale del
 * JWT (nunca de lo que envíe el cliente — si no, cualquiera podría firmar trámites
 * en nombre de otro) y la fecha del reloj del servidor.
 */
@Schema(description = "Datos para registrar un nuevo trámite")
public record NuevoTramiteRequest(

    @Schema(description = "Código del tipo de trámite (catálogo tipo_tramite)",
        example = "REVISION", requiredMode = Schema.RequiredMode.REQUIRED,
        allowableValues = {"REGISTRO", "ASIGNACION", "REVISION", "PROPUESTA", "RESOLUCION", "NOTIFICACION"})
    @NotBlank(message = "{validation.tramite.tipo.required}")
    String tipoTramite,

    @Schema(description = "Resultado del trámite; obligatorio en PROPUESTA y RESOLUCION",
        example = "FAVORABLE", nullable = true)
    @Pattern(regexp = "FAVORABLE|DESFAVORABLE", message = "{validation.tramite.resultado.invalid}")
    String resultado,

    @Schema(description = "Observaciones del instructor (máx. 1000 caracteres)", nullable = true)
    @Size(max = 1000, message = "{validation.tramite.observaciones.maxlength}")
    String observaciones

) {}
