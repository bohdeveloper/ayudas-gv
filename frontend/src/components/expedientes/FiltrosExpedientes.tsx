import { FormEvent, useState } from 'react'
import { useTranslation } from 'react-i18next'
import type { EstadoExpediente, ExpedienteFiltros, TipoAyuda } from '../../types'
import { IconChevronDown, IconEraser, IconSearch } from '../icons/Icon'

const TIPOS_AYUDA: TipoAyuda[] = ['ASIG_DISCAP', 'PAGO_NAC', 'PARTO_MULT']
const ESTADOS: EstadoExpediente[] = ['REGISTRADO', 'ASIGNADO', 'EN_REVISION', 'RESUELTO']

interface Props {
  filtros: ExpedienteFiltros
  onChange: (filtros: ExpedienteFiltros) => void
  onSubmit: () => void
  onClear: () => void
  loading: boolean
}

export default function FiltrosExpedientes({ filtros, onChange, onSubmit, onClear, loading }: Props) {
  const { t } = useTranslation()
  const [expanded, setExpanded] = useState(true)

  function handleSubmit(e: FormEvent) {
    e.preventDefault()
    onSubmit()
  }

  function set<K extends keyof ExpedienteFiltros>(key: K, value: ExpedienteFiltros[K]) {
    onChange({ ...filtros, [key]: value || undefined })
  }

  return (
    <div style={styles.card}>
      <button
        type="button"
        style={styles.toggle}
        onClick={() => setExpanded((v) => !v)}
        aria-expanded={expanded}
        aria-controls="filtros-expedientes-body"
      >
        <span style={styles.toggleLabel}>{t('expedientes.search')}</span>
        <IconChevronDown
          style={{
            transform: expanded ? 'rotate(180deg)' : 'rotate(0deg)',
            transition: 'transform var(--transition-fast)',
            color: 'var(--color-text-muted)',
          }}
        />
      </button>

      {expanded && (
        <form
          id="filtros-expedientes-body"
          onSubmit={handleSubmit}
          style={styles.form}
          aria-label={t('expedientes.search')}
        >
          <div style={styles.row}>
            <label style={{ ...styles.label, flexBasis: 170 }} htmlFor="f-numero">
              {t('expedientes.numero')}
              <input
                id="f-numero"
                style={styles.input}
                type="text"
                value={filtros.numeroExpediente ?? ''}
                onChange={(e) => set('numeroExpediente', e.target.value)}
                disabled={loading}
              />
            </label>

            <label style={{ ...styles.label, flexBasis: 190 }} htmlFor="f-tipo">
              {t('expedientes.tipoAyuda')}
              <select
                id="f-tipo"
                style={styles.input}
                value={filtros.tipoAyuda ?? ''}
                onChange={(e) => set('tipoAyuda', e.target.value as TipoAyuda)}
                disabled={loading}
              >
                <option value="">{t('common.all')}</option>
                {TIPOS_AYUDA.map((codigo) => (
                  <option key={codigo} value={codigo}>
                    {t(`tiposAyuda.${codigo}`)}
                  </option>
                ))}
              </select>
            </label>

            <label style={{ ...styles.label, flexBasis: 170 }} htmlFor="f-estado">
              {t('expedientes.estado')}
              <select
                id="f-estado"
                style={styles.input}
                value={filtros.estado ?? ''}
                onChange={(e) => set('estado', e.target.value as EstadoExpediente)}
                disabled={loading}
              >
                <option value="">{t('common.all')}</option>
                {ESTADOS.map((codigo) => (
                  <option key={codigo} value={codigo}>
                    {t(`expedientes.estados.${codigo}`)}
                  </option>
                ))}
              </select>
            </label>

            <label style={{ ...styles.label, flexBasis: 190 }} htmlFor="f-documento">
              {t('expedientes.documento')}
              <input
                id="f-documento"
                style={styles.input}
                type="text"
                value={filtros.documentoSolicitante ?? ''}
                onChange={(e) => set('documentoSolicitante', e.target.value)}
                disabled={loading}
              />
            </label>

            <label style={{ ...styles.label, flexBasis: 160 }} htmlFor="f-desde">
              {t('expedientes.fechaDesde')}
              <input
                id="f-desde"
                style={styles.input}
                type="date"
                value={filtros.fechaDesde ?? ''}
                onChange={(e) => set('fechaDesde', e.target.value)}
                disabled={loading}
              />
            </label>

            <label style={{ ...styles.label, flexBasis: 160 }} htmlFor="f-hasta">
              {t('expedientes.fechaHasta')}
              <input
                id="f-hasta"
                style={styles.input}
                type="date"
                value={filtros.fechaHasta ?? ''}
                onChange={(e) => set('fechaHasta', e.target.value)}
                disabled={loading}
              />
            </label>
          </div>

          <div style={styles.actions}>
            <button style={styles.btnPrimary} type="submit" disabled={loading}>
              <IconSearch />
              {t('common.search')}
            </button>
            <button style={styles.btnSecondary} type="button" onClick={onClear} disabled={loading}>
              <IconEraser />
              {t('common.clear')}
            </button>
          </div>
        </form>
      )}
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  card: {
    background: 'var(--color-surface)',
    border: '1px solid var(--color-border)',
    borderRadius: 'var(--radius-lg)',
    marginBottom: '1.5rem',
    overflow: 'hidden',
  },
  toggle: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    width: '100%',
    background: 'transparent',
    border: 'none',
    padding: '0.9rem 1.5rem',
    cursor: 'pointer',
    color: 'var(--color-text)',
  },
  toggleLabel: { fontSize: '0.9rem', fontWeight: 600 },
  form: {
    padding: '0 1.5rem 1.25rem',
    borderTop: '1px solid var(--color-border)',
  },
  row: {
    display: 'flex',
    flexWrap: 'nowrap',
    alignItems: 'flex-end',
    gap: '1rem',
    overflowX: 'auto',
    paddingTop: '1.1rem',
    paddingBottom: '0.25rem',
  },
  label: {
    display: 'flex',
    flexDirection: 'column',
    flexShrink: 0,
    gap: '0.4rem',
    color: 'var(--color-text)',
    fontSize: '0.8rem',
    fontWeight: 500,
    whiteSpace: 'nowrap',
  },
  input: {
    background: 'var(--color-bg)',
    border: '1px solid var(--color-border-strong)',
    borderRadius: 'var(--radius-sm)',
    padding: '0.55rem 0.65rem',
    color: 'var(--color-text)',
    fontSize: '0.85rem',
    fontFamily: 'inherit',
    outline: 'none',
    width: '100%',
    transition: 'border-color var(--transition-fast)',
  },
  actions: {
    display: 'flex',
    gap: '0.6rem',
    marginTop: '1rem',
  },
  btnPrimary: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '0.45rem',
    background: 'var(--color-primary)',
    color: 'var(--color-primary-contrast)',
    border: 'none',
    borderRadius: 'var(--radius-sm)',
    padding: '0.55rem 1.15rem',
    fontWeight: 700,
    fontSize: '0.85rem',
    cursor: 'pointer',
    whiteSpace: 'nowrap',
    transition: 'background-color var(--transition-fast)',
  },
  btnSecondary: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '0.45rem',
    background: 'transparent',
    border: '1px solid var(--color-border-strong)',
    color: 'var(--color-text-muted)',
    borderRadius: 'var(--radius-sm)',
    padding: '0.55rem 1.15rem',
    fontSize: '0.85rem',
    cursor: 'pointer',
    whiteSpace: 'nowrap',
    transition: 'border-color var(--transition-fast), color var(--transition-fast)',
  },
}
