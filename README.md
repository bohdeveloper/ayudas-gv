# Sistema de Gestión y Tramitación de Expedientes de Ayudas

**Gobierno Vasco — Dirección de Prestaciones Familiares**

Aplicación web interna para la gestión y tramitación de expedientes de ayudas de carácter social (arts. 351–362 y 373 del TRLGSS).

> **Estado:** Fase 0 (MVP) y **Fase K8s** completadas; **Fase 1 — Tramitación** completada (2026-07-29).
> El despliegue en Kubernetes está en [`k8s/`](k8s/) con su [guía paso a paso](docs/docker-k8s/kubernetes-aprendizaje.md).
> El repo sigue **Spec-Driven Development**: la fuente de verdad es [spec.md](spec.md) (memoria) + [plan.md](plan.md) (trabajo).

---

## Stack

| Capa | Tecnología |
|------|-----------|
| Frontend | React 18 + Vite + TypeScript 6 + react-i18next + Zustand |
| Backend | Java 21 + Spring Boot 4.1.1 + Spring Security 7 + JWT |
| Base de datos | PostgreSQL 15 + Flyway |
| Infraestructura | Docker Compose (3 contenedores) |
| API docs | Swagger UI (springdoc-openapi 3.1.1) |

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
| PostgreSQL | localhost:5433 (puerto externo; 5432 reservado para PostgreSQL local) |

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

## Pruebas

```bash
# Backend — JUnit 5 + Mockito + @WebMvcTest + @JdbcTest con H2 (104 pruebas)
cd backend
mvn test

# Frontend — Vitest + Testing Library (26 pruebas)
cd frontend
npm test -- --run
```

---

## Estructura del proyecto

```
ayudas-gv/
├── backend/                       # Spring Boot — monolito modular
│   ├── Dockerfile                 # Multi-stage: Maven build → JRE runtime
│   ├── pom.xml                    # Spring Boot 4.1.1, Java 21
│   └── src/main/
│       ├── java/com/ayudasgv/
│       │   ├── AyudasGvApplication.java
│       │   ├── config/            # SecurityConfig, OpenApiConfig
│       │   ├── auth/              # JWT, login (Semana 2)
│       │   ├── expedientes/       # Búsqueda y consulta por secciones (Semanas 4-5)
│       │   ├── tramitacion/       # Trámites y asignación de instructor (Fase 1)
│       │   └── commons/           # GlobalExceptionHandler + auditoría AOP + correlation-id (Semana 6)
│       └── resources/
│           ├── application.yml         # Config base (usa variables de entorno)
│           ├── application-dev.yml     # Override para desarrollo local
│           ├── db/migration/           # V1…V4 — migraciones Flyway
│           └── i18n/                   # messages_es / messages_eu
├── frontend/                      # React + Vite + TypeScript
│   ├── Dockerfile                 # Multi-stage: Node build → Nginx
│   ├── nginx.conf                 # Sirve la SPA y proxea /api
│   └── src/
│       ├── api/                   # Capa HTTP — axiosClient (interceptor JWT) + authApi + expedientes*Api
│       ├── components/            # Layout, selector de idioma, tabla/filtros/detalle de expedientes
│       ├── i18n/locales/          # es.json / eu.json
│       ├── pages/                 # Login, listado de expedientes, detalle por secciones
│       ├── router/                # AppRouter + ProtectedRoute — rutas públicas y protegidas
│       ├── store/                 # authStore (Zustand, persistido en localStorage)
│       └── types/                 # Tipos TypeScript del dominio
├── docs/
│   ├── README.md                  # Índice de la documentación (empezar aquí)
│   ├── tecnologias.md             # Explicación profunda del ecosistema técnico
│   ├── arquitectura.md            # Diagramas de capas, módulos y decisiones técnicas
│   ├── modelo-datos.md            # Esquema ER completo
│   ├── fase0-planificacion.md     # Plan detallado semana a semana (histórico)
│   ├── docker-k8s/                # Fase K8s: guía con ejercicios, relato divulgativo,
│   │                              #   referencia de Docker y preparación de entrevista
│   ├── diagramas/                 # Diagramas .excalidraw del entorno (Docker y K8s)
│   ├── instrucciones-iniciales/   # Enunciado original del proyecto (.docx)
│   └── reportes/                  # Reportes diarios y semanales de desarrollo
├── docker-compose.yml             # Orquestación local: postgres + backend + frontend
├── spec.md                        # Memoria del proyecto (SDD)
└── plan.md                        # Plan de trabajo vivo por fases (SDD)
```

