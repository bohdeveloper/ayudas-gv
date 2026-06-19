/**
 * Enrutador principal de la aplicación React.
 *
 * Usa React Router v6 (BrowserRouter ya configurado en main.tsx) para mapear
 * URLs del navegador a componentes de página. Aquí se define la estructura de
 * navegación completa de la SPA (Single Page Application).
 *
 * Estado actual (esqueleto Semana 1):
 *   - Solo existe la redirección raíz y un catch-all de "en construcción".
 *   - Las páginas reales se añadirán en Semanas 3–5.
 *
 * Semana 3 añadirá:
 *   - Ruta pública /login (pantalla de inicio de sesión).
 *   - Componente ProtectedRoute que redirige a /login si no hay token JWT.
 *   - Rutas privadas /expedientes y /expedientes/:id.
 */
import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'
import ProtectedRoute from './ProtectedRoute'
import MainLayout from '../components/layout/MainLayout'
import LoginPage from '../pages/Login/LoginPage'
import ExpedientesPage from '../pages/Expedientes/ExpedientesPage'

export default function AppRouter() {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated)

  return (
    <Routes>
      {/* Ruta pública: login — si ya autenticado, redirige a expedientes */}
      <Route
        path="/login"
        element={isAuthenticated ? <Navigate to="/expedientes" replace /> : <LoginPage />}
      />

      {/* Rutas privadas: dentro del layout principal */}
      <Route
        element={
          <ProtectedRoute>
            <MainLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/expedientes" element={<ExpedientesPage />} />
        {/* Semana 5: <Route path="/expedientes/:id" element={<ExpedienteDetalle />} /> */}
      </Route>

      {/* Raíz y catch-all */}
      <Route path="/" element={<Navigate to="/expedientes" replace />} />
      <Route path="*" element={<Navigate to="/expedientes" replace />} />
    </Routes>
  )
}
