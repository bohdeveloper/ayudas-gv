import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'
import ProtectedRoute from './ProtectedRoute'
import MainLayout from '../components/layout/MainLayout'
import LoginPage from '../pages/Login/LoginPage'
import ExpedientesPage from '../pages/Expedientes/ExpedientesPage'
import ExpedienteDetalle from '../pages/Expedientes/ExpedienteDetalle'

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
        <Route path="/expedientes/:id" element={<ExpedienteDetalle />} />
      </Route>

      {/* Raíz y catch-all */}
      <Route path="/" element={<Navigate to="/expedientes" replace />} />
      <Route path="*" element={<Navigate to="/expedientes" replace />} />
    </Routes>
  )
}
