import { describe, it, expect, beforeEach } from 'vitest'
import axiosClient from './axiosClient'
import { useAuthStore } from '../store/authStore'
import type { LoginResponse } from '../types'

const LOGIN_RESPONSE: LoginResponse = {
  token: 'jwt-de-prueba',
  username: 'admin',
  nombre: 'Administrador Sistema',
  roles: ['ADMIN'],
  idiomaPreferido: 'es',
}

beforeEach(() => {
  useAuthStore.getState().logout()
})

/**
 * axiosClient no expone sus interceptores como funciones nombradas (son closures
 * internas de axiosClient.ts), así que se accede al handler registrado a través de
 * la propia instancia de axios — patrón habitual para probar interceptores sin
 * levantar un servidor HTTP real.
 */
describe('axiosClient — interceptor de petición', () => {
  it('adjunta la cabecera Authorization cuando hay sesión activa', async () => {
    useAuthStore.getState().setSession(LOGIN_RESPONSE)

    const fulfilled = axiosClient.interceptors.request.handlers![0]!.fulfilled!
    const config = await fulfilled({ headers: {} } as never)

    expect(config.headers.Authorization).toBe('Bearer jwt-de-prueba')
  })

  it('no adjunta Authorization sin sesión activa', async () => {
    const fulfilled = axiosClient.interceptors.request.handlers![0]!.fulfilled!
    const config = await fulfilled({ headers: {} } as never)

    expect(config.headers.Authorization).toBeUndefined()
  })
})

describe('axiosClient — interceptor de respuesta (token expirado)', () => {
  it('ante un 401 cierra la sesión activa', async () => {
    useAuthStore.getState().setSession(LOGIN_RESPONSE)
    expect(useAuthStore.getState().isAuthenticated).toBe(true)

    const rejected = axiosClient.interceptors.response.handlers![0]!.rejected!
    const error401 = { response: { status: 401 } }

    await expect(rejected(error401)).rejects.toBe(error401)

    expect(useAuthStore.getState().isAuthenticated).toBe(false)
    expect(useAuthStore.getState().token).toBeNull()
  })

  it('ante otros errores (p.ej. 404) NO cierra la sesión', async () => {
    useAuthStore.getState().setSession(LOGIN_RESPONSE)

    const rejected = axiosClient.interceptors.response.handlers![0]!.rejected!
    const error404 = { response: { status: 404 } }

    await expect(rejected(error404)).rejects.toBe(error404)

    expect(useAuthStore.getState().isAuthenticated).toBe(true)
  })
})
