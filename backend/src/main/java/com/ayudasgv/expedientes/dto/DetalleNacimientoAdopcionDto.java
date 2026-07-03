package com.ayudasgv.expedientes.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Detalle de la ayuda PAGO_NAC: pago único por nacimiento o adopción. */
public record DetalleNacimientoAdopcionDto(

    String tipo,

    String supuesto,

    LocalDate fechaHecho,

    BigDecimal ingresosAnuales,

    Integer numeroHijosCargo

) implements DetalleAyudaDto {}
