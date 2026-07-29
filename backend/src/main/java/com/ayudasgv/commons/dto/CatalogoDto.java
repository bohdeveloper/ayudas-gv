package com.ayudasgv.commons.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Representa un valor de catálogo (tipo de ayuda, estado, tipo de trámite...) con su
 * descripción ya resuelta en el idioma de la petición (ver {@code Accept-Language}).
 *
 * Vive en {@code commons} y no en un módulo concreto porque lo usan varios contextos
 * (expedientes y tramitación). Así {@code tramitacion} no tiene que depender del
 * paquete de DTOs de {@code expedientes} — la frontera entre módulos se mantiene
 * limpia de cara a extraer microservicios (spec.md §2, invariante 6): en ese momento
 * {@code commons} se convierte en la librería compartida entre servicios.
 */
public record CatalogoDto(

    @Schema(description = "Código interno del valor de catálogo", example = "ASIG_DISCAP")
    String codigo,

    @Schema(description = "Descripción del valor de catálogo en el idioma de la petición",
        example = "Asignación económica por hijo o menor a cargo con discapacidad")
    String descripcion

) {}
