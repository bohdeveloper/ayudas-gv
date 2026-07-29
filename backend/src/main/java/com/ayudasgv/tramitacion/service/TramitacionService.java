package com.ayudasgv.tramitacion.service;

import com.ayudasgv.commons.dto.ColumnaCatalogo;
import com.ayudasgv.commons.exception.OperacionNoPermitidaException;
import com.ayudasgv.commons.exception.RecursoNoEncontradoException;
import com.ayudasgv.commons.exception.ReglaNegocioException;
import com.ayudasgv.tramitacion.dto.AsignacionInstructorRequest;
import com.ayudasgv.tramitacion.dto.ExpedienteTramitacionInfo;
import com.ayudasgv.tramitacion.dto.InstructorDto;
import com.ayudasgv.tramitacion.dto.NuevoTramiteRequest;
import com.ayudasgv.tramitacion.dto.SeccionTramitacionDto;
import com.ayudasgv.tramitacion.dto.TramiteDto;
import com.ayudasgv.tramitacion.repository.TramiteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Lógica de negocio de la tramitación de expedientes (Fase 1).
 *
 * Aquí viven TODAS las reglas; el controlador solo traduce HTTP. Esto es
 * deliberado: la regla "un instructor solo puede tramitar lo suyo" es un
 * invariante del sistema (spec.md §3, invariante 4) y debe cumplirse venga la
 * llamada de donde venga — del REST de hoy o del microservicio de mañana.
 *
 * <h3>Modelo de tramitación</h3>
 * Cada trámite deja traza y, salvo la notificación, mueve el estado del expediente:
 * <pre>
 *   REGISTRO      → REGISTRADO
 *   ASIGNACION    → ASIGNADO      (no por este servicio: se registra al asignar instructor)
 *   REVISION      → EN_REVISION
 *   PROPUESTA     → EN_REVISION   (sigue en revisión hasta que se resuelve)
 *   RESOLUCION    → RESUELTO      (+ sella fecha_resolucion)
 *   NOTIFICACION  → sin cambio de estado (solo comunica lo ya resuelto)
 * </pre>
 *
 * <h3>Quién puede hacer qué</h3>
 * <ul>
 *   <li>Asignar instructor y resolver: RESPONSABLE (o ADMIN).</li>
 *   <li>Tramitar: INSTRUCTOR, <b>pero solo los expedientes asignados a él</b>.
 *       Un RESPONSABLE/ADMIN no tiene esa restricción.</li>
 *   <li>Consultar la sección: cualquier usuario autenticado (también CONSULTOR).</li>
 * </ul>
 */
@Service
public class TramitacionService {

    private static final Logger log = LoggerFactory.getLogger(TramitacionService.class);

    private static final String ROL_INSTRUCTOR = "INSTRUCTOR";
    private static final String ROL_RESPONSABLE = "RESPONSABLE";
    private static final String ROL_ADMIN = "ADMIN";

    private static final String TIPO_ASIGNACION = "ASIGNACION";
    private static final String TIPO_PROPUESTA = "PROPUESTA";
    private static final String TIPO_RESOLUCION = "RESOLUCION";
    private static final String TIPO_NOTIFICACION = "NOTIFICACION";

    private static final String ESTADO_ASIGNADO = "ASIGNADO";
    private static final String ESTADO_RESUELTO = "RESUELTO";

    /** Estado al que pasa el expediente tras cada tipo de trámite (los ausentes no lo cambian). */
    private static final Map<String, String> ESTADO_TRAS_TRAMITE = Map.of(
            "REGISTRO", "REGISTRADO",
            "REVISION", "EN_REVISION",
            TIPO_PROPUESTA, "EN_REVISION",
            TIPO_RESOLUCION, ESTADO_RESUELTO
    );

    /** Trámites que exigen que el expediente ya tenga instructor asignado. */
    private static final Set<String> REQUIEREN_INSTRUCTOR =
            Set.of("REVISION", TIPO_PROPUESTA, TIPO_RESOLUCION);

    /** Trámites que exigen indicar resultado (FAVORABLE / DESFAVORABLE). */
    private static final Set<String> REQUIEREN_RESULTADO = Set.of(TIPO_PROPUESTA, TIPO_RESOLUCION);

    private final TramiteRepository tramiteRepository;

    public TramitacionService(TramiteRepository tramiteRepository) {
        this.tramiteRepository = tramiteRepository;
    }

