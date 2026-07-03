package com.ayudasgv.expedientes.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Sección "Datos de la ayuda" del detalle de un expediente. El contenido de
 * {@code detalle} depende del tipo de ayuda del expediente (ver {@link DetalleAyudaDto}).
 */
public record SeccionAyudaDto(

    LocalDate fechaSolicitud,

    BigDecimal importeSolicitado,

    String observaciones,

    DetalleAyudaDto detalle

) {}
