# Arquitectura — Sistema de Gestión de Ayudas GV

## Estrategia: Monolito modular → Microservicios

El MVP (Fase 0) se construye como un **monolito modular** con bounded contexts limpios y sin dependencias cruzadas. A partir de la Fase 1, los módulos pueden extraerse como microservicios independientes sin reescribir la lógica.

---

## Módulos / Bounded Contexts

| Módulo | Responsabilidad | Fase |
|--------|----------------|------|
| `auth` | Login, emisión/validación de JWT, gestión de usuarios y roles | Fase 0 |
| `expedientes` | Expedientes, interesados, contacto, búsqueda y consulta por secciones | Fase 0 |
| `tramitacion` | Asignación de instructor y registro de trámites | Fase 1 |
| `commons` | Auditoría (AOP), logging, manejo de errores, DTOs comunes | Transversal |

---

## Diagrama de capas

```
┌─────────────────────────────────────────────────────┐
│                  Frontend (React + Vite)              │
│   Login │ Búsqueda │ Detalle expediente (pestañas)   │
└───────────────────────┬─────────────────────────────┘
                        │ HTTP/JSON + JWT Bearer
                        │ Accept-Language: es | eu
┌───────────────────────▼─────────────────────────────┐
│            Spring Boot — Monolito modular             │
│                                                       │
│  ┌──────────┐ ┌─────────────┐ ┌──────────────────┐  │
│  │   auth   │ │ expedientes │ │   tramitacion    │  │
│  │ /api/v1/ │ │  /api/v1/   │ │    /api/v1/      │  │
│  │  auth/*  │ │ expedientes │ │   tramites/*     │  │
│  └────┬─────┘ └──────┬──────┘ └────────┬─────────┘  │
│       │              │                  │             │
│  ┌────▼──────────────▼──────────────────▼──────────┐ │
│  │              commons                             │ │
│  │  AuditInterceptor │ GlobalExceptionHandler       │ │
│  │  CorrelationIdFilter │ DTOs comunes              │ │
│  └───────────────────────────────────────────────── ┘ │
└───────────────────────┬─────────────────────────────┘
                        │ JdbcTemplate / SQL parametrizado
┌───────────────────────▼─────────────────────────────┐
│                 PostgreSQL 15                         │
│          Flyway — migraciones versionadas             │
└─────────────────────────────────────────────────────┘
```

---

## Flujo de autenticación

```
1. POST /api/v1/auth/login  {username, password}
        ↓
   Spring Security valida credenciales (BCrypt)
        ↓
   JWT firmado (HS256, 24h) ← respuesta al cliente
        ↓
2. Cada petición protegida:
   Authorization: Bearer <token>
        ↓
   JwtAuthFilter extrae y valida el token
        ↓
   SecurityContext cargado con usuario + roles
        ↓
   @PreAuthorize / reglas de negocio se aplican
```

---

## Reglas de arquitectura

| Regla | Descripción |
|-------|-------------|
| Capas | `controller → service → repository`. Lógica de negocio SIEMPRE en `service` |
| DTO/Mapper | Nunca exponer entidades de BD en la API directamente |
| SQL explícito | `JdbcTemplate` / `NamedParameterJdbcTemplate`. Sin JPA/Hibernate |
| SQL seguro | Siempre parametrizado. Nunca concatenación de strings → anti-inyección SQL |
| Auditoría | Implementada via `@Aspect` (AOP) para no contaminar lógica de negocio |
| i18n | Sin literales hardcoded en frontend ni backend |
| Seguridad | Autorización en backend siempre, no solo en frontend |

---

## Endpoints Fase 0

| Método | Ruta | Auth requerida | Descripción |
|--------|------|---------------|-------------|
| `POST` | `/api/v1/auth/login` | Público | Obtener JWT |
| `GET` | `/api/v1/expedientes` | Todos los roles | Búsqueda con filtros + paginación |
| `GET` | `/api/v1/expedientes/{id}/generales` | Todos los roles | Datos generales del expediente |
| `GET` | `/api/v1/expedientes/{id}/interesados` | Todos los roles | Solicitante, representante, causantes |
| `GET` | `/api/v1/expedientes/{id}/contacto` | Todos los roles | Domicilio, teléfono, canal de aviso |
| `GET` | `/api/v1/expedientes/{id}/ayuda` | Todos los roles | Datos específicos del tipo de ayuda |

Todas las rutas bajo `/api/v1/...` — versionado explícito para compatibilidad futura.

## Endpoints Fase 1 (tramitación)

| Método | Ruta | Auth requerida | Descripción |
|--------|------|---------------|-------------|
| `GET` | `/api/v1/expedientes/{id}/tramites` | Todos los roles | Estado, instructor asignado e histórico de trámites |
| `POST` | `/api/v1/expedientes/{id}/tramites` | INSTRUCTOR · RESPONSABLE · ADMIN | Registrar un trámite y mover el estado |
| `PUT` | `/api/v1/expedientes/{id}/instructor` | RESPONSABLE · ADMIN | Asignar instructor (estado → ASIGNADO + traza) |
| `GET` | `/api/v1/instructores` | RESPONSABLE · ADMIN | Instructores asignables (alimenta el desplegable) |

