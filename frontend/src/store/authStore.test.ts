import { describe, it, expect, beforeEach } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import { useAuthStore } from './authStore'
import type { LoginResponse } from '../types'

const LOGIN_RESPONSE: LoginResponse = {
  token: 'jwt-de-prueba',
  username: 'admin',
  nombre: 'Administrador Sistema',
  roles: ['ADMIN', 'RESPONSABLE'],
  idiomaPreferido: 'es',
}

// Zustand con persist() escribe en localStorage — jsdom lo proporciona, pero
// reseteamos el store entre pruebas para que no se contaminen entre sí.
beforeEach(() => {
  useAuthStore.getState().logout()
})

describe('useAuthStore', () => {
  it('empieza sin sesión (isAuthenticated=false, sin token)', () => {
    const { result } = renderHook(() => useAuthStore())

    expect(result.current.isAuthenticated).toBe(false)
    expect(result.current.token).toBeNull()
  })

  it('setSession (login) guarda el token y los datos del usuario', () => {
    const { result } = renderHook(() => useAuthStore())

    act(() => result.current.setSession(LOGIN_RESPONSE))

    expect(result.current.isAuthenticated).toBe(true)
    expect(result.current.token).toBe('jwt-de-prueba')
    expect(result.current.username).toBe('admin')
    expect(result.current.roles).toEqual(['ADMIN', 'RESPONSABLE'])
    expect(result.current.idiomaPreferido).toBe('es')
  })

  it('logout limpia la sesión por completo', () => {
    const { result } = renderHook(() => useAuthStore())

    act(() => result.current.setSession(LOGIN_RESPONSE))
    expect(result.current.isAuthenticated).toBe(true)

    act(() => result.current.logout())

    expect(result.current.isAuthenticated).toBe(false)
    expect(result.current.token).toBeNull()
    expect(result.current.username).toBeNull()
    expect(result.current.roles).toEqual([])
  })
})