---

## Fases del proyecto

El proyecto avanza en una sola secuencia de etapas. Las fases funcionales siguen el orden del enunciado
original y cada tecnología nueva entra de una en una, en la etapa donde resuelve un problema real del dominio
(por eso es también el laboratorio del portfolio: todo lo que figura aquí se puede ver en el código).

| Etapa | Objetivo | Tecnología nueva | Estado |
|-------|----------|------------------|--------|
| 1 — Fase 0: MVP | Autenticación + búsqueda + consulta de expedientes por secciones | Spring Boot, JWT, SQL explícito, i18n | Completada |
| 2 — Fase K8s | Desplegar y operar la app en Kubernetes | Kubernetes (minikube) | Completada |
| 3 — Fase 1: Tramitación | Trámites, asignación de instructor y cambio de estado | — | Completada |
| 4 — Plataforma | Migración a Spring Boot 4 y clúster al día | Spring Boot 4 | En curso |
| 5 — Asistente de normativa | Respuestas con artículos citados, en es/eu, también como servidor MCP | Spring AI (RAG) + MCP | Siguiente |
| 6 — Fase 2: Alta | Creación de nuevos expedientes y solicitudes | — | Pendiente |
| 7 — Fase 3: Resolución | Documentos de resolución y notificaciones | — | Pendiente |
| 8 — Fase 4: Reglas de negocio | Validación de requisitos y cálculo de cuantías | — | Pendiente |
| 9 — Microservicios I | Extraer `ms-tramitacion` con puertos y adaptadores | Arquitectura hexagonal | Pendiente |
| 10 — Microservicios II | Notificaciones asíncronas por eventos (outbox) | Kafka | Pendiente |
| 11 — Microservicios III | API gateway reactivo | WebFlux (Spring Cloud Gateway) | Pendiente |
| 12 — Despliegue final | Todo el sistema en OpenShift | OpenShift | Pendiente |

Detalle de cada etapa y checklist en [plan.md](plan.md).


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
| `instructor1` | `admin123` | INSTRUCTOR (tiene expedientes asignados) |
| `instructor2` | `admin123` | INSTRUCTOR (sin expedientes: sirve para probar el 403 de la regla) |
| `consultor1` | `admin123` | CONSULTOR |

---

## Documentación adicional

| Documento | Descripción |
|-----------|-------------|
| [spec.md](spec.md) | Memoria del proyecto: qué es, arquitectura, decisiones, metodología (SDD) |
| [plan.md](plan.md) | Plan de trabajo vivo: fases, checklist e histórico condensado (SDD) |
| [Índice de la documentación](docs/README.md) | Mapa de todo lo que hay en `docs/` y por dónde empezar |
| [Guía Docker + Kubernetes](docs/docker-k8s/kubernetes-aprendizaje.md) | Paso a paso con ejercicios (learning track actual) |
| [Kubernetes desde 0](docs/docker-k8s/kubernetes-desde-0.md) | El mismo track contado en cristiano, para quien no ha tocado K8s nunca |
| [Tecnologías y ecosistema](docs/tecnologias.md) | Explicación profunda del stack, Docker y comunicaciones |
| [Arquitectura](docs/arquitectura.md) | Diagramas de capas, módulos, endpoints y decisiones técnicas |
| [Modelo de datos](docs/modelo-datos.md) | Esquema ER completo por contextos |
| [Planificación Fase 0](docs/fase0-planificacion.md) | Detalle histórico semana a semana |
| [Reportes diarios](docs/reportes/) | Log de progreso por sesión |

---

Este repositorio usa **Spec-Driven Development**: [spec.md](spec.md) y [plan.md](plan.md) son la fuente de verdad; toda decisión de producto/arquitectura se registra en `spec.md` y nada se implementa sin su punto en `plan.md`.