**Doble capa de autorización.** El rol se comprueba con `@PreAuthorize` en el controlador
(barato, corta antes de tocar la BD); la pertenencia se comprueba en el servicio, que sí
necesita mirar el dato: *un INSTRUCTOR solo puede tramitar los expedientes asignados a él*
(spec.md §3, invariante 4). Un RESPONSABLE/ADMIN no tiene esa restricción.

**Códigos de estado y qué significa cada uno:**

| Código | Cuándo | Ejemplo |
|--------|--------|---------|
| `403` | El rol autoriza, pero no sobre ESTE expediente | Instructor no asignado (`OperacionNoPermitidaException`) |
| `404` | El recurso referenciado no existe | Expediente o tipo de trámite inexistente |
| `409` | La petición es correcta, el estado del recurso no la admite | Tramitar un expediente ya RESUELTO (`ReglaNegocioException`) |

**Modelo de tramitación** — cada trámite deja traza y, salvo la notificación, mueve el estado:

```
REGISTRO      → REGISTRADO
ASIGNACION    → ASIGNADO      (vía PUT /instructor, no como trámite suelto)
REVISION      → EN_REVISION
PROPUESTA     → EN_REVISION   (exige resultado FAVORABLE | DESFAVORABLE)
RESOLUCION    → RESUELTO      (+ sella fecha_resolucion; solo RESPONSABLE)
NOTIFICACION  → sin cambio de estado
```

## Endpoints de infraestructura

| Método | Ruta | Auth requerida | Descripción |
|--------|------|---------------|-------------|
| `GET` | `/api/actuator/health/liveness` | Público | ¿El proceso sigue vivo? Lo consulta el kubelet |
| `GET` | `/api/actuator/health/readiness` | Público | ¿Puede atender tráfico? Incluye el estado de la BD |

Son públicos porque los lanza el kubelet del nodo, que no tiene ni puede tener un JWT.
Devuelven solo `{"status":"UP"}` (`show-details: never`): no filtran configuración interna.

---

## Bilingüismo técnico

### Frontend
- `react-i18next` con ficheros `src/i18n/locales/es.json` y `eu.json`
- Idioma persiste en `localStorage` bajo la clave `idioma`
- Conmutación en caliente sin recargar la página
- Sin literales hardcoded; todas las cadenas via `t('clave')`

### Backend
- `MessageSource` con `messages_es.properties` y `messages_eu.properties`
- Idioma resuelto desde cabecera `Accept-Language` en cada petición
- Catálogos BD: `descripcion_es` / `descripcion_eu` en todas las tablas auxiliares
- El repositorio recibe el idioma como parámetro y lo usa en la consulta SQL

### Base de datos
- `usuario.idioma_preferido` — idioma de interfaz del usuario interno
- `datos_contacto.idioma_notificacion` — idioma de comunicación con el interesado (independiente)

---

## Evolución a microservicios (Fase 1+)

```
                         ┌──────────────────┐
  React Frontend ───────▶│  Gateway         │
                         │  (Spring Cloud)  │
                         │  Auth filter     │
                         └────────┬─────────┘
                                  │
               ┌──────────────────┼──────────────────┐
               ▼                  ▼                  ▼
      ┌─────────────────┐ ┌──────────────┐ ┌──────────────────┐
      │ ms-autenticacion│ │ms-expedientes│ │ ms-tramitacion   │
      │   JWT emitter   │ │ búsqueda +   │ │  trámites +      │
      │   user mgmt     │ │ consulta     │ │  notificaciones  │
      └─────────────────┘ └──────────────┘ └──────────────────┘
               │                  │                  │
       ┌───────┴──────────────────┴──────────────────┴───────┐
       │                    PostgreSQL 15                      │
       └──────────────────────────────────────────────────────┘
```

Los paquetes Java (`auth`, `expedientes`, `tramitacion`) están diseñados con fronteras limpias desde el inicio, sin dependencias cruzadas indebidas, para facilitar esta extracción.

---

## Decisiones técnicas tomadas

| Decisión | Opción elegida | Alternativa descartada | Por qué |
|----------|---------------|----------------------|---------|
| Acceso a datos | JdbcTemplate + SQL explícito | JPA/Hibernate | Requisito del proyecto para aprender SQL |
| Estado frontend | Zustand | Redux Toolkit | Más simple, menos boilerplate |
| JWT library | jjwt 0.12.5 (HS256) | Spring Security OAuth | Más control, proyecto educativo |
| i18n frontend | react-i18next | i18n-js | Estándar de React, mejor soporte |
| Bilingüismo BD | columnas `_es`/`_eu` | tabla de traducciones | Solución más simple para 2 idiomas fijos |
| Arquitectura inicial | Monolito modular | Microservicios desde el día 1 | El enunciado lo recomienda explícitamente |

---

## Convenciones del proyecto

- **Idioma del código:** Inglés para nombres de variables/clases, español para comentarios complejos
- **API base:** `/api/v1/...`
- **Paginación:** query params `page` (0-based), `size`, `sort`
- **i18n backend:** cabecera `Accept-Language: es` o `Accept-Language: eu`
- **i18n frontend:** clave `idioma` en localStorage
- **Contraseñas:** BCrypt strength 10, nunca en claro
- **SQL:** parametrizado siempre con `NamedParameterJdbcTemplate`, nunca concatenación
