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
