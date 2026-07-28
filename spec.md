# spec.md — Sistema de Gestión de Ayudas GV

> Documento vivo de especificación (Spec-Driven Development).
> Memoria del proyecto: qué es, cómo está construido, decisiones y metodología.
> El trabajo pendiente vive en [plan.md](plan.md) (que incluye el histórico resumido de lo completado).
> Regla: toda decisión nueva de producto/arquitectura se registra aquí en la misma sesión.

---

## 1. Qué es Ayudas GV

Aplicación web interna para el **Gobierno Vasco — Dirección de Prestaciones Familiares** que permite
gestionar y tramitar **expedientes de ayudas de carácter social** (arts. 351–362 y 373 del TRLGSS).

Es un **proyecto de formación incremental**: se construye por fases cerradas y demostrables, con foco
en aprender el stack (Spring Boot, React, JWT, SQL explícito, i18n) y las prácticas profesionales
(pruebas, auditoría, arquitectura limpia). El usuario es una persona junior que ya programa y está
dominando este stack; parte del valor del repo es didáctico.

- **Usuarios internos** (no ciudadanía): personal de consulta, instructores, responsables y administración.
- **Bilingüe obligatorio** (castellano `es` / euskera `eu`) en interfaz, mensajes y catálogos.
- **Tres ayudas gestionadas**:

  | Código | Descripción | Modalidad | Base normativa |
  |--------|-------------|-----------|----------------|
  | `ASIG_DISCAP` | Asignación económica por hijo/menor a cargo con discapacidad | Periódico (mensual) | Arts. 351.a, 352–356 |
  | `PAGO_NAC` | Pago único por nacimiento o adopción (supuestos específicos) | Pago único | Arts. 351.b, 357–358 |
  | `PARTO_MULT` | Pago único por parto o adopción múltiples | Pago único | Arts. 351.c, 359–360 |

- **Fase actual:** Fase 0 (MVP) **completada** (semanas S1–S6). Próximo foco decidido con el usuario:
  **aprender Docker + Kubernetes** sobre este proyecto antes de abordar la Fase 1 (ver plan.md).
- **Repo:** monorepo local `c:\aplic\ayudas_gv` (Git, rama principal `main`).

---

## 2. Stack y arquitectura

### Stack

| Capa | Tecnología |
|------|-----------|
| Frontend | React 18 + Vite + TypeScript + react-i18next + Zustand + axios |
| Backend | Java 21 + Spring Boot 3.5.x + Spring Security + JWT (jjwt 0.12.5) |
| Acceso a datos | `NamedParameterJdbcTemplate` — **SQL explícito, sin JPA/Hibernate** |
| Base de datos | PostgreSQL 15 + Flyway (migraciones versionadas) |
| API docs | Swagger UI (springdoc-openapi 2.7.0) |
| Infra local | Docker + Docker Compose (postgres + backend + frontend) |
| Pruebas | Backend: JUnit 5 + Mockito + `@WebMvcTest` + `@JdbcTest`(H2). Frontend: Vitest + Testing Library |

### Estructura del repo

```
ayudas-gv/
├── backend/                    # Spring Boot — monolito modular
│   ├── Dockerfile              # Multi-stage: Maven build → JRE runtime
│   └── src/main/
│       ├── java/com/ayudasgv/
│       │   ├── config/         # SecurityConfig, OpenApiConfig
│       │   ├── auth/           # JWT, login (Fase 0)
│       │   ├── expedientes/    # Búsqueda y consulta por secciones (Fase 0)
│       │   ├── tramitacion/    # Trámites (Fase 1, pendiente)
│       │   └── commons/        # GlobalExceptionHandler + auditoría AOP + correlation-id
│       └── resources/
│           ├── application.yml / application-dev.yml
│           ├── db/migration/   # V1…V4 — Flyway
│           └── i18n/           # messages_es / messages_eu
├── frontend/                   # React + Vite + TS
│   ├── Dockerfile              # Multi-stage: Node build → Nginx
│   ├── nginx.conf              # Sirve la SPA y proxea /api → backend
│   └── src/{api,components,i18n,pages,router,store,types}/
├── docs/                       # Documentación (ver §7)
├── docker-compose.yml
├── spec.md · plan.md · README.md · CLAUDE.md
├── .mcp.json                   # Config del grafo de código (codebase-memory-mcp)
└── .codebase-memory/           # Índice local del grafo (no versionado) + graph.html (sí versionado)
```