    /**
     * Sección "Tramitación" del detalle: estado actual, instructor asignado e histórico.
     * Solo lectura — la puede consultar cualquier usuario autenticado.
     *
     * @throws RecursoNoEncontradoException 404 si el expediente no existe
     */
    public SeccionTramitacionDto obtenerTramitacion(Long expedienteId, Locale locale) {
        String descColumn = ColumnaCatalogo.segunIdioma(locale);
        ExpedienteTramitacionInfo info = cargarInfo(expedienteId, descColumn);
        List<TramiteDto> tramites = tramiteRepository.buscarPorExpediente(expedienteId, descColumn);

        return new SeccionTramitacionDto(info.estado(), info.instructorId(), info.instructorNombre(), tramites);
    }

    /**
     * Instructores activos a los que se puede asignar un expediente.
     * Alimenta el desplegable de la sección Tramitación; el rol lo filtra el controlador.
     */
    public List<InstructorDto> listarInstructoresAsignables() {
        return tramiteRepository.listarInstructoresActivos();
    }

    /**
     * Asigna un instructor al expediente: cambia el estado a ASIGNADO y deja el trámite
     * ASIGNACION en el histórico. Las dos cosas o ninguna — de ahí {@code @Transactional}:
     * un expediente asignado sin traza de quién lo asignó sería un agujero de auditoría.
     *
     * <p>La autorización por rol (solo RESPONSABLE/ADMIN) la aplica el controlador con
     * {@code @PreAuthorize}; aquí se valida lo que depende del dato: que el expediente
     * exista, que el usuario destino exista y que de verdad sea instructor.
     *
     * @param usernameActor username del JWT de quien realiza la asignación
     * @throws RecursoNoEncontradoException 404 si el expediente o el actor no existen
     * @throws ReglaNegocioException        409 si el usuario destino no es un instructor activo
     */
    @Transactional
    public SeccionTramitacionDto asignarInstructor(Long expedienteId,
                                                   AsignacionInstructorRequest peticion,
                                                   String usernameActor,
                                                   Locale locale) {

        String descColumn = ColumnaCatalogo.segunIdioma(locale);
        cargarInfo(expedienteId, descColumn);

        Long instructorId = peticion.instructorId();
        if (!tramiteRepository.esInstructorActivo(instructorId)) {
            log.warn("Intento de asignar el expediente {} al usuario {}, que no es un instructor activo",
                    expedienteId, instructorId);
            throw new ReglaNegocioException("tramite.instructor.invalido", instructorId);
        }

        Long usuarioActorId = resolverUsuarioActor(usernameActor);

        tramiteRepository.asignarInstructor(expedienteId, instructorId);
        tramiteRepository.actualizarEstado(expedienteId, ESTADO_ASIGNADO);
        tramiteRepository.insertar(expedienteId, TIPO_ASIGNACION, usuarioActorId,
                LocalDateTime.now(), null, null);

        log.info("Expediente {} asignado al instructor {} por {}", expedienteId, instructorId, usernameActor);

        return obtenerTramitacion(expedienteId, locale);
    }

    /**
     * Registra un trámite sobre el expediente y mueve su estado en consecuencia.
     *
     * <p>Orden de las comprobaciones, de la más barata a la más cara y de la más
     * genérica a la más específica: existencia → catálogo → estado → permisos → datos.
     *
     * @param username username del JWT de quien tramita (nunca del cuerpo de la petición)
     * @param roles    roles del JWT, sin el prefijo {@code ROLE_}
     * @throws RecursoNoEncontradoException   404 expediente o tipo de trámite inexistentes
     * @throws ReglaNegocioException          409 el estado del expediente no admite este trámite
     * @throws OperacionNoPermitidaException  403 el instructor no tiene asignado este expediente
     */
    @Transactional
    public TramiteDto registrarTramite(Long expedienteId,
                                       NuevoTramiteRequest peticion,
                                       String username,
                                       Collection<String> roles,
                                       Locale locale) {

        String descColumn = ColumnaCatalogo.segunIdioma(locale);
        ExpedienteTramitacionInfo info = cargarInfo(expedienteId, descColumn);
        String tipo = peticion.tipoTramite().trim().toUpperCase(Locale.ROOT);

        if (!tramiteRepository.existeTipoTramite(tipo)) {
            throw new RecursoNoEncontradoException("tramite.tipo.notfound", tipo);
        }

        // La asignación tiene su propio endpoint porque además de dejar traza cambia el
        // instructor del expediente. Permitirla aquí dejaría trámites ASIGNACION que no
        // asignan a nadie: histórico y realidad dejarían de coincidir.
        if (TIPO_ASIGNACION.equals(tipo)) {
            throw new ReglaNegocioException("tramite.asignacion.endpoint");
        }

        // Un expediente resuelto está cerrado: lo único que queda por hacer es notificarlo.
        if (ESTADO_RESUELTO.equals(info.estado().codigo()) && !TIPO_NOTIFICACION.equals(tipo)) {
            throw new ReglaNegocioException("tramite.estado.invalido");
        }

        if (REQUIEREN_INSTRUCTOR.contains(tipo) && info.instructorId() == null) {
            throw new ReglaNegocioException("tramite.sin.instructor");
        }

        // Resolver es competencia del responsable, no del instructor que ha revisado.
        if (TIPO_RESOLUCION.equals(tipo) && !tieneAlgunRol(roles, ROL_RESPONSABLE, ROL_ADMIN)) {
            throw new OperacionNoPermitidaException("tramite.resolucion.rol");
        }

        // Se resuelve una sola vez: lo necesitan tanto la comprobación de pertenencia
        // como el propio INSERT. Va aquí y no antes para que las validaciones baratas
        // (catálogo, estado) puedan rechazar la petición sin tocar la tabla de usuarios.
        Long usuarioId = resolverUsuarioActor(username);

        comprobarInstructorAsignado(info, username, usuarioId, roles);

        if (REQUIEREN_RESULTADO.contains(tipo) && !tieneTexto(peticion.resultado())) {
            throw new ReglaNegocioException("tramite.resultado.required", tipo);
        }

        LocalDateTime ahora = LocalDateTime.now();

        Long tramiteId = tramiteRepository.insertar(expedienteId, tipo, usuarioId, ahora,
                peticion.resultado(), peticion.observaciones());

        aplicarCambioDeEstado(expedienteId, tipo);

        log.info("Trámite {} registrado sobre el expediente {} por {} (resultado: {})",
                tipo, expedienteId, username, peticion.resultado());

        // Se relee de BD en vez de componer el DTO en memoria: así la descripción del
        // catálogo sale traducida por el mismo camino que en la consulta, sin duplicar
        // la lógica de idioma, y el id/fecha devueltos son exactamente los persistidos.
        return tramiteRepository.buscarPorId(tramiteId, descColumn)
                .orElseThrow(() -> new RecursoNoEncontradoException("tramite.notfound", tramiteId));
    }

