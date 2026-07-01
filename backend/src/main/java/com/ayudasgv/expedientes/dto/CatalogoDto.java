package com.ayudasgv.expedientes.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Representa un valor de catálogo (tipo de ayuda, estado, ...) con su descripción
 * ya resuelta en el idioma de la petición (ver {@code Accept-Language}).
 */
public record CatalogoDto(

    @Schema(description = "Código interno del valor de catálogo", example = "ASIG_DISCAP")
    String codigo,

    @Schema(description = "Descripción del valor de catálogo en el idioma de la petición",
        example = "Asignación económica por hijo o menor a cargo con discapacidad")
    String descripcion

) {}
