import { useTranslation } from 'react-i18next'
import { useAuthStore } from '../../store/authStore'

export default function ExpedientesPage() {
  const { t } = useTranslation()
  const { nombre, roles } = useAuthStore()

  return (
    <div>
      <h2 style={{ color: '#e2e8f0', marginBottom: '0.5rem' }}>
        {t('expedientes.title')}
      </h2>
      <p style={{ color: '#94a3b8', fontSize: '0.9rem' }}>
        {t('common.loading')} — {nombre} ({roles.join(', ')})
      </p>
      <p style={{ color: '#475569', fontSize: '0.85rem', marginTop: '1rem' }}>
        🚧 Búsqueda y tabla de expedientes — Semana 4
      </p>
    </div>
  )
}
