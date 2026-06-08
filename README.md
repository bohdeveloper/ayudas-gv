# Sistema de Gestión y Tramitación de Expedientes de Ayudas

**Gobierno Vasco — Dirección de Prestaciones Familiares**

Aplicación web interna para la gestión y tramitación de expedientes de ayudas de carácter social (arts. 351–362 y 373 del TRLGSS).

---

## Stack

| Capa | Tecnología |
|------|-----------|
| Frontend | React 18 + Vite + TypeScript + react-i18next + Zustand |
| Backend | Java 17 + Spring Boot 3.x + Spring Security + JWT |
| Base de datos | PostgreSQL 15 + Flyway |
| Infraestructura | Docker Compose |
| API docs | Swagger UI (springdoc-openapi) |

## Requisitos previos

- JDK 17+
- Node 20+
- Docker + Docker Compose
- Maven 3.9+

## Arranque local

### 1. Base de datos

```bash
docker-compose up -d
```

### 2. Backend

```bash
cd backend
mvn spring-boot:run -Dspring.profiles.active=dev
```

Swagger UI: [http://localhost:8080/api/swagger-ui.html](http://localhost:8080/api/swagger-ui.html)

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Aplicación: [http://localhost:5173](http://localhost:5173)

---

## Estructura del proyecto

```
ayudas-gv/
├── backend/                       # Spring Boot — monolito modular
│   ├── src/main/java/com/ayudasgv/
│   │   ├── auth/                  # Autenticación y autorización (JWT)
│   │   ├── expedientes/           # Núcleo: expedientes, interesados, búsqueda
│   │   ├── tramitacion/           # Trámites (Fase 1)
│   │   └── commons/               # Auditoría, logging, errores, DTOs comunes
│   └── src/main/resources/
│       ├── db/migration/          # Migraciones Flyway (V1…Vn)
│       └── i18n/                  # messages_es.properties / messages_eu.properties
├── frontend/                      # React + Vite + TypeScript
│   └── src/
│       ├── api/                   # Capa de servicios HTTP (axios)
│       ├── components/            # Componentes reutilizables
│       ├── i18n/locales/          # es.json / eu.json
│       ├── pages/                 # Login, Expedientes, ExpedienteDetalle
│       ├── router/                # Rutas protegidas por rol
│       ├── store/                 # Estado global (Zustand)
│       └── types/                 # Tipos TypeScript compartidos
├── docs/                          # Documentación técnica
│   ├── assets/                    # Diagramas (ER, Gantt)
│   ├── arquitectura.md
│   ├── modelo-datos.md
│   └── fase0-planificacion.md
├── docker-compose.yml
└── README.md
```

---

## Fases del proyecto

| Fase | Objetivo | Estado |
|------|----------|--------|
| Fase 0 — MVP | Autenticación + búsqueda + consulta de expedientes por secciones | 🔄 En curso |
| Fase 1 — Tramitación | Registro de trámites y cambio de estado | ⏳ Pendiente |
| Fase 2 — Alta | Creación de nuevos expedientes y solicitudes | ⏳ Pendiente |
| Fase 3 — Resolución | Generación de documentos y notificaciones | ⏳ Pendiente |
| Fase 4 — Reglas de negocio | Validación de requisitos y cálculo de cuantías | ⏳ Pendiente |

## Roles

| Rol | Descripción |
|-----|-------------|
| `CONSULTOR` | Búsqueda y consulta de expedientes (solo lectura) |
| `INSTRUCTOR` | Tramitación de expedientes asignados |
| `RESPONSABLE` | Asignación de instructor y resolución |
| `ADMIN` | Gestión de usuarios, roles y catálogos |

## Ayudas gestionadas

| Código | Descripción | Modalidad |
|--------|-------------|-----------|
| `ASIG_DISCAP` | Asignación económica por hijo/menor a cargo con discapacidad | Periódico (mensual) |
| `PAGO_NAC` | Pago único por nacimiento o adopción en supuestos específicos | Pago único |
| `PARTO_MULT` | Pago único por parto o adopción múltiples | Pago único |

## Bilingüismo (es / eu)

La aplicación funciona íntegramente en castellano y euskera.

- **Interfaz**: selector de idioma persistente, conmutación en caliente sin recargar.
- **Mensajes backend**: resueltos por `Accept-Language` desde `messages_es/eu.properties`.
- **Catálogos BD**: columnas `descripcion_es` / `descripcion_eu` en todas las tablas auxiliares.
- **Idioma de notificación**: independiente del idioma de interfaz; se guarda por expediente.

## Usuarios de prueba (datos semilla)

| Usuario | Contraseña | Rol |
|---------|-----------|-----|
| `admin` | `admin123` | ADMIN + RESPONSABLE |
| `instructor1` | `admin123` | INSTRUCTOR |
| `consultor1` | `admin123` | CONSULTOR |

## Documentación adicional

- [Arquitectura](docs/arquitectura.md)
- [Modelo de datos](docs/modelo-datos.md)
- [Planificación Fase 0](docs/fase0-planificacion.md)
- [ROADMAP](ROADMAP.md)
