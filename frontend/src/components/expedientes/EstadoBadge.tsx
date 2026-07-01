import type { EstadoExpediente } from '../../types'

// Color semántico por estado — la información nunca depende solo del color:
// el texto de la descripción (ya traducida por el backend) siempre acompaña.
const ESTADO_STYLE: Record<EstadoExpediente, { color: string; bg: string }> = {
  REGISTRADO: { color: 'var(--color-neutral)', bg: 'var(--color-neutral-bg)' },
  ASIGNADO: { color: 'var(--color-info)', bg: 'var(--color-info-bg)' },
  EN_REVISION: { color: 'var(--color-warning)', bg: 'var(--color-warning-bg)' },
  RESUELTO: { color: 'var(--color-success)', bg: 'var(--color-success-bg)' },
}

interface Props {
  codigo: EstadoExpediente
  descripcion: string
}

export default function EstadoBadge({ codigo, descripcion }: Props) {
  const style = ESTADO_STYLE[codigo] ?? { color: 'var(--color-neutral)', bg: 'var(--color-neutral-bg)' }

  return (
    <span
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: '0.4rem',
        padding: '0.22rem 0.6rem',
        borderRadius: 999,
        background: style.bg,
        color: style.color,
        fontSize: '0.78rem',
        fontWeight: 600,
        whiteSpace: 'nowrap',
      }}
    >
      <span
        aria-hidden="true"
        style={{ width: 6, height: 6, borderRadius: '50%', background: 'currentColor', flexShrink: 0 }}
      />
      {descripcion}
    </span>
  )
}
