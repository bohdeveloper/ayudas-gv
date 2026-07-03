import type { ReactNode } from 'react'

interface Props {
  label: string
  children?: ReactNode
}

/** Par etiqueta/valor reutilizado en todas las secciones del detalle del expediente. */
export default function CampoDato({ label, children }: Props) {
  const vacio = children === undefined || children === null || children === ''

  return (
    <div style={styles.campo}>
      <dt style={styles.label}>{label}</dt>
      <dd style={{ ...styles.value, ...(vacio ? styles.valueVacio : {}) }}>{vacio ? '—' : children}</dd>
    </div>
  )
}

const styles: Record<string, React.CSSProperties> = {
  campo: { display: 'flex', flexDirection: 'column', gap: '0.3rem', minWidth: 160 },
  label: {
    margin: 0,
    color: 'var(--color-text-muted)',
    fontSize: '0.74rem',
    fontWeight: 600,
    textTransform: 'uppercase',
    letterSpacing: '0.04em',
  },
  value: { margin: 0, color: 'var(--color-text)', fontSize: '0.92rem' },
  valueVacio: { color: 'var(--color-text-faint)' },
}
