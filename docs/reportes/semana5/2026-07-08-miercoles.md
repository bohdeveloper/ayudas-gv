# Reporte diario — Miércoles 08/07/2026

## Completado

### `GET /v1/expedientes/{id}/ayuda`

- `ExpedienteRepository.buscarAyuda(id)`: primero resuelve `solicitud_ayuda` + el código de `tipo_ayuda` del expediente con un único `JOIN`; si no hay solicitud, `Optional.empty()` → 404 ("no tiene una solicitud de ayuda registrada")
- Con el código de tipo de ayuda en mano, `buscarDetalleAyuda(solicitudId, tipoAyudaCodigo)` hace un `switch` de Java 21 sobre `"ASIG_DISCAP" | "PAGO_NAC" | "PARTO_MULT"` y consulta la tabla de detalle correspondiente (`detalle_asignacion_discapacidad`, `detalle_nacimiento_adopcion`, `detalle_parto_multiple`), cada una con su propio `RowMapper` que construye el DTO sellado correspondiente
- Para `ASIG_DISCAP` el detalle incluye además el `causante` completo (`JOIN persona`), no solo su id — el frontend necesita nombre y fecha de nacimiento para mostrarlo sin una segunda petición

### Verificación end-to-end completa de las 4 secciones

- Reconstruido el backend con `docker compose up -d --build backend` y confirmado que compila sin errores (interfaces selladas de Java 21 incluidas)
- Recorridas por curl las 4 secciones (`generales`, `interesados`, `contacto`, `ayuda`) para los 5 expedientes de ejemplo, cubriendo las 3 modalidades de ayuda:
  - `EXP-2024-001`/`EXP-2024-004` (ASIG_DISCAP) → detalle con `gradoDiscapacidad`, `necesitaTerceraPersona`, `causante`
  - `EXP-2024-002`/`EXP-2024-005` (PAGO_NAC) → detalle con `supuesto`, `fechaHecho`, `ingresosAnuales`, `numeroHijosCargo`, con dos supuestos distintos (`FAMILIA_NUMEROSA` y `MONOPARENTAL`) para probar ambos textos
  - `EXP-2024-003` (PARTO_MULT) → detalle con `numeroNacidos`, `fechaHecho`
- Confirmado 404 en id inexistente y 401 sin token en las 4 rutas
- Revisado el JSON de cada respuesta: el campo `detalle` no lleva envoltorio adicional, solo el objeto con su `tipo` como discriminante — coincide carácter a carácter con el tipo unión `DetalleAyuda` del frontend, así que TypeScript podrá inferir el tipo correcto por *narrowing* sobre `detalle.tipo` sin ningún adaptador intermedio

## Pendiente (Semana 5)

- Frontend: cliente API, componentes de sección, vista de detalle con pestañas
- Enlace desde la tabla de búsqueda al detalle
- Internacionalización de las etiquetas nuevas

## Bloqueos

Ninguno.

---
*Hito del día: los 4 endpoints de detalle del expediente están completos, verificados end-to-end para las 3 modalidades de ayuda y protegidos con la misma seguridad que el resto de la API. Backend de la Semana 5 cerrado.*

<!-- evidencias
Respuesta de curl para GET /v1/expedientes/1/ayuda (ASIG_DISCAP) mostrando el detalle con causante embebido
Respuesta de curl para GET /v1/expedientes/2/ayuda y GET /v1/expedientes/5/ayuda (PAGO_NAC) con supuestos distintos
Respuesta de curl para GET /v1/expedientes/3/ayuda (PARTO_MULT)
-->
