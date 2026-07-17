import '../../i18n'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import ExpedienteDetalle from './ExpedienteDetalle'
import * as detalleApi from '../../api/expedienteDetalleApi'
import type { Persona, SeccionGenerales, SeccionInteresados } from '../../types'

vi.mock('../../api/expedienteDetalleApi')

const GENERALES: SeccionGenerales = {
  id: 1,
  numeroExpediente: 'EXP-2024-001',
  tipoAyuda: { codigo: 'ASIG_DISCAP', descripcion: 'Asignación por discapacidad' },
  estado: { codigo: 'REGISTRADO', descripcion: 'Registrado' },
  fechaRegistro: '2026-06-01T10:00:00',
  fechaResolucion: undefined,
  instructorNombre: 'Ane Etxeberria',
}

const SOLICITANTE: Persona = {
  id: 1,
  tipoDocumento: 'DNI',
  numeroDocumento: '12345678A',
  nombre: 'Iker',
  apellido1: 'Aguirre',
  apellido2: 'Etxeberria',
  fechaNacimiento: '1990-01-01',
}

const INTERESADOS: SeccionInteresados = {
  solicitante: SOLICITANTE,
  representante: undefined,
  causantes: [],
}

beforeEach(() => {
  vi.mocked(detalleApi.obtenerGenerales).mockReset().mockResolvedValue(GENERALES)
  vi.mocked(detalleApi.obtenerInteresados).mockReset().mockResolvedValue(INTERESADOS)
  vi.mocked(detalleApi.obtenerContacto).mockReset().mockResolvedValue({} as never)
  vi.mocked(detalleApi.obtenerAyuda).mockReset().mockResolvedValue({} as never)
})

function renderDetalle(id = '1') {
  return render(
    <MemoryRouter initialEntries={[`/expedientes/${id}`]}>
      <Routes>
        <Route path="/expedientes/:id" element={<ExpedienteDetalle />} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('ExpedienteDetalle', () => {
  it('al montar, carga la sección "Datos generales" y muestra el número de expediente', async () => {
    renderDetalle()

    expect(detalleApi.obtenerGenerales).toHaveBeenCalledWith(1)
    expect(await screen.findByRole('heading', { name: 'EXP-2024-001' })).toBeInTheDocument()
    // "Registrado" aparece dos veces: en el badge de cabecera y en el campo "Estado" del panel
    expect(screen.getAllByText('Registrado').length).toBeGreaterThan(0)
  })

  it('al cambiar de pestaña, carga la sección correspondiente solo la primera vez', async () => {
    const user = userEvent.setup()
    renderDetalle()

    await screen.findByRole('heading', { name: 'EXP-2024-001' })
    expect(detalleApi.obtenerInteresados).not.toHaveBeenCalled()

    await user.click(screen.getByRole('tab', { name: 'Interesados' }))

    await waitFor(() => expect(detalleApi.obtenerInteresados).toHaveBeenCalledWith(1))
    expect(await screen.findByText('Iker Aguirre Etxeberria')).toBeInTheDocument()

    // La pestaña "Datos generales" queda oculta (no desmontada) al cambiar de tab
    expect(document.getElementById('tabpanel-generales')).toHaveAttribute('hidden')
  })

  it('si la sección falla, muestra un mensaje de error accesible', async () => {
    vi.mocked(detalleApi.obtenerGenerales).mockRejectedValue(new Error('500'))

    renderDetalle()

    expect(await screen.findByRole('alert')).toHaveTextContent('No se pudo cargar esta sección')
  })
})
