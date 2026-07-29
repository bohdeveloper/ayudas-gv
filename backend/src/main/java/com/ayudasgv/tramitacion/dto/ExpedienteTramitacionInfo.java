package com.ayudasgv.tramitacion.dto;

import com.ayudasgv.commons.dto.CatalogoDto;

/**
 * Vista mínima del expediente que necesita la capa de negocio para decidir si un
 * trámite es válido: en qué estado está y a quién está asignado.
 *
 * No se expone en la API — es un objeto interno entre repositorio y servicio. Se
 * consulta de una sola vez para no hacer tres viajes a la BD (¿existe? ¿en qué
 * estado? ¿de quién es?) por cada trámite que se registra.
 */
public record ExpedienteTramitacionInfo(

    Long expedienteId,
    CatalogoDto estado,
    Long instructorId,
    String instructorNombre

) {}
