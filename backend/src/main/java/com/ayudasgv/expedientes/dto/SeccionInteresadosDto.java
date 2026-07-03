package com.ayudasgv.expedientes.dto;

import java.util.List;

/**
 * Sección "Interesados" del detalle de un expediente: quién solicita, quién
 * representa (opcional) y sobre quién recae la ayuda (causantes, si aplica).
 */
public record SeccionInteresadosDto(

    PersonaDto solicitante,

    PersonaDto representante,

    List<PersonaDto> causantes

) {}
