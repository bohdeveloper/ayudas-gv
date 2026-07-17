# Reporte diario — Miércoles 15/07/2026

## Completado

### Pruebas unitarias de la capa de servicios (JUnit 5 + Mockito)

- `AuthServiceTest` — los tres caminos que documenta el propio Javadoc de `AuthService`: login con credenciales válidas (devuelve token + datos de usuario), usuario inexistente (`BadCredentialsException`, sin llegar a generar token) y contraseña incorrecta (mismo resultado, mismo mensaje — para no revelar si el usuario existe). Colaboradores (`UsuarioRepository`, `PasswordEncoder`, `JwtProvider`) simulados con `@Mock`/`@InjectMocks`, sin base de datos real
- `ExpedienteServiceTest` (11 casos) — normalización de paginación (`page`/`size` por defecto, `size` limitado a 100), resolución de columna de catálogo según idioma (`es`/`eu`), y los caminos 404 de cada sección del detalle (`obtenerGenerales`, `obtenerInteresados`, `obtenerContacto`, `obtenerAyuda`) verificando además que, cuando el expediente no existe, el repositorio de la sección específica ni se llega a consultar (`verify(..., never())`)

### Verificación

- `mvn test`: 14 pruebas nuevas en verde (3 + 11), sin tocar Postgres — toda la lógica de negocio queda cubierta de forma aislada y rápida (~7s las de `AuthService`, ~0,5s las de `ExpedienteService`)

## Pendiente (Semana 6)

- Prueba de controlador (`@WebMvcTest`/MockMvc) en backend
- Pruebas de componentes y hooks en frontend (Vitest + Testing Library)
- Repaso de autorización por rol
- Swagger completo, README verificado y demo final

## Bloqueos

Ninguno.

---
*Hito del día: la lógica de negocio de autenticación y de consulta de expedientes queda cubierta por 14 pruebas unitarias aisladas (sin BD), verificando tanto los caminos felices como los de error (credenciales inválidas, expediente inexistente).*

<!-- evidencias
Salida de `mvn test`: Tests run: 14, Failures: 0, Errors: 0 (AuthServiceTest + ExpedienteServiceTest)
-->
