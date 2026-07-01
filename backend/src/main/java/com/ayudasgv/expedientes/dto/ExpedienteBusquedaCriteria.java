package com.ayudasgv.expedientes.dto;

import java.time.LocalDate;

/**
 * Agrupa los filtros de búsqueda de expedientes que llegan como query params
 * en {@code GET /api/v1/expedientes}. Objeto interno — no se expone tal cual en Swagger,
 * el controlador documenta cada parámetro por separado.
 *
 * Todos los campos de filtro son opcionales (null = sin filtrar por ese campo).
 */
public record ExpedienteBusquedaCriteria(
    String numeroExpediente,
    String tipoAyuda,
    String estado,
    String documentoSolicitante,
    LocalDate fechaDesde,
    LocalDate fechaHasta,
    int page,
    int size,
    String sort
) {}
