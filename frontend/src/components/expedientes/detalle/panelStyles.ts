import type { CSSProperties } from 'react'

/** Estilos compartidos por los paneles de sección del detalle del expediente. */
export const panelStyles: Record<string, CSSProperties> = {
  grid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))',
    gap: '1.25rem 1.5rem',
  },
  subseccion: {
    display: 'flex',
    flexDirection: 'column',
    gap: '0.9rem',
    paddingTop: '1.1rem',
    marginTop: '1.1rem',
    borderTop: '1px solid var(--color-border)',
  },
  subseccionTitulo: {
    margin: 0,
    color: 'var(--color-text)',
    fontSize: '0.85rem',
    fontWeight: 700,
  },
  vacio: { color: 'var(--color-text-faint)', fontSize: '0.88rem', margin: 0 },
}
