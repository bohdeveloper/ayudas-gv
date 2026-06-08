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
