import '../../i18n'
import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import TablaExpedientes from './TablaExpedientes'
import type { ExpedienteResumen } from '../../types'

const EXPEDIENTES: ExpedienteResumen[] = [
  {
    id: 1,
    numeroExpediente: 'EXP-2024-001',
    tipoAyuda: { codigo: 'ASIG_DISCAP', descripcion: 'Asignación por discapacidad' },
    estado: { codigo: 'REGISTRADO', descripcion: 'Registrado' },
    fechaRegistro: '2026-06-01T10:00:00',
    instructorNombre: 'Ane Etxeberria',
  },
  {
    id: 2,
    numeroExpediente: 'EXP-2024-002',
    tipoAyuda: { codigo: 'PAGO_NAC', descripcion: 'Pago único por nacimiento' },
    estado: { codigo: 'RESUELTO', descripcion: 'Resuelto' },
    fechaRegistro: '2026-06-02T10:00:00',
  },
]

function renderTabla(props: { expedientes: ExpedienteResumen[]; loading: boolean }) {
  return render(
    <MemoryRouter>
      <TablaExpedientes {...props} />
    </MemoryRouter>,
  )
}

describe('TablaExpedientes', () => {
  it('muestra una fila por expediente con su número, tipo y estado', () => {
    renderTabla({ expedientes: EXPEDIENTES, loading: false })

    expect(screen.getByRole('link', { name: 'EXP-2024-001' })).toHaveAttribute('href', '/expedientes/1')
    expect(screen.getByText('Asignación por discapacidad')).toBeInTheDocument()
    expect(screen.getByText('Registrado')).toBeInTheDocument()
    expect(screen.getByText('Ane Etxeberria')).toBeInTheDocument()
  })

  it('muestra un guion cuando no hay instructor asignado', () => {
    renderTabla({ expedientes: EXPEDIENTES, loading: false })

    expect(screen.getByRole('link', { name: 'EXP-2024-002' })).toBeInTheDocument()
    expect(screen.getByText('—')).toBeInTheDocument()
  })

  it('sin resultados y sin cargar, muestra el estado vacío', () => {
    renderTabla({ expedientes: [], loading: false })

    expect(screen.getByText('No se encontraron expedientes con los filtros indicados')).toBeInTheDocument()
  })

  it('mientras carga, no muestra filas de datos ni el estado vacío', () => {
    renderTabla({ expedientes: [], loading: true })

    expect(screen.queryByRole('link')).not.toBeInTheDocument()
    expect(screen.queryByText('No se encontraron expedientes con los filtros indicados')).not.toBeInTheDocument()
  })
})
