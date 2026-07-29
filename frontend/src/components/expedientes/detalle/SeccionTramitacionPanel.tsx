import { useEffect, useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import type {
  InstructorAsignable,
  NuevoTramiteRequest,
  ResultadoTramite,
  SeccionTramitacion,
  TipoTramite,
} from '../../../types'
import { asignarInstructor, obtenerInstructores, registrarTramite } from '../../../api/tramitacionApi'
import { useAuthStore } from '../../../store/authStore'
import { formatFecha } from '../../../utils/formatFecha'
import EstadoBadge from '../EstadoBadge'
import CampoDato from './CampoDato'
import { panelStyles } from './panelStyles'
import { IconAlertCircle, IconCheckCircle, IconPlus, IconUser } from '../../icons/Icon'

interface Props {
  expedienteId: number
  data: SeccionTramitacion
  /** Se llama tras registrar un trámite o asignar instructor, para refrescar la sección. */
  onCambio: () => void
}

/** Trámites que el usuario puede registrar desde la interfaz. */
const TIPOS_REGISTRABLES: TipoTramite[] = ['REGISTRO', 'REVISION', 'PROPUESTA', 'RESOLUCION', 'NOTIFICACION']

/** Trámites que exigen resultado — el backend los rechaza sin él (409). */
const REQUIEREN_RESULTADO: TipoTramite[] = ['PROPUESTA', 'RESOLUCION']

/** Trámites reservados al rol RESPONSABLE. */
const SOLO_RESPONSABLE: TipoTramite[] = ['RESOLUCION']

const RESULTADOS: ResultadoTramite[] = ['FAVORABLE', 'DESFAVORABLE']

export default function SeccionTramitacionPanel({ expedienteId, data, onCambio }: Props) {
  const { t, i18n } = useTranslation()
  const roles = useAuthStore((state) => state.roles)

  const esResponsable = roles.includes('RESPONSABLE') || roles.includes('ADMIN')
  const puedeTramitar = esResponsable || roles.includes('INSTRUCTOR')
  const expedienteCerrado = data.estado.codigo === 'RESUELTO'

  // Los tipos que se ofrecen dependen del rol: no tiene sentido enseñar RESOLUCION
  // a un instructor si el backend se la va a rechazar con un 403.
  const tiposDisponibles = useMemo(
    () => TIPOS_REGISTRABLES.filter((tipo) => esResponsable || !SOLO_RESPONSABLE.includes(tipo)),
    [esResponsable],
  )

  const [tipoTramite, setTipoTramite] = useState<TipoTramite>(tiposDisponibles[0])
  const [resultado, setResultado] = useState<ResultadoTramite | ''>('')
  const [observaciones, setObservaciones] = useState('')
  const [enviando, setEnviando] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [instructores, setInstructores] = useState<InstructorAsignable[]>([])
  const [instructorSeleccionado, setInstructorSeleccionado] = useState<string>('')
  const [asignando, setAsignando] = useState(false)

  // Solo quien puede asignar carga la lista: al resto el backend le devolvería 403.
  // Y si el expediente ya está resuelto no se reasigna, así que tampoco hace falta.
  useEffect(() => {
    if (!esResponsable || expedienteCerrado) return
    obtenerInstructores()
      .then(setInstructores)
      .catch(() => setInstructores([]))
  }, [esResponsable, expedienteCerrado])

  const necesitaResultado = REQUIEREN_RESULTADO.includes(tipoTramite)

  async function onSubmitTramite(evento: React.FormEvent) {
    evento.preventDefault()
    setError(null)
    setEnviando(true)

    const peticion: NuevoTramiteRequest = {
      tipoTramite,
      resultado: resultado === '' ? undefined : resultado,
      observaciones: observaciones.trim() === '' ? undefined : observaciones.trim(),
    }

    try {
      await registrarTramite(expedienteId, peticion)
      setObservaciones('')
      setResultado('')
      onCambio()
    } catch (e) {
      // El backend ya devuelve el motivo traducido según Accept-Language:
      // se muestra tal cual en vez de reinventar el mensaje aquí.
      setError(mensajeDeError(e) ?? t('expedientes.tramitacion.errorRegistrar'))
    } finally {
      setEnviando(false)
    }
  }

  async function onAsignar(evento: React.FormEvent) {
    evento.preventDefault()
    setError(null)
    setAsignando(true)

    try {
      await asignarInstructor(expedienteId, Number(instructorSeleccionado))
      setInstructorSeleccionado('')
      onCambio()
    } catch (e) {
      setError(mensajeDeError(e) ?? t('expedientes.tramitacion.errorAsignar'))
    } finally {
      setAsignando(false)
    }
  }

  return (
    <div>
      <div style={panelStyles.grid}>
        <CampoDato label={t('expedientes.estado')}>
          <EstadoBadge codigo={data.estado.codigo} descripcion={data.estado.descripcion} />
        </CampoDato>
        <CampoDato label={t('expedientes.instructor')}>
          {data.instructorNombre ?? (
            <span style={styles.sinAsignar}>{t('expedientes.tramitacion.sinInstructor')}</span>
          )}
        </CampoDato>
        <CampoDato label={t('expedientes.tramitacion.numeroTramites')}>{data.tramites.length}</CampoDato>
      </div>

      {error && (
        <p role="alert" style={styles.error}>
          <IconAlertCircle style={{ flexShrink: 0 }} />
          {error}
        </p>
      )}

      {/* ─── Histórico ─────────────────────────────────────────────────────── */}
      <section style={panelStyles.subseccion}>
        <h3 style={panelStyles.subseccionTitulo}>{t('expedientes.tramitacion.historico')}</h3>

        {data.tramites.length === 0 ? (
          <p style={panelStyles.vacio}>{t('expedientes.tramitacion.sinTramites')}</p>
        ) : (
          <ol style={styles.timeline}>
            {data.tramites.map((tramite) => (
              <li key={tramite.id} style={styles.hito}>
                <span aria-hidden="true" style={styles.punto} />
                <div style={styles.hitoCabecera}>
                  <span style={styles.hitoTipo}>{tramite.tipoTramite.descripcion}</span>
                  {tramite.resultado && (
                    <span
                      style={{
                        ...styles.resultado,
                        color:
                          tramite.resultado === 'FAVORABLE'
                            ? 'var(--color-success)'
                            : 'var(--color-danger)',
                        background:
                          tramite.resultado === 'FAVORABLE'
                            ? 'var(--color-success-bg)'
                            : 'var(--color-danger-bg)',
                      }}
                    >
                      <IconCheckCircle width={13} height={13} />
                      {t(`expedientes.tramitacion.resultados.${tramite.resultado}`)}
                    </span>
                  )}
                </div>
                <div style={styles.hitoMeta}>
                  <IconUser width={13} height={13} />
                  <span>{tramite.usuarioNombre}</span>
                  <span aria-hidden="true">·</span>
                  <time dateTime={tramite.fecha}>{formatFecha(tramite.fecha, i18n.language)}</time>
                </div>
                {tramite.observaciones && <p style={styles.hitoObs}>{tramite.observaciones}</p>}
              </li>
            ))}
          </ol>
        )}
      </section>

      {/* ─── Asignar instructor (solo RESPONSABLE/ADMIN) ───────────────────── */}
      {esResponsable && !expedienteCerrado && (
        <section style={panelStyles.subseccion}>
          <h3 style={panelStyles.subseccionTitulo}>{t('expedientes.tramitacion.asignarInstructor')}</h3>
          <form onSubmit={onAsignar} style={styles.formaLinea}>
            <label style={styles.label} htmlFor="instructor-select">
              {t('expedientes.instructor')}
            </label>
            <select
              id="instructor-select"
              style={styles.control}
              value={instructorSeleccionado}
              onChange={(e) => setInstructorSeleccionado(e.target.value)}
            >
              <option value="">{t('expedientes.tramitacion.elegirInstructor')}</option>
              {instructores.map((instructor) => (
                <option key={instructor.id} value={instructor.id}>
                  {instructor.nombreCompleto}
                </option>
              ))}
            </select>
            <button type="submit" style={styles.boton} disabled={instructorSeleccionado === '' || asignando}>
              {asignando ? t('common.loading') : t('expedientes.tramitacion.asignar')}
            </button>
          </form>
        </section>
      )}

      {/* ─── Registrar trámite ─────────────────────────────────────────────── */}
      {puedeTramitar && (
        <section style={panelStyles.subseccion}>
          <h3 style={panelStyles.subseccionTitulo}>{t('expedientes.tramitacion.nuevoTramite')}</h3>

          {expedienteCerrado && (
            <p style={panelStyles.vacio}>{t('expedientes.tramitacion.expedienteCerrado')}</p>
          )}

          <form onSubmit={onSubmitTramite} style={styles.formulario}>
            <div style={styles.fila}>
              <div style={styles.campo}>
                <label style={styles.label} htmlFor="tipo-tramite">
                  {t('expedientes.tramitacion.tipo')}
                </label>
                <select
                  id="tipo-tramite"
                  style={styles.control}
                  value={tipoTramite}
                  onChange={(e) => setTipoTramite(e.target.value as TipoTramite)}
                >
                  {tiposDisponibles.map((tipo) => (
                    <option key={tipo} value={tipo}>
                      {t(`expedientes.tramitacion.tipos.${tipo}`)}
                    </option>
                  ))}
                </select>
              </div>

              <div style={styles.campo}>
                <label style={styles.label} htmlFor="resultado-tramite">
                  {t('expedientes.tramitacion.resultado')}
                  {necesitaResultado && <span aria-hidden="true"> *</span>}
                </label>
                <select
                  id="resultado-tramite"
                  style={styles.control}
                  value={resultado}
                  onChange={(e) => setResultado(e.target.value as ResultadoTramite | '')}
                  required={necesitaResultado}
                >
                  <option value="">{t('expedientes.tramitacion.sinResultado')}</option>
                  {RESULTADOS.map((valor) => (
                    <option key={valor} value={valor}>
                      {t(`expedientes.tramitacion.resultados.${valor}`)}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div style={styles.campo}>
              <label style={styles.label} htmlFor="observaciones-tramite">
                {t('expedientes.detalle.observaciones')}
              </label>
              <textarea
                id="observaciones-tramite"
                style={{ ...styles.control, minHeight: 72, resize: 'vertical' }}
                maxLength={1000}
                value={observaciones}
                onChange={(e) => setObservaciones(e.target.value)}
              />
            </div>

            <button type="submit" style={styles.boton} disabled={enviando}>
              <IconPlus width={15} height={15} />
              {enviando ? t('common.loading') : t('expedientes.tramitacion.registrar')}
            </button>
          </form>
        </section>
      )}
    </div>
  )
}

/** Extrae el mensaje que ya viene traducido en el ErrorResponse del backend. */
function mensajeDeError(error: unknown): string | null {
  const respuesta = (error as { response?: { data?: { message?: string } } })?.response
  return respuesta?.data?.message ?? null
}

const styles: Record<string, React.CSSProperties> = {
  sinAsignar: { color: 'var(--color-text-faint)', fontStyle: 'italic' },
  error: {
    display: 'flex',
    alignItems: 'center',
    gap: '0.5rem',
    background: 'var(--color-danger-bg)',
    border: '1px solid rgba(248,113,113,0.35)',
    borderRadius: 'var(--radius-sm)',
    color: 'var(--color-danger)',
    fontSize: '0.85rem',
    padding: '0.6rem 0.75rem',
    marginTop: '1rem',
    marginBottom: 0,
  },
  timeline: {
    listStyle: 'none',
    margin: 0,
    padding: '0 0 0 1.1rem',
    borderLeft: '2px solid var(--color-border)',
    display: 'flex',
    flexDirection: 'column',
    gap: '1.1rem',
  },
  hito: { position: 'relative' },
  punto: {
    position: 'absolute',
    left: '-1.45rem',
    top: '0.35rem',
    width: 8,
    height: 8,
    borderRadius: '50%',
    background: 'var(--color-primary)',
  },
  hitoCabecera: { display: 'flex', alignItems: 'center', gap: '0.5rem', flexWrap: 'wrap' },
  hitoTipo: { color: 'var(--color-text)', fontSize: '0.9rem', fontWeight: 600 },
  resultado: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '0.25rem',
    borderRadius: 999,
    padding: '0.1rem 0.5rem',
    fontSize: '0.72rem',
    fontWeight: 600,
  },
  hitoMeta: {
    display: 'flex',
    alignItems: 'center',
    gap: '0.35rem',
    color: 'var(--color-text-muted)',
    fontSize: '0.78rem',
    marginTop: '0.2rem',
  },
  hitoObs: { color: 'var(--color-text-muted)', fontSize: '0.85rem', margin: '0.35rem 0 0' },
  formulario: { display: 'flex', flexDirection: 'column', gap: '0.85rem' },
  formaLinea: { display: 'flex', alignItems: 'flex-end', gap: '0.75rem', flexWrap: 'wrap' },
  fila: { display: 'flex', gap: '0.75rem', flexWrap: 'wrap' },
  campo: { display: 'flex', flexDirection: 'column', gap: '0.3rem', minWidth: 190, flex: 1 },
  label: {
    color: 'var(--color-text-muted)',
    fontSize: '0.74rem',
    fontWeight: 600,
    textTransform: 'uppercase',
    letterSpacing: '0.04em',
  },
  control: {
    background: 'var(--color-bg)',
    border: '1px solid var(--color-border)',
    borderRadius: 'var(--radius-sm)',
    color: 'var(--color-text)',
    fontSize: '0.88rem',
    padding: '0.45rem 0.6rem',
    fontFamily: 'inherit',
  },
  boton: {
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '0.4rem',
    alignSelf: 'flex-start',
    background: 'var(--color-primary)',
    border: 'none',
    borderRadius: 'var(--radius-sm)',
    color: '#fff',
    cursor: 'pointer',
    fontSize: '0.85rem',
    fontWeight: 600,
    padding: '0.5rem 0.9rem',
  },
}
