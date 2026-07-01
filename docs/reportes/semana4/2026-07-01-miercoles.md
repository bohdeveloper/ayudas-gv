# Reporte diario — Miércoles 01/07/2026

## Completado

### `ExpedienteController` — `GET /api/v1/expedientes`

- Nuevo endpoint en `com.ayudasgv.expedientes.controller.ExpedienteController`, mapeado en `/v1/expedientes`
- Todos los filtros como `@RequestParam(required = false)`: `numeroExpediente`, `tipoAyuda`, `estado`, `documentoSolicitante`, `fechaDesde`/`fechaHasta` (`@DateTimeFormat(iso = DATE)`), `page`, `size`, `sort`
- Parámetro `Locale locale` inyectado directamente por Spring MVC — se resuelve automáticamente a partir de la cabecera `Accept-Language` sin configuración adicional (el `AcceptHeaderLocaleResolver` es el resolver por defecto de Spring Boot)
- Documentado en Swagger: `@Operation`, `@Parameter` por cada filtro con ejemplo, `@SecurityRequirement(name = "bearerAuth")`
- Ruta **no** está en `PUBLIC_PATHS` de `SecurityConfig` → cualquier usuario autenticado (con JWT válido) puede consultar; sin token, 401

### Pruebas manuales por `curl`

- Login con `admin`/`admin123` → token JWT válido
- `GET /v1/expedientes` sin filtros → 5 expedientes de ejemplo, correctos
- `GET /v1/expedientes?tipoAyuda=ASIG_DISCAP` → filtra correctamente a 2 resultados (`EXP-2024-001`, `EXP-2024-004`)
- `GET /v1/expedientes?documentoSolicitante=1234` → filtra a 1 resultado por coincidencia parcial de documento del solicitante (subconsulta `EXISTS` funcionando)
- Sin cabecera `Authorization` → 401, confirmado

### Compilación backend

- `mvn compile` sin errores tras añadir el controlador
- Revisado `GlobalExceptionHandler`: no requiere cambios — el `@RestControllerAdvice(basePackages = "com.ayudasgv")` ya cubre el nuevo controlador automáticamente

## Pendiente

- Verificar paginación y ordenación con distintos `size`/`sort`
- Verificar traducción es/eu de las descripciones de catálogo con `Accept-Language`
- Frontend: consumir el endpoint

## Bloqueos

Ninguno.

---
*Hito del día: endpoint de búsqueda de expedientes accesible y protegido — filtros básicos (número, tipo, estado, documento) verificados por curl.*

<!-- evidencias
Swagger UI — sección "Expedientes" con GET /v1/expedientes documentado, botón Authorize visible
Respuesta de curl para GET /v1/expedientes?tipoAyuda=ASIG_DISCAP
Respuesta 401 al llamar sin token
-->
