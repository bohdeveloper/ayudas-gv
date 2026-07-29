import '../../../i18n'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import SeccionTramitacionPanel from './SeccionTramitacionPanel'
import * as tramitacionApi from '../../../api/tramitacionApi'
import { useAuthStore } from '../../../store/authStore'
import type { RolUsuario, SeccionTramitacion } from '../../../types'

vi.mock('../../../api/tramitacionApi')

const SECCION: SeccionTramitacion = {
  estado: { codigo: 'ASIGNADO', descripcion: 'Asignado a instructor' },
  instructorId: 2,
  instructorNombre: 'Ane Etxeberria',
  tramites: [
    {
      id: 2,
      tipoTramite: { codigo: 'ASIGNACION', descripcion: 'Asignación de instructor' },
      fecha: '2024-01-11T12:30:00',
      username: 'admin',
      usuarioNombre: 'Administrador Sistema',
      observaciones: 'Asignado por carga de trabajo',
    },
    {
      id: 1,
      tipoTramite: { codigo: 'REGISTRO', descripcion: 'Registro de entrada' },
      fecha: '2024-01-10T09:05:00',
      username: 'admin',
      usuarioNombre: 'Administrador Sistema',
    },
  ],
}

/** El panel decide qué enseña a partir de los roles del JWT (useAuthStore). */
function sesionCon(roles: RolUsuario[]) {
  useAuthStore.setState({ roles, isAuthenticated: true, username: 'usuario-prueba' })
}

function renderPanel(data: SeccionTramitacion = SECCION, onCambio = vi.fn()) {
  render(<SeccionTramitacionPanel expedienteId={1} data={data} onCambio={onCambio} />)
  return onCambio
}

beforeEach(() => {
  vi.mocked(tramitacionApi.obtenerInstructores).mockReset().mockResolvedValue([
    { id: 2, username: 'instructor1', nombreCompleto: 'Ane Etxeberria' },
    { id: 6, username: 'instructor2', nombreCompleto: 'Miren Agirre' },
  ])
  vi.mocked(tramitacionApi.registrarTramite).mockReset().mockResolvedValue({
    id: 3,
    tipoTramite: { codigo: 'REVISION', descripcion: 'Revisión / subsanación' },
    fecha: '2026-07-29T10:00:00',
    username: 'instructor1',
    usuarioNombre: 'Ane Etxeberria',
  })
  vi.mocked(tramitacionApi.asignarInstructor).mockReset().mockResolvedValue(SECCION)
  sesionCon(['CONSULTOR'])
})

describe('SeccionTramitacionPanel', () => {
  it('muestra el histórico del más reciente al más antiguo, con quién y cuándo', () => {
    renderPanel()

    const hitos = screen.getAllByRole('listitem')
    expect(hitos).toHaveLength(2)
    expect(hitos[0]).toHaveTextContent('Asignación de instructor')
    expect(hitos[1]).toHaveTextContent('Registro de entrada')
    expect(hitos[0]).toHaveTextContent('Administrador Sistema')
  })

  it('muestra el instructor asignado y el estado actual', () => {
    renderPanel()

    expect(screen.getByText('Ane Etxeberria')).toBeInTheDocument()
    expect(screen.getByText('Asignado a instructor')).toBeInTheDocument()
  })

  it('indica cuando el expediente no tiene instructor asignado', () => {
    renderPanel({ ...SECCION, instructorId: undefined, instructorNombre: undefined })

    expect(screen.getByText('Sin asignar')).toBeInTheDocument()
  })

  it('a un CONSULTOR no le ofrece registrar trámites ni asignar instructor', () => {
    renderPanel()

    expect(screen.queryByRole('button', { name: /Registrar/ })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /Asignar/ })).not.toBeInTheDocument()
  })

  it('a un INSTRUCTOR le ofrece registrar, pero no asignar ni resolver', async () => {
    sesionCon(['INSTRUCTOR'])
    renderPanel()

    expect(screen.getByRole('button', { name: /Registrar/ })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /^Asignar$/ })).not.toBeInTheDocument()

    // RESOLUCION está reservada al responsable: no aparece en el desplegable
    const tipos = screen.getByLabelText('Tipo de trámite')
    expect(tipos).not.toHaveTextContent('Resolución')
  })

  it('a un RESPONSABLE le ofrece asignar instructor y resolver', async () => {
    sesionCon(['RESPONSABLE'])
    renderPanel()

    await waitFor(() => expect(tramitacionApi.obtenerInstructores).toHaveBeenCalled())
    expect(screen.getByRole('button', { name: 'Asignar' })).toBeInTheDocument()
    expect(screen.getByLabelText('Tipo de trámite')).toHaveTextContent('Resolución')
  })

  it('registra el trámite con el tipo elegido y avisa al padre para refrescar', async () => {
    sesionCon(['INSTRUCTOR'])
    const onCambio = renderPanel()
    const user = userEvent.setup()

    await user.selectOptions(screen.getByLabelText('Tipo de trámite'), 'REVISION')
    await user.type(screen.getByLabelText('Observaciones'), 'Falta el certificado')
    await user.click(screen.getByRole('button', { name: /Registrar/ }))

    await waitFor(() =>
      expect(tramitacionApi.registrarTramite).toHaveBeenCalledWith(1, {
        tipoTramite: 'REVISION',
        resultado: undefined,
        observaciones: 'Falta el certificado',
      }),
    )
    expect(onCambio).toHaveBeenCalled()
  })

  it('muestra el motivo que devuelve el backend cuando rechaza el trámite', async () => {
    sesionCon(['INSTRUCTOR'])
    vi.mocked(tramitacionApi.registrarTramite).mockRejectedValue({
      response: { data: { message: 'Solo puede tramitar los expedientes que tiene asignados' } },
    })
    renderPanel()
    const user = userEvent.setup()

    await user.click(screen.getByRole('button', { name: /Registrar/ }))

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Solo puede tramitar los expedientes que tiene asignados',
    )
  })

  it('avisa de que un expediente resuelto solo admite la notificación', () => {
    sesionCon(['RESPONSABLE'])
    renderPanel({ ...SECCION, estado: { codigo: 'RESUELTO', descripcion: 'Resuelto' } })

    expect(
      screen.getByText('El expediente está resuelto: solo admite la notificación al interesado'),
    ).toBeInTheDocument()
    // Un expediente cerrado ya no se reasigna
    expect(screen.queryByRole('button', { name: 'Asignar' })).not.toBeInTheDocument()
  })

  it('asigna el instructor elegido', async () => {
    sesionCon(['RESPONSABLE'])
    const onCambio = renderPanel()
    const user = userEvent.setup()

    await waitFor(() => expect(tramitacionApi.obtenerInstructores).toHaveBeenCalled())
    await user.selectOptions(screen.getByLabelText('Instructor'), '6')
    await user.click(screen.getByRole('button', { name: 'Asignar' }))

    await waitFor(() => expect(tramitacionApi.asignarInstructor).toHaveBeenCalledWith(1, 6))
    expect(onCambio).toHaveBeenCalled()
  })
})
