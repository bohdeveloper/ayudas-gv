import '../../i18n'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import LoginPage from './LoginPage'
import { useAuthStore } from '../../store/authStore'
import * as authApi from '../../api/authApi'

vi.mock('../../api/authApi')

const mockedNavigate = vi.fn()
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom')
  return { ...actual, useNavigate: () => mockedNavigate }
})

beforeEach(() => {
  useAuthStore.getState().logout()
  mockedNavigate.mockClear()
  vi.mocked(authApi.login).mockReset()
})

function renderLoginPage() {
  return render(
    <MemoryRouter initialEntries={['/login']}>
      <LoginPage />
    </MemoryRouter>,
  )
}

describe('LoginPage', () => {
  it('con credenciales correctas guarda la sesión y navega a /expedientes', async () => {
    const user = userEvent.setup()
    vi.mocked(authApi.login).mockResolvedValue({
      token: 'jwt-de-prueba',
      username: 'admin',
      nombre: 'Administrador Sistema',
      roles: ['ADMIN'],
      idiomaPreferido: 'es',
    })

    renderLoginPage()

    await user.type(screen.getByLabelText('Usuario'), 'admin')
    await user.type(screen.getByLabelText('Contraseña'), 'admin123')
    await user.click(screen.getByRole('button', { name: 'Iniciar sesión' }))

    await waitFor(() => expect(mockedNavigate).toHaveBeenCalledWith('/expedientes', { replace: true }))
    expect(useAuthStore.getState().isAuthenticated).toBe(true)
    expect(useAuthStore.getState().username).toBe('admin')
  })

  it('con credenciales incorrectas muestra el error y no crea sesión', async () => {
    const user = userEvent.setup()
    vi.mocked(authApi.login).mockRejectedValue(new Error('401'))

    renderLoginPage()

    await user.type(screen.getByLabelText('Usuario'), 'admin')
    await user.type(screen.getByLabelText('Contraseña'), 'mala-clave')
    await user.click(screen.getByRole('button', { name: 'Iniciar sesión' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Credenciales incorrectas')
    expect(useAuthStore.getState().isAuthenticated).toBe(false)
    expect(mockedNavigate).not.toHaveBeenCalled()
  })
})
