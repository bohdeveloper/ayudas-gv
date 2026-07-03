package com.ayudasgv.expedientes.dto;

import java.time.LocalDate;

/** Detalle de la ayuda PARTO_MULT: pago único por parto o adopción múltiples. */
public record DetallePartoMultipleDto(

    String tipo,

    Integer numeroNacidos,

    LocalDate fechaHecho

) implements DetalleAyudaDto {}
