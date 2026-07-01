package com.ayudasgv.expedientes.service;

import com.ayudasgv.expedientes.dto.ExpedienteBusquedaCriteria;
import com.ayudasgv.expedientes.dto.ExpedienteResumenDto;
import com.ayudasgv.expedientes.dto.PageResponseDto;
import com.ayudasgv.expedientes.repository.ExpedienteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * Lógica de negocio de la búsqueda de expedientes.
 *
 * Se encarga de:
 *   - Normalizar page/size (valores por defecto y límites razonables).
 *   - Resolver qué columna de descripción de catálogo usar según el idioma
 *     de la petición (cabecera Accept-Language).
 *   - Orquestar la consulta de datos y el conteo total para la paginación.
 */
@Service
public class ExpedienteService {

    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;

    private final ExpedienteRepository expedienteRepository;

    public ExpedienteService(ExpedienteRepository expedienteRepository) {
        this.expedienteRepository = expedienteRepository;
    }

    public PageResponseDto<ExpedienteResumenDto> buscar(
            String numeroExpediente,
            String tipoAyuda,
            String estado,
            String documentoSolicitante,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            Integer page,
            Integer size,
            String sort,
            Locale locale) {

        int normalizedPage = (page == null || page < 0) ? 0 : page;
        int normalizedSize = (size == null || size <= 0) ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);

        var criteria = new ExpedienteBusquedaCriteria(
                numeroExpediente, tipoAyuda, estado, documentoSolicitante,
                fechaDesde, fechaHasta, normalizedPage, normalizedSize, sort
        );

        String descColumn = "eu".equalsIgnoreCase(locale.getLanguage()) ? "descripcion_eu" : "descripcion_es";

        List<ExpedienteResumenDto> content = expedienteRepository.buscar(criteria, descColumn);
        long total = expedienteRepository.contar(criteria);

        return PageResponseDto.of(content, total, normalizedPage, normalizedSize);
    }
}
