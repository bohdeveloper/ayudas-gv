package com.ayudasgv.expedientes.dto;

/**
 * Detalle de la solicitud de ayuda, distinto según el tipo de ayuda del expediente.
 * Cada implementación lleva su propio campo {@code tipo} como discriminante, igual
 * que el tipo unión {@code DetalleAyuda} en el frontend (frontend/src/types/index.ts).
 */
public sealed interface DetalleAyudaDto
    permits DetalleAsignacionDiscapacidadDto, DetalleNacimientoAdopcionDto, DetallePartoMultipleDto {
}