### Patrones de arquitectura establecidos

- **Monolito modular → microservicios.** El MVP es un único despliegue con paquetes `auth`,
  `expedientes`, `tramitacion`, `commons` de **fronteras limpias** (sin dependencias cruzadas indebidas),
  diseñados para extraerse como microservicios a partir de la Fase 1: `gateway`, `ms-autenticacion`,
  `ms-expedientes`, `ms-tramitacion` + librería `commons`.
- **Capas:** `controller → service → repository`. La lógica de negocio vive SIEMPRE en `service`.
- **DTO + Mapper:** nunca se exponen entidades de BD en la API.
- **Auth:** login público emite JWT (HS, 24h); `JwtAuthFilter` valida el token en cada petición protegida;
  autorización por rol en backend (no basta ocultar botones en el frontend).
- **Auditoría transversal:** vía `@Aspect` (AOP) → tabla `auditoria`; correlation-id propagado por petición.
- **Errores homogéneos:** `@ControllerAdvice` / `GlobalExceptionHandler`.
- **i18n:** sin literales hardcoded; frontend por `t('clave')`, backend por `MessageSource` + `Accept-Language`.
- **Salud estructural (grafo de código, 2026-07-23):** sin ciclos de importación — mantenerlo así.
  Abstracciones núcleo (las más conectadas) a reutilizar y nunca duplicar: `useAuthStore` (hub del estado de
  auth; todas las páginas y menús del frontend leen de él, no hay hook `useAuth` separado) y
  `ExpedienteRepository` (acceso a datos SQL de expedientes).

### Modelo de dominio (resumen)

El **expediente** es la entidad central: pertenece a un `tipo_ayuda` y un `estado_expediente`, tiene
instructor asignable, y se organiza en **secciones independientes** (datos generales, interesados,
contacto/aviso, datos de la ayuda, tramitación). Los **interesados** (`SOLICITANTE`/`REPRESENTANTE`/`CAUSANTE`)
vinculan `persona`↔`expediente`. La `solicitud_ayuda` (1:1) tiene un detalle por tipo
(`detalle_asignacion_discapacidad` / `detalle_nacimiento_adopcion` / `detalle_parto_multiple`).
La `tramitacion` (Fase 1) registra el histórico de `tramite`. Todo lo relevante deja traza en `auditoria`.
Esquema completo en [docs/modelo-datos.md](docs/modelo-datos.md).

---

## 3. Decisiones de producto y reglas de negocio (invariantes)

Decisiones YA tomadas que no se reabren sin decisión explícita del usuario:

1. **SQL explícito, nunca JPA/Hibernate.** Acceso a datos con `JdbcTemplate`/`NamedParameterJdbcTemplate`;
   SQL escrito a mano, **siempre parametrizado** (anti-inyección), centralizado en repositorios.
2. **Bilingüismo `es`/`eu` de extremo a extremo.** Catálogos con columnas `descripcion_es`/`descripcion_eu`
   y `codigo` como identificador estable. Nada de literales quemados.
3. **Idioma de interfaz ≠ idioma de notificación.** `usuario.idioma_preferido` (interfaz interna) es
   independiente de `datos_contacto.idioma_notificacion` (comunicación con el interesado).
4. **Autorización en backend, siempre.** El frontend adapta la UI al rol, pero la regla se valida en servidor.
   Regla de negocio clave: *un instructor solo puede tramitar expedientes asignados a él*.
5. **Roles:** `CONSULTOR` (lectura) ⊂ `INSTRUCTOR` (+ tramitar lo asignado) ⊂ `RESPONSABLE`
   (+ asignar instructor y resolver); `ADMIN` gestiona usuarios/roles/catálogos.
6. **Arquitectura evolutiva monolito modular → microservicios** con fronteras limpias desde el día 1.
7. **API versionada** bajo `/api/v1/...`; recursos en plural; paginación/filtro/orden por query params.
8. **Entrega incremental por fases**, cada fase cerrada y demostrable (código en rama + PR, migraciones
   Flyway, Swagger operativo, pruebas en verde, demo).
9. **Seguridad:** contraseñas BCrypt (nunca en claro); endpoints protegidos por defecto, solo login público;
   nunca datos sensibles en logs.
