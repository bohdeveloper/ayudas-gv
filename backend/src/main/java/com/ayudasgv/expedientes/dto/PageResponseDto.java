package com.ayudasgv.expedientes.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Envoltorio de paginación genérico para listados de la API.
 *
 * Se define a mano (en vez de devolver directamente un {@code org.springframework.data.domain.Page})
 * porque el proyecto no usa Spring Data — el acceso a datos es JDBC explícito — y así
 * mantenemos el mismo formato de respuesta sin acoplar la API a esa dependencia.
 */
public record PageResponseDto<T>(

    List<T> content,

    @Schema(description = "Número total de elementos que cumplen los filtros (todas las páginas)")
    long totalElements,

    @Schema(description = "Número total de páginas")
    int totalPages,

    @Schema(description = "Página actual (0-based)")
    int number,

    @Schema(description = "Tamaño de página solicitado")
    int size

) {
    public static <T> PageResponseDto<T> of(List<T> content, long totalElements, int page, int size) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new PageResponseDto<>(content, totalElements, totalPages, page, size);
    }
}
