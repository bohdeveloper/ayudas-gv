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

// Páginas — se implementan en Semanas 3–5 de la Fase 0
// import Login from '../pages/Login/Login'
// import Expedientes from '../pages/Expedientes/Expedientes'
// import ExpedienteDetalle from '../pages/ExpedienteDetalle/ExpedienteDetalle'
// import ProtectedRoute from './ProtectedRoute'

export default function AppRouter() {
  return (
    <Routes>
      {/* Ruta raíz — redirige a expedientes o login según auth */}
      <Route path="/" element={<Navigate to="/expedientes" replace />} />

      {/* TODO Semana 3: <Route path="/login" element={<Login />} /> */}
      {/* TODO Semana 4: rutas protegidas por rol */}

      {/* Placeholder hasta implementar auth */}
      <Route path="*" element={<div style={{ padding: 32 }}>🚧 En construcción — Fase 0</div>} />
    </Routes>
  )
}
