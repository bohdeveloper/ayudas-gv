import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { LoginResponse, RolUsuario, Idioma } from '../types'

interface AuthState {
  token: string | null
  username: string | null
  nombre: string | null
  roles: RolUsuario[]
  idiomaPreferido: Idioma | null
  isAuthenticated: boolean
  setSession: (data: LoginResponse) => void
  logout: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      username: null,
      nombre: null,
      roles: [],
      idiomaPreferido: null,
      isAuthenticated: false,

      setSession: (data: LoginResponse) =>
        set({
          token: data.token,
          username: data.username,
          nombre: data.nombre,
          roles: data.roles,
          idiomaPreferido: data.idiomaPreferido,
          isAuthenticated: true,
        }),

      logout: () =>
        set({
          token: null,
          username: null,
          nombre: null,
          roles: [],
          idiomaPreferido: null,
          isAuthenticated: false,
        }),
    }),
    {
      name: 'auth-session',
      // Solo persistimos el token; el resto se recarga del token en futuros arranques
      partialize: (state) => ({
        token: state.token,
        username: state.username,
        nombre: state.nombre,
        roles: state.roles,
        idiomaPreferido: state.idiomaPreferido,
        isAuthenticated: state.isAuthenticated,
      }),
    },
  ),
)
