# Sistema de Gestión y Tramitación de Expedientes de Ayudas

**Gobierno Vasco — Dirección de Prestaciones Familiares**

Aplicación web interna para la gestión y tramitación de expedientes de ayudas de carácter social (arts. 351–362 y 373 del TRLGSS).

---

## Stack

| Capa | Tecnología |
|------|-----------|
| Frontend | React 18 + Vite + TypeScript 6 + react-i18next + Zustand |
| Backend | Java 21 + Spring Boot 3.5.14 + Spring Security + JWT |
| Base de datos | PostgreSQL 15 + Flyway |
| Infraestructura | Docker Compose (3 contenedores) |
| API docs | Swagger UI (springdoc-openapi 2.5) |

---

## Arranque rápido — Docker (recomendado)

Un solo comando levanta los tres servicios (BD + backend + frontend):

```bash
docker compose up --build
```

| Servicio | URL |
|----------|-----|
| Frontend (React) | http://localhost:3000 |
| Backend (API) | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/api/swagger-ui.html |
| PostgreSQL | localhost:5432 |

Para parar: `docker compose down`
Para parar y borrar datos de BD: `docker compose down -v`

---

## Arranque local (desarrollo sin Docker)

Requiere: **JDK 21**, **Node 20**, **Docker** (solo para la BD), **Maven 3.9**

### 1. Base de datos

```bash
docker compose up -d postgres
```

### 2. Backend

```bash
cd backend
mvn spring-boot:run -Dspring.profiles.active=dev
```

Swagger UI: http://localhost:8080/api/swagger-ui.html

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Aplicación: http://localhost:5173

> El frontend en dev (puerto 5173) proxea automáticamente `/api/*` al backend (8080) — configurado en `vite.config.ts`.

---

## Estructura del proyecto

```
ayudas-gv/
├── backend/                       # Spring Boot — monolito modular
│   ├── Dockerfile                 # Multi-stage: Maven build → JRE runtime
│   ├── pom.xml                    # Spring Boot 3.5.14, Java 21
│   └── src/main/
│       ├── java/com/ayudasgv/
│       │   ├── AyudasGvApplication.java
│       │   ├── config/            # SecurityConfig, OpenApiConfig
│       │   ├── auth/              # JWT, login (Semana 2)
│       │   ├── expedientes/       # Búsqueda y consulta (Semanas 4-5)
│       │   ├── tramitacion/       # Trámites (Fase 1)
│       │   └── commons/           # Auditoría AOP, errores, logging
│       └── resources/
│           ├── application.yml         # Config base (usa variables de entorno)
│           ├── application-dev.yml     # Override para desarrollo local
│           ├── db/migration/           # V1…V4 — migraciones Flyway
│           └── i18n/                   # messages_es / messages_eu
├── frontend/                      # React + Vite + TypeScript
│   ├── Dockerfile                 # Multi-stage: Node build → Nginx
│   ├── nginx.conf                 # Sirve la SPA y proxea /api
│   └── src/
│       ├── api/                   # Capa HTTP (axios)
│       ├── components/            # Componentes reutilizables
│       ├── i18n/locales/          # es.json / eu.json
│       ├── pages/                 # Login, Expedientes, ExpedienteDetalle
│       ├── router/                # Rutas (públicas y protegidas)
│       ├── store/                 # Estado global (Zustand)
│       └── types/                 # Tipos TypeScript del dominio
├── docs/
│   ├── tecnologias.md             # Explicación profunda del ecosistema técnico
│   ├── arquitectura.md            # Diagramas de capas y módulos
│   ├── modelo-datos.md            # Esquema ER completo
│   ├── fase0-planificacion.md     # Plan detallado semana a semana
│   └── reportes/                  # Reportes diarios de desarrollo
├── docker-compose.yml             # Orquestación: postgres + backend + frontend
├── PROJECT_MEMORY.md              # Estado actual y log de sesiones
└── ROADMAP.md                     # Fases, semanas, hitos y seguimiento de ramas
```

---

## Fases del proyecto

| Fase | Objetivo | Estado |
|------|----------|--------|
| **Fase 0 — MVP** | Autenticación + búsqueda + consulta de expedientes por secciones | 🔄 S1 ✅ · S2–S6 pendientes |
| Fase 1 — Tramitación | Registro de trámites y cambio de estado | ⏳ Pendiente |
| Fase 2 — Alta | Creación de nuevos expedientes y solicitudes | ⏳ Pendiente |
| Fase 3 — Resolución | Generación de documentos y notificaciones | ⏳ Pendiente |
| Fase 4 — Reglas de negocio | Validación de requisitos y cálculo de cuantías | ⏳ Pendiente |

---

## Roles

| Rol | Descripción |
|-----|-------------|
| `CONSULTOR` | Búsqueda y consulta de expedientes (solo lectura) |
| `INSTRUCTOR` | Tramitación de expedientes asignados |
| `RESPONSABLE` | Asignación de instructor y resolución |
| `ADMIN` | Gestión de usuarios, roles y catálogos |

---

## Ayudas gestionadas

| Código | Descripción | Modalidad |
|--------|-------------|-----------|
| `ASIG_DISCAP` | Asignación económica por hijo/menor a cargo con discapacidad | Periódico (mensual) |
| `PAGO_NAC` | Pago único por nacimiento o adopción en supuestos específicos | Pago único |
| `PARTO_MULT` | Pago único por parto o adopción múltiples | Pago único |

---

## Bilingüismo (es / eu)

La aplicación funciona íntegramente en castellano y euskera:

- **Interfaz**: selector de idioma persistente, conmutación en caliente sin recargar.
- **Mensajes backend**: resueltos por cabecera `Accept-Language` desde `messages_es/eu.properties`.
- **Catálogos BD**: columnas `descripcion_es` / `descripcion_eu` en todas las tablas auxiliares.
- **Idioma de notificación**: independiente del idioma de interfaz; se guarda por expediente.

---

## Usuarios de prueba (datos semilla)

| Usuario | Contraseña | Rol |
|---------|-----------|-----|
| `admin` | `admin123` | ADMIN + RESPONSABLE |
| `instructor1` | `admin123` | INSTRUCTOR |
| `consultor1` | `admin123` | CONSULTOR |

---

## Documentación adicional

| Documento | Descripción |
|-----------|-------------|
| [Tecnologías y ecosistema](docs/tecnologias.md) | Explicación profunda del stack, Docker y comunicaciones |
| [Arquitectura](docs/arquitectura.md) | Diagramas de capas, módulos y endpoints |
| [Modelo de datos](docs/modelo-datos.md) | Esquema ER completo por contextos |
| [Planificación Fase 0](docs/fase0-planificacion.md) | Detalle semana a semana |
| [ROADMAP](ROADMAP.md) | Fases, hitos y seguimiento de ramas |
| [Reportes diarios](docs/reportes/) | Log de progreso por sesión |
