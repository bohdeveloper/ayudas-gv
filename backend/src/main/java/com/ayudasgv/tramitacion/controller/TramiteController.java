package com.ayudasgv.tramitacion.controller;

import com.ayudasgv.commons.audit.Auditable;
import com.ayudasgv.tramitacion.dto.AsignacionInstructorRequest;
import com.ayudasgv.tramitacion.dto.NuevoTramiteRequest;
import com.ayudasgv.tramitacion.dto.SeccionTramitacionDto;
import com.ayudasgv.tramitacion.dto.TramiteDto;
import com.ayudasgv.tramitacion.service.TramitacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

/**
 * Tramitación de expedientes (Fase 1): consulta del histórico, registro de trámites
 * y asignación de instructor.
 *
 * <p>Las rutas cuelgan del expediente ({@code /v1/expedientes/{id}/...}) porque un
 * trámite no existe por sí solo: siempre pertenece a un expediente.
 *
 * <p><b>Doble capa de autorización</b>, y las dos hacen falta:
 * <ul>
 *   <li>{@code @PreAuthorize} filtra por ROL — "¿puedes hacer este tipo de operación?".
 *       Es barato y corta antes de tocar la base de datos.</li>
 *   <li>El servicio filtra por DATO — "¿puedes hacerla sobre ESTE expediente?". Un
 *       instructor tiene el rol para tramitar, pero solo lo que tiene asignado, y eso
 *       no se sabe sin mirar el expediente.</li>
 * </ul>
 * El frontend además oculta los botones que no correspondan, pero eso es comodidad,
 * no seguridad: la regla se valida siempre aquí (spec.md §3, invariante 4).
 */
@RestController
@RequestMapping("/v1/expedientes/{id}")
@Tag(name = "Tramitación", description = "Trámites de un expediente y asignación de instructor")
public class TramiteController {

    private final TramitacionService tramitacionService;

    public TramiteController(TramitacionService tramitacionService) {
        this.tramitacionService = tramitacionService;
    }

    @Operation(
        summary = "Sección de tramitación del expediente",
        description = "Estado actual, instructor asignado e histórico de trámites (del más reciente al "
                + "más antiguo), con las descripciones de catálogo traducidas según Accept-Language. "
                + "Solo lectura: accesible a cualquier usuario autenticado, incluido el rol CONSULTOR.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sección de tramitación (el histórico puede estar vacío)"),
        @ApiResponse(responseCode = "401", description = "Falta el token o no es válido"),
        @ApiResponse(responseCode = "404", description = "No existe un expediente con ese id")
    })
    @GetMapping("/tramites")
    @Auditable(accion = "READ", entidad = "TRAMITE")
    public ResponseEntity<SeccionTramitacionDto> tramites(
            @Parameter(description = "Id del expediente") @PathVariable Long id,
            Locale locale) {

        return ResponseEntity.ok(tramitacionService.obtenerTramitacion(id, locale));
    }

    @Operation(
        summary = "Registrar un trámite",
        description = "Registra un trámite sobre el expediente y mueve su estado en consecuencia "
                + "(REVISION → EN_REVISION, RESOLUCION → RESUELTO, etc.). El autor y la fecha los pone "
                + "el servidor a partir del JWT y de su reloj, nunca el cliente. "
                + "La asignación de instructor NO se hace por aquí: tiene su propio endpoint.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Trámite registrado"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "401", description = "Falta el token o no es válido"),
        @ApiResponse(responseCode = "403", description = "El rol no permite tramitar, o el instructor "
                + "no tiene asignado este expediente"),
        @ApiResponse(responseCode = "404", description = "No existe el expediente o el tipo de trámite"),
        @ApiResponse(responseCode = "409", description = "El estado del expediente no admite este trámite")
    })
    @PostMapping("/tramites")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'RESPONSABLE', 'ADMIN')")
    @Auditable(accion = "TRAMITE", entidad = "EXPEDIENTE")
    public ResponseEntity<TramiteDto> registrarTramite(
            @Parameter(description = "Id del expediente") @PathVariable Long id,
            @Valid @RequestBody NuevoTramiteRequest peticion,
            Authentication authentication,
            Locale locale) {

        TramiteDto creado = tramitacionService.registrarTramite(
                id, peticion, authentication.getName(), rolesDe(authentication), locale);

        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @Operation(
        summary = "Asignar instructor al expediente",
        description = "Asigna el expediente a un usuario con rol INSTRUCTOR, pasa el estado a ASIGNADO "
                + "y deja el trámite ASIGNACION en el histórico — las tres cosas en la misma transacción. "
                + "Operación reservada al rol RESPONSABLE (y ADMIN).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Expediente asignado; devuelve la sección actualizada"),
        @ApiResponse(responseCode = "400", description = "Falta el id del instructor"),
        @ApiResponse(responseCode = "401", description = "Falta el token o no es válido"),
        @ApiResponse(responseCode = "403", description = "El rol no permite asignar instructores"),
        @ApiResponse(responseCode = "404", description = "No existe un expediente con ese id"),
        @ApiResponse(responseCode = "409", description = "El usuario indicado no es un instructor activo")
    })
    @PutMapping("/instructor")
    @PreAuthorize("hasAnyRole('RESPONSABLE', 'ADMIN')")
    @Auditable(accion = "UPDATE", entidad = "EXPEDIENTE")
    public ResponseEntity<SeccionTramitacionDto> asignarInstructor(
            @Parameter(description = "Id del expediente") @PathVariable Long id,
            @Valid @RequestBody AsignacionInstructorRequest peticion,
            Authentication authentication,
            Locale locale) {

        return ResponseEntity.ok(
                tramitacionService.asignarInstructor(id, peticion, authentication.getName(), locale));
    }

    /**
     * Extrae los códigos de rol del token, quitando el prefijo {@code ROLE_} que
     * {@code JwtAuthenticationFilter} añade por convenio de Spring Security. El
     * servicio razona en términos de negocio ("INSTRUCTOR"), no del convenio del framework.
     */
    private static List<String> rolesDe(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(rol -> rol.startsWith("ROLE_") ? rol.substring("ROLE_".length()) : rol)
                .toList();
    }
}
