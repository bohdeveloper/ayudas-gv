# Reporte diario — Miércoles 17/06/2026

## ✅ Completado

### Implementación de `JwtProvider`

- `JwtProvider.java` implementado con librería `jjwt` 0.12.5 (`io.jsonwebtoken`)
- Algoritmo HS384 (HMAC-SHA384): firma simétrica, la misma clave secreta firma y verifica
  - HS384 vs HS256: más bits de seguridad sin necesidad de clave asimétrica
  - Si se necesitara verificación externa sin compartir la clave, se usaría RS256
- Clave criptográfica construida desde `app.jwt.secret` de `application.yml` con `Keys.hmacShaKeyFor()`
- Claims del token: `subject` (username), `roles` (lista), `idioma`, `iat` (emisión), `exp` (caducidad)
- `isTokenValid()` captura `JwtException` e `IllegalArgumentException` → devuelve `false` (no propaga)
- `parseClaims()` llama a `verifyWith(secretKey)` **antes** de leer el payload — la firma siempre se verifica

### Endpoint de login (`POST /api/v1/auth/login`)

- DTOs creados:
  - `LoginRequest` record con `@NotBlank` en ambos campos
  - `LoginResponse` record (token, username, nombreCompleto, roles, idiomaPreferido)
- `AuthService.login()` completo: busca usuario → verifica BCrypt → genera JWT → devuelve `LoginResponse`
- Principio de seguridad: mismo mensaje `"Credenciales incorrectas"` tanto si el usuario no existe como si la contraseña es incorrecta (evita *user enumeration*)
- `AuthController`: `@PostMapping("/login")`, `@Valid @RequestBody`, anotaciones Swagger `@Operation` + `@ApiResponse`
- Probado desde Swagger UI: `POST /api/v1/auth/login` con `{ "username": "admin", "password": "admin" }` → 200 con JWT

## 🔜 Pendiente

- Implementar `JwtAuthenticationFilter` para validar el token en cada petición
- Crear endpoint protegido de prueba

## 🚧 Bloqueos

Ninguno.

---
*Hito del día: Login funcional — `POST /api/v1/auth/login` devuelve un JWT firmado con credenciales válidas.*

<!-- evidencias
Swagger UI mostrando POST /api/v1/auth/login con el cuerpo de petición y la respuesta 200 con el token JWT
JwtProvider.java abierto mostrando el método generateToken() con los claims del token
-->
