package com.ayudasgv.expedientes.controller;

import com.ayudasgv.expedientes.dto.ExpedienteResumenDto;
import com.ayudasgv.expedientes.dto.PageResponseDto;
import com.ayudasgv.expedientes.dto.SeccionAyudaDto;
import com.ayudasgv.expedientes.dto.SeccionContactoDto;
import com.ayudasgv.expedientes.dto.SeccionGeneralesDto;
import com.ayudasgv.expedientes.dto.SeccionInteresadosDto;
import com.ayudasgv.expedientes.service.ExpedienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Locale;

/**
 * Búsqueda de expedientes con filtros, paginación y ordenación.
 *
 * Requiere autenticación (cualquier rol) — la ruta no está en PUBLIC_PATHS de
 * {@code SecurityConfig}, así que Spring Security exige un JWT válido.
 *
 * La descripción de los catálogos (tipo de ayuda, estado) se devuelve en el
 * idioma indicado por la cabecera {@code Accept-Language} (es/eu), resuelto
 * automáticamente por Spring en el parámetro {@code Locale}.
 */
@RestController
@RequestMapping("/v1/expedientes")
@Tag(name = "Expedientes", description = "Búsqueda y consulta de expedientes")
public class ExpedienteController {

    private final ExpedienteService expedienteService;

    public ExpedienteController(ExpedienteService expedienteService) {
        this.expedienteService = expedienteService;
    }

    @Operation(
        summary = "Buscar expedientes",
        description = "Devuelve una página de expedientes que cumplen los filtros indicados. "
                + "Las descripciones de catálogo (tipo de ayuda, estado) se traducen según Accept-Language.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    public ResponseEntity<PageResponseDto<ExpedienteResumenDto>> buscar(
            @Parameter(description = "Número de expediente (coincidencia parcial)", example = "EXP-2024")
            @RequestParam(required = false) String numeroExpediente,

            @Parameter(description = "Código de tipo de ayuda", example = "ASIG_DISCAP")
            @RequestParam(required = false) String tipoAyuda,

            @Parameter(description = "Código de estado del expediente", example = "REGISTRADO")
            @RequestParam(required = false) String estado,

            @Parameter(description = "Documento del solicitante (coincidencia parcial)", example = "12345678A")
            @RequestParam(required = false) String documentoSolicitante,

            @Parameter(description = "Fecha de registro desde (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,

            @Parameter(description = "Fecha de registro hasta (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,

            @Parameter(description = "Página solicitada (0-based)", example = "0")
            @RequestParam(required = false) Integer page,

            @Parameter(description = "Tamaño de página (máx. 100)", example = "10")
            @RequestParam(required = false) Integer size,

            @Parameter(description = "Campo y dirección de orden: 'campo,asc' o 'campo,desc'. "
                    + "Campos admitidos: numeroExpediente, fechaRegistro, estado, tipoAyuda.",
                    example = "fechaRegistro,desc")
            @RequestParam(required = false) String sort,

            Locale locale) {

        PageResponseDto<ExpedienteResumenDto> resultado = expedienteService.buscar(
                numeroExpediente, tipoAyuda, estado, documentoSolicitante,
                fechaDesde, fechaHasta, page, size, sort, locale
        );

        return ResponseEntity.ok(resultado);
    }

    @Operation(
        summary = "Datos generales del expediente",
        description = "Sección de cabecera del detalle: número, tipo de ayuda, estado, fechas e instructor. "
                + "404 si no existe un expediente con ese id.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{id}/generales")
    public ResponseEntity<SeccionGeneralesDto> generales(
            @Parameter(description = "Id del expediente") @PathVariable Long id,
            Locale locale) {
        return ResponseEntity.ok(expedienteService.obtenerGenerales(id, locale));
    }

    @Operation(
        summary = "Interesados del expediente",
        description = "Solicitante, representante (si lo hay) y causantes vinculados al expediente. "
                + "404 si no existe un expediente con ese id.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{id}/interesados")
    public ResponseEntity<SeccionInteresadosDto> interesados(
            @Parameter(description = "Id del expediente") @PathVariable Long id) {
        return ResponseEntity.ok(expedienteService.obtenerInteresados(id));
    }

    @Operation(
        summary = "Contacto y aviso del expediente",
        description = "Dirección postal, canal e idioma de notificación. "
                + "404 si el expediente no existe o no tiene datos de contacto registrados.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{id}/contacto")
    public ResponseEntity<SeccionContactoDto> contacto(
            @Parameter(description = "Id del expediente") @PathVariable Long id) {
        return ResponseEntity.ok(expedienteService.obtenerContacto(id));
    }

    @Operation(
        summary = "Datos de la ayuda solicitada",
        description = "Fecha e importe solicitados, observaciones y detalle específico según el tipo de ayuda "
                + "(ASIG_DISCAP / PAGO_NAC / PARTO_MULT). 404 si el expediente no existe o no tiene solicitud registrada.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{id}/ayuda")
    public ResponseEntity<SeccionAyudaDto> ayuda(
            @Parameter(description = "Id del expediente") @PathVariable Long id) {
        return ResponseEntity.ok(expedienteService.obtenerAyuda(id));
    }
}