10. **Pruebas como entregable**, no como extra: objetivo orientativo ≥ 70 % en capa de servicios,
    priorizando reglas de negocio, validaciones y autorización.

---

## 4. Sistema de diseño y convenciones UI

El frontend tiene un sistema de diseño propio (tokens CSS, tema claro/oscuro, badges de estado)
documentado en el propio código y en la memoria del proyecto — reutilizarlo en las fases siguientes.

**PROHIBICIONES explícitas (preferencia del usuario, aplica también a documentos generados):**

- **Nunca usar emojis como iconos.** Los iconos son SVG propios con `fill/stroke: currentColor`.
  En documentación se usan marcadores de texto (`COMPLETADA`, `EN CURSO`, `PENDIENTE`) y checkboxes
  Markdown `[x]`/`[ ]`, no emojis decorativos.

---

## 5. Entorno de desarrollo y producción

### Arranque con Docker (recomendado)

```bash
docker compose up --build
```

| Servicio | URL | Puerto contenedor |
|----------|-----|-------------------|
| Frontend (React + Nginx) | http://localhost:3000 | 80 |
| Backend (API Spring Boot) | http://localhost:8080/api | 8080 |
| Swagger UI | http://localhost:8080/api/swagger-ui.html | — |
| PostgreSQL | localhost:**5433** (externo; 5432 reservado a Postgres local) | 5432 |

Parar: `docker compose down` · Parar y borrar datos: `docker compose down -v`.

### Arranque local (sin Docker)

Requiere JDK 21, Node 20, Maven 3.9 y Docker (solo para la BD).

```bash
docker compose up -d postgres                                   # 1. BD
cd backend && mvn spring-boot:run -Dspring.profiles.active=dev  # 2. backend (8080)
cd frontend && npm install && npm run dev                       # 3. frontend (5173, proxea /api → 8080)
```

### Variables de entorno críticas (backend)

`DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` (¡cambiar en producción!), `JWT_EXPIRATION`, `SERVER_PORT`.
En Compose están en `docker-compose.yml`; en Kubernetes irán a ConfigMap/Secret (ver plan.md, fase K8s).

### Usuarios de prueba (datos semilla)

| Usuario | Contraseña | Rol |
|---------|-----------|-----|
| `admin` | `admin123` | ADMIN + RESPONSABLE |
| `instructor1` | `admin123` | INSTRUCTOR |
| `consultor1` | `admin123` | CONSULTOR |

### Deuda aceptada deliberadamente

- `JWT_SECRET` de ejemplo en `docker-compose.yml` (proyecto formativo; en prod → Secret).
- Sin actuator/health endpoints todavía: relevante para las *probes* de Kubernetes (ver la guía de K8s).

---

## 6. Metodología de desarrollo (Spec-Driven Development)

### Antes de desarrollar
1. Leer `spec.md` y `plan.md`; confirmar que la tarea está en el plan (si no, añadirla primero).
2. Contrastar con los invariantes de §3; avisar si algo choca.
3. Explorar el código con **codebase-memory-mcp** (tools MCP: `search_graph`/`trace_path`/`query_graph`/`get_architecture`) antes de grep/lectura amplia.
4. Buscar entidad/helper existente que ya cubra el concepto antes de crear uno nuevo.
5. Usar la skill de diseño (`ux-ui-designer`) antes de maquetar UI nueva.
6. Desglosar tareas grandes en fases dentro de `plan.md`.

### Durante el desarrollo
Respetar convenciones (§2), validar entradas y aplicar guards/autorización en cada endpoint nuevo,
reutilizar servicios y repositorios existentes, SQL parametrizado siempre.

### Después de desarrollar
1. Typecheck + build de lo tocado (`mvn test` backend, `npm test -- --run` frontend).
2. `/code-review` sobre el diff — obligatorio antes de cerrar cualquier feature.
3. `/security-review` si toca auth/privacidad/auditoría o endpoints nuevos.
4. Verificar end-to-end en la app real, limpiando los procesos propios.
5. Registrar: marcar `plan.md` con fecha; decisiones nuevas → `spec.md`; cambios de alcance/stack → `README.md`.
6. Commit solo cuando el usuario lo pida, en el estilo del `git log` (mensajes `tipo: descripción`).
7. `/simplify` opcional al cerrar una fase.
8. El grafo (codebase-memory-mcp) se refresca solo tras editar (watcher incremental); reindexar solo tras un
   `git pull` grande. Regenerar `.codebase-memory/graph.html` al cerrar cada fase.
