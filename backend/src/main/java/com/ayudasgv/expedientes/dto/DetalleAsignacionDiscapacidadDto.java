package com.ayudasgv.expedientes.dto;

/** Detalle de la ayuda ASIG_DISCAP: asignación por hijo o menor a cargo con discapacidad. */
public record DetalleAsignacionDiscapacidadDto(

    String tipo,

    int gradoDiscapacidad,

    boolean necesitaTerceraPersona,

    PersonaDto causante

) implements DetalleAyudaDto {}
