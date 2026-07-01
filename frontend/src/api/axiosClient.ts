import axios from 'axios'
import { useAuthStore } from '../store/authStore'
import i18n from '../i18n'

const axiosClient = axios.create({
  baseURL: '/api/v1',
  headers: { 'Content-Type': 'application/json' },
})

// Interceptor de petición: adjunta el JWT Bearer y el idioma activo
axiosClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  // Accept-Language: el backend lo usa para traducir descripciones de catálogo (tipo de ayuda, estado)
  config.headers['Accept-Language'] = i18n.language?.slice(0, 2) ?? 'es'
  return config
})

// Interceptor de respuesta: ante un 401 limpia la sesión para redirigir a /login
axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      useAuthStore.getState().logout()
    }
    return Promise.reject(error)
  },
)

export default axiosClient