9. **Cola de git limpia al cerrar sesión**: ningún fichero se queda indefinidamente pendiente. Cada cambio se
   commitea, se ignora (documentando el motivo) o se borra. Criterio y pasos en `CLAUDE.md`.

> **Decisión (2026-07-28) — el snapshot del grafo deja de versionarse.** `.codebase-memory/graph.db.zst` y
> `artifact.json` pasan a `.gitignore`. Motivo: `artifact.json` guarda el hash de HEAD, así que **cada commit
> disparaba un reindexado que volvía a ensuciar ambos ficheros** — era imposible dejar la cola de git vacía, y
> cada vuelta añadía ~880 kB de binario sin diff (6,5 MB de los 10,58 MB que pesaba el repo). El grafo **no se
> pierde**: vive en local y el watcher lo mantiene; un clon nuevo lo reconstruye indexando. Se siguen
> versionando `graph.html` (entregable visual de cierre de fase) y `.gitattributes`. Revierte la decisión de la
> migración desde graphify (2026-07-23), que lo hacía committeable.

### Skills del proyecto y cuándo usarlas

| Skill | Cuándo |
|-------|--------|
| `grafo-designer` | Cualquier pregunta sobre el código/arquitectura; navegación del repo vía codebase-memory-mcp. Trigger `/grafo-designer` |
| `spec-driven` | Mantener `spec.md`/`plan.md` al día; adoptar/reordenar SDD |
| `ux-ui-designer` (agente) | Diseño o rediseño de páginas/componentes, sistema de diseño, responsive, accesibilidad |
| `git-master` (agente) | Operaciones Git no triviales: conflictos, rebase, estrategia de ramas, rescate de historial |
| `security-master` / `/security-review` | Auditoría de seguridad de la app (IDOR, rate limiting, secretos, headers) |
| `/code-review` | Revisión obligatoria del diff antes de cerrar una feature |
| `/simplify` | Limpieza de reuso/simplificación al cerrar una fase |

---

## 7. Documentos del proyecto

| Documento | Rol |
|-----------|-----|
| [spec.md](spec.md) | **Este documento.** Memoria: qué es, arquitectura, decisiones, metodología |
| [plan.md](plan.md) | Plan de trabajo vivo: fases, checkboxes y histórico condensado |
| [README.md](README.md) | Escaparate público del repo: qué es, features, stack, arranque |
| [docs/README.md](docs/README.md) | **Índice de la documentación**: mapa de `docs/` y por dónde empezar |
| [docs/docker-k8s/kubernetes-aprendizaje.md](docs/docker-k8s/kubernetes-aprendizaje.md) | **Guía paso a paso de Docker + Kubernetes con ejercicios** (learning track actual) |
| [docs/docker-k8s/kubernetes-desde-0.md](docs/docker-k8s/kubernetes-desde-0.md) | Relato divulgativo de la Fase K8s: qué se hizo, por qué y qué se aprendió, sin dar nada por sabido |
| [docs/docker-k8s/docker-guia.md](docs/docker-k8s/docker-guia.md) | Referencia de Docker: instalación, cada archivo Docker y comunicación entre contenedores (Bloque 0) |
| [docs/docker-k8s/entrevista-docker-k8s.md](docs/docker-k8s/entrevista-docker-k8s.md) | Modelo visual (diagramas Mermaid) + 30 preguntas con respuesta modelo + chuleta de comandos |
| [docs/diagramas/](docs/diagramas/) | Diagramas `.excalidraw` del entorno Docker y del despliegue en Kubernetes |
| [docs/arquitectura.md](docs/arquitectura.md) | Diagramas de capas, módulos, endpoints y decisiones técnicas |
| [docs/modelo-datos.md](docs/modelo-datos.md) | Esquema ER completo por contextos + DDL |
| [docs/tecnologias.md](docs/tecnologias.md) | Explicación profunda del ecosistema técnico |
| [docs/fase0-planificacion.md](docs/fase0-planificacion.md) | Detalle histórico de la planificación de Fase 0 |
| [docs/instrucciones-iniciales/](docs/instrucciones-iniciales/) | Enunciado original y planificación (fuente de verdad del alcance) |
| [docs/reportes/](docs/reportes/) | Reportes diarios y semanales de desarrollo |
