package com.ayudasgv.tramitacion.controller;

import com.ayudasgv.tramitacion.dto.InstructorDto;
import com.ayudasgv.tramitacion.service.TramitacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Lista de instructores asignables — lo que alimenta el desplegable de asignación
 * de la sección Tramitación.
 *
 * Va en el módulo {@code tramitacion} y no en uno de "usuarios" a propósito: no es
 * una gestión de usuarios (eso es competencia de ADMIN y llegará con su propio
 * módulo), es un dato que la tramitación necesita para hacer su trabajo. Por eso
 * está restringido a los mismos roles que pueden asignar.
 */
@RestController
@RequestMapping("/v1/instructores")
@Tag(name = "Tramitación", description = "Trámites de un expediente y asignación de instructor")
public class InstructorController {

    private final TramitacionService tramitacionService;

    public InstructorController(TramitacionService tramitacionService) {
        this.tramitacionService = tramitacionService;
    }

    @Operation(
        summary = "Instructores asignables",
        description = "Usuarios activos con rol INSTRUCTOR, ordenados por apellidos. "
                + "Reservado a los roles que pueden asignar expedientes (RESPONSABLE, ADMIN).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de instructores"),
        @ApiResponse(responseCode = "401", description = "Falta el token o no es válido"),
        @ApiResponse(responseCode = "403", description = "El rol no permite asignar instructores")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE', 'ADMIN')")
    public ResponseEntity<List<InstructorDto>> instructores() {
        return ResponseEntity.ok(tramitacionService.listarInstructoresAsignables());
    }
}
