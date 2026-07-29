import { useCallback, useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import {
  obtenerAyuda,
  obtenerContacto,
  obtenerGenerales,
  obtenerInteresados,
} from '../../api/expedienteDetalleApi'
import EstadoBadge from '../../components/expedientes/EstadoBadge'
import SeccionGeneralesPanel from '../../components/expedientes/detalle/SeccionGeneralesPanel'
import SeccionInteresadosPanel from '../../components/expedientes/detalle/SeccionInteresadosPanel'
import SeccionContactoPanel from '../../components/expedientes/detalle/SeccionContactoPanel'
import SeccionAyudaPanel from '../../components/expedientes/detalle/SeccionAyudaPanel'
import SeccionTramitacionPanel from '../../components/expedientes/detalle/SeccionTramitacionPanel'
import { obtenerTramitacion } from '../../api/tramitacionApi'
import { IconAlertCircle, IconChevronLeft } from '../../components/icons/Icon'
import type {
  SeccionAyuda,
  SeccionContacto,
  SeccionGenerales,
  SeccionInteresados,
  SeccionTramitacion,
} from '../../types'

type TabKey = 'generales' | 'interesados' | 'contacto' | 'ayuda' | 'tramitacion'

const TABS: TabKey[] = ['generales', 'interesados', 'contacto', 'ayuda', 'tramitacion']

interface SeccionData {
  generales?: SeccionGenerales
  interesados?: SeccionInteresados
  contacto?: SeccionContacto
  ayuda?: SeccionAyuda
  tramitacion?: SeccionTramitacion
}

interface EstadoSeccion {
  status: 'idle' | 'loading' | 'success' | 'error'
}

const CARGADORES: Record<TabKey, (id: number) => Promise<unknown>> = {
  generales: obtenerGenerales,
  interesados: obtenerInteresados,
  contacto: obtenerContacto,
  ayuda: obtenerAyuda,
  tramitacion: obtenerTramitacion,
}

export default function ExpedienteDetalle() {
  const { t, i18n } = useTranslation()
  const { id } = useParams<{ id: string }>()
  const expedienteId = Number(id)

  const [activeTab, setActiveTab] = useState<TabKey>('generales')
  const [datos, setDatos] = useState<SeccionData>({})
  const [estados, setEstados] = useState<Record<TabKey, EstadoSeccion>>({
    generales: { status: 'idle' },
    interesados: { status: 'idle' },
    contacto: { status: 'idle' },
    ayuda: { status: 'idle' },
    tramitacion: { status: 'idle' },
  })

  const cargarSeccion = useCallback(
    async (tab: TabKey) => {
      setEstados((prev) => ({ ...prev, [tab]: { status: 'loading' } }))
      try {
        const data = await CARGADORES[tab](expedienteId)
        setDatos((prev) => ({ ...prev, [tab]: data }))
        setEstados((prev) => ({ ...prev, [tab]: { status: 'success' } }))
      } catch {
        setEstados((prev) => ({ ...prev, [tab]: { status: 'error' } }))
      }
    },
    [expedienteId],
  )

  // Recarga la sección activa al cambiar de pestaña (primera vez) o de idioma
  // (las descripciones de catálogo de "generales" dependen de Accept-Language).
  useEffect(() => {
    cargarSeccion(activeTab)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeTab, i18n.language])

  // Un trámite cambia el estado del expediente, y el estado se muestra en dos sitios:
  // la propia sección y el badge de la cabecera (que sale de "generales"). Por eso se
  // recargan las dos, o la cabecera se quedaría mostrando el estado anterior.
  const recargarTrasTramitar = useCallback(() => {
    cargarSeccion('tramitacion')
    cargarSeccion('generales')
  }, [cargarSeccion])

  const generales = datos.generales

  return (
    <div>
      <Link to="/expedientes" style={styles.volver}>
        <IconChevronLeft />
        {t('common.back')}
      </Link>

      <header style={styles.header}>
        <h1 style={styles.titulo}>
          {generales?.numeroExpediente ?? t('expedientes.detalle.titulo')}
        </h1>
        {generales && <EstadoBadge codigo={generales.estado.codigo} descripcion={generales.estado.descripcion} />}
      </header>

      <div role="tablist" aria-label={t('expedientes.detalle.titulo')} style={styles.tablist}>
        {TABS.map((tab) => (
          <button
            key={tab}
            type="button"
            role="tab"
            id={`tab-${tab}`}
            aria-selected={activeTab === tab}
            aria-controls={`tabpanel-${tab}`}
            style={{ ...styles.tab, ...(activeTab === tab ? styles.tabActive : {}) }}
            onClick={() => setActiveTab(tab)}
          >
            {t(`expedientes.sections.${tab}`)}
          </button>
        ))}
      </div>

      <div style={styles.panel}>
        {TABS.map((tab) => (
          <div
            key={tab}
            id={`tabpanel-${tab}`}
            role="tabpanel"
            aria-labelledby={`tab-${tab}`}
            hidden={activeTab !== tab}
          >
            {estados[tab].status === 'loading' && <p style={styles.info}>{t('common.loading')}</p>}

            {estados[tab].status === 'error' && (
              <p role="alert" style={styles.error}>
                <IconAlertCircle style={{ flexShrink: 0 }} />
                {t('expedientes.detalle.errorCargaSeccion')}
              </p>
            )}

            {estados[tab].status === 'success' && tab === 'generales' && datos.generales && (
              <SeccionGeneralesPanel data={datos.generales} />
            )}
            {estados[tab].status === 'success' && tab === 'interesados' && datos.interesados && (
              <SeccionInteresadosPanel data={datos.interesados} />
            )}
            {estados[tab].status === 'success' && tab === 'contacto' && datos.contacto && (
              <SeccionContactoPanel data={datos.contacto} />
            )}
            {estados[tab].status === 'success' && tab === 'ayuda' && datos.ayuda && (
              <SeccionAyudaPanel data={datos.ayuda} />
            )}
            {estados[tab].status === 'success' && tab === 'tramitacion' && datos.tramitacion && (
              <SeccionTramitacionPanel
                expedienteId={expedienteId}
                data={datos.tramitacion}
                onCambio={recargarTrasTramitar}
              />
            )}
          </div>
        ))}
      </div>
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  volver: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '0.3rem',
    color: 'var(--color-text-muted)',
    textDecoration: 'none',
    fontSize: '0.85rem',
    fontWeight: 500,
    marginBottom: '1rem',
  },
  header: {
    display: 'flex',
    alignItems: 'center',
    gap: '0.85rem',
    marginBottom: '1.5rem',
    flexWrap: 'wrap',
  },
  titulo: { color: 'var(--color-text)', fontSize: '1.4rem', fontWeight: 700, margin: 0 },
  tablist: {
    display: 'flex',
    gap: '0.25rem',
    borderBottom: '1px solid var(--color-border)',
    marginBottom: '1.5rem',
    overflowX: 'auto',
  },
  tab: {
    background: 'transparent',
    border: 'none',
    borderBottom: '2px solid transparent',
    color: 'var(--color-text-muted)',
    padding: '0.7rem 1rem',
    fontSize: '0.87rem',
    fontWeight: 600,
    cursor: 'pointer',
    whiteSpace: 'nowrap',
    transition: 'color var(--transition-fast), border-color var(--transition-fast)',
  },
  tabActive: { color: 'var(--color-text)', borderBottomColor: 'var(--color-primary)' },
  panel: {
    background: 'var(--color-surface)',
    border: '1px solid var(--color-border)',
    borderRadius: 'var(--radius-lg)',
    padding: '1.5rem',
  },
  info: { color: 'var(--color-text-muted)', fontSize: '0.88rem', margin: 0 },
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
    margin: 0,
  },
}