    /**
     * REGLA CLAVE DE LA FASE 1 (spec.md §3, invariante 4): un usuario cuyo único rol de
     * tramitación es INSTRUCTOR solo puede actuar sobre expedientes asignados a él.
     *
     * <p>Un RESPONSABLE o un ADMIN pasan de largo: su rol les da alcance sobre todos los
     * expedientes. Y ojo — esto se valida SIEMPRE en servidor: que el frontend oculte el
     * botón no es una medida de seguridad, solo de comodidad.
     */
    private void comprobarInstructorAsignado(ExpedienteTramitacionInfo info,
                                             String username,
                                             Long usuarioId,
                                             Collection<String> roles) {

        if (tieneAlgunRol(roles, ROL_RESPONSABLE, ROL_ADMIN)) {
            return;
        }
        if (!tieneAlgunRol(roles, ROL_INSTRUCTOR)) {
            throw new OperacionNoPermitidaException("tramite.no.permiso");
        }

        Long instructorAsignadoId = info.instructorId();

        if (instructorAsignadoId == null || !instructorAsignadoId.equals(usuarioId)) {
            log.warn("{} intentó tramitar el expediente {}, asignado a {}",
                    username, info.expedienteId(), instructorAsignadoId);
            throw new OperacionNoPermitidaException("tramite.no.permiso");
        }
    }

    /** Mueve el estado del expediente según el tipo de trámite; NOTIFICACION no lo cambia. */
    private void aplicarCambioDeEstado(Long expedienteId, String tipo) {
        if (TIPO_RESOLUCION.equals(tipo)) {
            tramiteRepository.marcarResuelto(expedienteId, LocalDate.now());
            return;
        }

        String nuevoEstado = ESTADO_TRAS_TRAMITE.get(tipo);
        if (nuevoEstado != null) {
            tramiteRepository.actualizarEstado(expedienteId, nuevoEstado);
        }
    }

    private ExpedienteTramitacionInfo cargarInfo(Long expedienteId, String descColumn) {
        return tramiteRepository.buscarInfoTramitacion(expedienteId, descColumn)
                .orElseThrow(() -> new RecursoNoEncontradoException("expediente.notfound", expedienteId));
    }

    /**
     * El username viene de un JWT válido, así que el usuario debería existir; que no
     * exista significa que se ha borrado con la sesión abierta — se trata como 404
     * en vez de dejar el trámite sin autor.
     */
    private Long resolverUsuarioActor(String username) {
        return tramiteRepository.buscarIdUsuarioActivo(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("usuario.notfound", username));
    }

    private static boolean tieneAlgunRol(Collection<String> roles, String... buscados) {
        if (roles == null) {
            return false;
        }
        for (String buscado : buscados) {
            if (roles.contains(buscado)) {
                return true;
            }
        }
        return false;
    }

    private static boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
