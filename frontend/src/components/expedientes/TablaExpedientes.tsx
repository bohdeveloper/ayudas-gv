import { Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import type { ExpedienteResumen } from '../../types'
import EstadoBadge from './EstadoBadge'
import { IconInbox } from '../icons/Icon'
import { formatFecha } from '../../utils/formatFecha'

interface Props {
  expedientes: ExpedienteResumen[]
  loading: boolean
}

const SKELETON_ROWS = 5

export default function TablaExpedientes({ expedientes, loading }: Props) {
  const { t, i18n } = useTranslation()

  return (
    <div style={styles.wrapper}>
      <div style={styles.scroller}>
        <table style={styles.table}>
          <thead>
            <tr>
              <th style={styles.th}>{t('expedientes.numero')}</th>
              <th style={styles.th}>{t('expedientes.tipoAyuda')}</th>
              <th style={styles.th}>{t('expedientes.estado')}</th>
              <th style={styles.th}>{t('expedientes.fechaRegistro')}</th>
              <th style={styles.th}>{t('expedientes.instructor')}</th>
            </tr>
          </thead>
          <tbody>
            {loading &&
              Array.from({ length: SKELETON_ROWS }).map((_, i) => (
                <tr key={`skeleton-${i}`} style={styles.tr}>
                  {Array.from({ length: 5 }).map((__, j) => (
                    <td key={j} style={styles.td}>
                      <span style={styles.skeletonBar} />
                    </td>
                  ))}
                </tr>
              ))}

            {!loading &&
              expedientes.map((exp) => (
                <tr key={exp.id} style={styles.tr}>
                  <td style={{ ...styles.td, ...styles.noWrap, fontWeight: 600 }} className="tabular-nums">
                    <Link to={`/expedientes/${exp.id}`} style={styles.link}>
                      {exp.numeroExpediente}
                    </Link>
                  </td>
                  <td style={styles.td}>{exp.tipoAyuda.descripcion}</td>
                  <td style={{ ...styles.td, ...styles.noWrap }}>
                    <EstadoBadge codigo={exp.estado.codigo} descripcion={exp.estado.descripcion} />
                  </td>
                  <td style={{ ...styles.td, ...styles.noWrap }} className="tabular-nums">
                    {formatFecha(exp.fechaRegistro, i18n.language)}
                  </td>
                  <td
                    style={{
                      ...styles.td,
                      ...styles.noWrap,
                      color: exp.instructorNombre ? undefined : 'var(--color-text-faint)',
                    }}
                  >
                    {exp.instructorNombre ?? '—'}
                  </td>
                </tr>
              ))}
          </tbody>
        </table>
      </div>

      {!loading && expedientes.length === 0 && (
        <div style={styles.emptyState}>
          <IconInbox width={28} height={28} style={{ color: 'var(--color-text-faint)' }} />
          <p style={styles.emptyText}>{t('expedientes.noResults')}</p>
        </div>
      )}
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  wrapper: {
    background: 'var(--color-surface)',
    border: '1px solid var(--color-border)',
    borderRadius: 'var(--radius-lg)',
    overflow: 'hidden',
  },
  scroller: { overflowX: 'auto' },
  table: {
    width: '100%',
    borderCollapse: 'collapse',
    minWidth: 760,
  },
  th: {
    textAlign: 'left',
    padding: '0.7rem 1.1rem',
    background: 'var(--color-surface-raised)',
    color: 'var(--color-text-muted)',
    fontSize: '0.74rem',
    fontWeight: 600,
    textTransform: 'uppercase',
    letterSpacing: '0.04em',
    borderBottom: '1px solid var(--color-border)',
    whiteSpace: 'nowrap',
  },
  tr: { borderBottom: '1px solid var(--color-border)' },
  td: {
    padding: '0.7rem 1.1rem',
    color: 'var(--color-text)',
    fontSize: '0.87rem',
  },
  noWrap: { whiteSpace: 'nowrap' },
  link: { color: 'var(--color-primary)', textDecoration: 'none' },
  skeletonBar: {
    display: 'block',
    height: 12,
    borderRadius: 4,
    background: 'var(--color-surface-raised)',
    animation: 'ayudasgv-pulse 1.4s ease-in-out infinite',
    width: '70%',
  },
  emptyState: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    gap: '0.6rem',
    padding: '2.5rem 1rem',
  },
  emptyText: { color: 'var(--color-text-muted)', fontSize: '0.88rem', margin: 0 },
}
