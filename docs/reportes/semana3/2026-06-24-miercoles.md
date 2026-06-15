# Reporte diario — Miércoles 24/06/2026

## ✅ Completado

### Rutas protegidas — `ProtectedRoute.tsx`

- Creado `frontend/src/router/ProtectedRoute.tsx`: componente wrapper que lee `isAuthenticated` del store Zustand
- Si no autenticado: `<Navigate to="/login" state={{ from: location.pathname }} replace />` — guarda la ruta intentada para redirigir tras el login exitoso
- Si autenticado: renderiza `{children}` directamente
- Patrón elegido vs alternativa de `<Outlet />` propio: al pasar `children` se puede envolver cualquier componente, incluido el `MainLayout` que tiene su propio `<Outlet />`

### AppRouter actualizado con rutas definitivas

- Ruta pública `/login`: si ya autenticado, redirige a `/expedientes` para evitar que el usuario logado vea el formulario de login
- Ruta de layout (sin `path`): `<ProtectedRoute><MainLayout /></ProtectedRoute>` — cualquier ruta hija hereda protección y layout en una sola declaración
- Ruta privada `/expedientes`: `<ExpedientesPage />` placeholder (implementación en Semana 4)
- `catch-all`: redirige a `/expedientes` (que a su vez redirigirá a `/login` si no hay sesión)

### Redirección automática en 401

- El interceptor de respuesta de `axiosClient.ts` (implementado el lunes) llama a `logout()` del store ante cualquier 401 del backend
- Como `ProtectedRoute` lee `isAuthenticated` reactivamente, al limpiar el store React re-renderiza y `ProtectedRoute` redirige a `/login` automáticamente — sin lógica adicional
- Esto cubre el caso de token expirado: el backend devuelve 401, el interceptor limpia la sesión, React redirige

### Placeholder `ExpedientesPage.tsx`

- Creada `frontend/src/pages/Expedientes/ExpedientesPage.tsx` como pantalla temporal
- Muestra el nombre y roles del usuario logado (confirma que el store funciona en una ruta privada)
- Texto indicativo: "Búsqueda y tabla de expedientes — Semana 4"

## 🔜 Pendiente

- i18n: verificar que todos los componentes usan `useTranslation()` correctamente
- Selector de idioma con conmutación en caliente

## 🚧 Bloqueos

Ninguno.

---
*Hito del día: rutas protegidas activas — sin token, cualquier ruta privada redirige a /login; con 401 del backend, la sesión se limpia automáticamente.*

<!-- evidencias
Navegador mostrando redirección automática a /login al acceder a /expedientes sin sesión
Navegador mostrando /expedientes con el nombre del usuario tras login exitoso
Network DevTools mostrando el header Authorization: Bearer en las peticiones protegidas
-->
