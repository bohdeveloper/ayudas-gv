import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { buscarExpedientes } from '../../api/expedientesApi'
import FiltrosExpedientes from '../../components/expedientes/FiltrosExpedientes'
import TablaExpedientes from '../../components/expedientes/TablaExpedientes'
import Paginacion from '../../components/expedientes/Paginacion'
import { IconAlertCircle } from '../../components/icons/Icon'
import type { ExpedienteFiltros, ExpedienteResumen } from '../../types'

const PAGE_SIZE = 10
const FILTROS_INICIALES: ExpedienteFiltros = {}

export default function ExpedientesPage() {
  const { t, i18n } = useTranslation()

  const [filtros, setFiltros] = useState<ExpedienteFiltros>(FILTROS_INICIALES)
  const [page, setPage] = useState(0)
  const [expedientes, setExpedientes] = useState<ExpedienteResumen[]>([])
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  async function ejecutarBusqueda(filtrosActivos: ExpedienteFiltros, paginaActual: number) {
    setLoading(true)
    setError(null)
    try {
      const resultado = await buscarExpedientes({
        ...filtrosActivos,
        page: paginaActual,
        size: PAGE_SIZE,
      })
      setExpedientes(resultado.content)
      setTotalPages(resultado.totalPages)
      setTotalElements(resultado.totalElements)
    } catch {
      setError(t('common.error'))
      setExpedientes([])
      setTotalPages(0)
      setTotalElements(0)
    } finally {
      setLoading(false)
    }
  }

  // Vuelve a pedir la página actual cuando cambia (búsqueda inicial, paginación,
  // o cambio de idioma — las descripciones de catálogo dependen de Accept-Language)
  useEffect(() => {
    ejecutarBusqueda(filtros, page)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, i18n.language])

  function handleBuscar() {
    setPage(0)
    ejecutarBusqueda(filtros, 0)
  }

  function handleLimpiar() {
    setFiltros(FILTROS_INICIALES)
    setPage(0)
    ejecutarBusqueda(FILTROS_INICIALES, 0)
  }

  return (
    <div>
      <header style={{ marginBottom: '1.5rem' }}>
        <h1 style={{ color: 'var(--color-text)', fontSize: '1.4rem', fontWeight: 700, margin: 0 }}>
          {t('expedientes.title')}
        </h1>
        <p style={{ color: 'var(--color-text-muted)', fontSize: '0.88rem', marginTop: '0.3rem' }}>
          {t('expedientes.search')}
        </p>
      </header>

      <FiltrosExpedientes
        filtros={filtros}
        onChange={setFiltros}
        onSubmit={handleBuscar}
        onClear={handleLimpiar}
        loading={loading}
      />

      {error && (
        <p
          role="alert"
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            background: 'var(--color-danger-bg)',
            border: '1px solid rgba(248,113,113,0.35)',
            borderRadius: 'var(--radius-sm)',
            color: 'var(--color-danger)',
            fontSize: '0.85rem',
            padding: '0.6rem 0.75rem',
            marginBottom: '1rem',
          }}
        >
          <IconAlertCircle style={{ flexShrink: 0 }} />
          {error}
        </p>
      )}

      <TablaExpedientes expedientes={expedientes} loading={loading} />

      <Paginacion
        page={page}
        totalPages={totalPages}
        totalElements={totalElements}
        onPageChange={setPage}
        disabled={loading}
      />
    </div>
  )
}
