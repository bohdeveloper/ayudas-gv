# PROJECT MEMORY — ayudas-gv

> Lee este archivo al inicio de cada sesión para tener contexto completo del proyecto.
> Actualiza siempre la sección "Estado actual" y el "Log de sesiones" al terminar.

---

## Identidad del proyecto

- **Nombre:** Sistema de Gestión y Tramitación de Expedientes de Ayudas
- **Cliente:** Gobierno Vasco — Dirección de Prestaciones Familiares
- **Tipo:** Proyecto de formación DAW — entregable evaluado
- **Repo GitHub:** https://github.com/[USUARIO]/ayudas-gv ← actualizar con URL real tras primer push
- **Stack:** React 18 + Vite + TypeScript + react-i18next + Zustand | Spring Boot 3.x + Java 17 + JDBC + PostgreSQL + Flyway | Docker Compose

---

## Arranque rápido

```bash
# Un solo comando — levanta BD + backend + frontend
docker-compose up --build

# Accesos
# Frontend:  http://localhost:3000
# Backend:   http://localhost:8080
# Swagger:   http://localhost:8080/api/swagger-ui.html

# Desarrollo local (más rápido, sin Docker para el código)
docker-compose up -d postgres          # solo la BD
cd backend && mvn spring-boot:run -Dspring.profiles.active=dev
cd frontend && npm install && npm run dev
```

**Usuarios de prueba (contraseña: `admin123`):**
- `admin` → ADMIN + RESPONSABLE
- `instructor1` → INSTRUCTOR
- `consultor1` → CONSULTOR

---

## Estado actual

**Fecha última actualización:** 2026-06-08
**Fase activa:** Fase 0 — MVP
**Semana activa:** Semana 1 (entorno, esqueleto y BD)

### Completado hasta ahora

- [x] Estructura de carpetas creada (backend + frontend + docs)
- [x] README.md, ROADMAP.md, .gitignore
- [x] docs/arquitectura.md, docs/modelo-datos.md, docs/fase0-planificacion.md
- [x] docker-compose.yml (3 servicios: postgres + backend + frontend)
- [x] backend/pom.xml con todas las dependencias Spring Boot 3
- [x] backend/AyudasGvApplication.java
- [x] backend/application.yml + application-dev.yml
- [x] V1__security_catalogs.sql (tablas + datos semilla bilingües es/eu)
- [x] V2__expedientes.sql (tablas + datos de ejemplo)
- [x] V3__auditoria_indices.sql (auditoría + índices)
- [x] V4__tramitacion.sql (Fase 1 — preparado)
- [x] messages_es.properties + messages_eu.properties
- [x] backend/Dockerfile (multi-stage Maven → JRE)
- [x] frontend/package.json, tsconfig.json, vite.config.ts
- [x] frontend/index.html, main.tsx, App.tsx
- [x] frontend/src/router/AppRouter.tsx (skeleton)
- [x] frontend/src/i18n/index.ts + locales/es.json + locales/eu.json
- [x] frontend/src/types/index.ts (todos los tipos TypeScript)
- [x] frontend/Dockerfile + nginx.conf
- [x] Repo GitHub creado y código subido

### Pendiente próxima sesión

- [ ] Verificar que `docker-compose up --build` levanta todo sin errores
- [ ] Semana 1: Spring Boot conecta a BD y Flyway aplica migraciones
- [ ] Semana 1: React arranca y muestra pantalla inicial
- [ ] Semana 1: Swagger UI accesible → **Hito M1**
- [ ] **Imágenes:** Exportar del Word y guardar en `docs/assets/er-diagrama.png` y `docs/assets/cronograma-fase0.png`

---

## Estructura de archivos clave

```
ayudas-gv/
├── PROJECT_MEMORY.md              ← este archivo
├── README.md                      ← arranque y descripción
├── ROADMAP.md                     ← fases y tareas por semana
├── docker-compose.yml             ← BD + backend + frontend
├── .gitignore
├── docs/
│   ├── arquitectura.md
│   ├── modelo-datos.md
│   ├── fase0-planificacion.md
│   └── assets/                    ← er-diagrama.png + cronograma-fase0.png
├── backend/
│   ├── pom.xml                    ← Spring Boot 3.2.5, JDBC, JWT, Flyway, Swagger
│   ├── Dockerfile
│   └── src/main/
│       ├── java/com/ayudasgv/
│       │   ├── AyudasGvApplication.java
│       │   ├── auth/              ← semana 2: login, JWT, Spring Security
│       │   ├── expedientes/       ← semana 4-5: búsqueda y secciones
│       │   ├── tramitacion/       ← fase 1
│       │   └── commons/           ← auditoría AOP, errores, logging
│       └── resources/
│           ├── application.yml
│           ├── application-dev.yml
│           ├── db/migration/
│           │   ├── V1__security_catalogs.sql   ← semana 1 ✓
│           │   ├── V2__expedientes.sql          ← semana 4 ✓
│           │   ├── V3__auditoria_indices.sql    ← semana 6 ✓
│           │   └── V4__tramitacion.sql          ← fase 1 ✓
│           └── i18n/
│               ├── messages_es.properties
│               └── messages_eu.properties
└── frontend/
    ├── package.json               ← React 18, react-i18next, Zustand, axios
    ├── vite.config.ts             ← proxy /api → localhost:8080
    ├── tsconfig.json
    ├── Dockerfile + nginx.conf
    └── src/
        ├── main.tsx
        ├── App.tsx
        ├── router/AppRouter.tsx   ← skeleton, rutas por implementar
        ├── i18n/
        │   ├── index.ts           ← configuración i18next
        │   └── locales/
        │       ├── es.json        ← traducciones castellano
        │       └── eu.json        ← traducciones euskera
        ├── types/index.ts         ← tipos TS de toda la app
        ├── api/                   ← capa HTTP (implementar semana 3)
        ├── components/            ← componentes reutilizables
        ├── pages/                 ← Login, Expedientes, ExpedienteDetalle
        ├── store/                 ← Zustand (auth store, expedientes store)
        └── hooks/                 ← hooks custom
```

---

## Decisiones técnicas tomadas

| Decisión | Opción elegida | Alternativa descartada | Por qué |
|----------|---------------|----------------------|---------|
| Acceso a datos | JdbcTemplate + SQL explícito | JPA/Hibernate | Requisito del proyecto para aprender SQL |
| Estado frontend | Zustand | Redux Toolkit | Más simple, menos boilerplate |
| JWT library | jjwt 0.12.5 | Spring Security OAuth | Más control, proyecto educativo |
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

---

## Ayudas gestionadas (resumen)

| Código | Descripción | Modalidad | Clave técnica |
|--------|-------------|-----------|---------------|
| ASIG_DISCAP | Hijo/menor con discapacidad | Periódico mensual | grado ≥ 33% (<18a) o ≥ 65% (≥18a) |
| PAGO_NAC | Nacimiento/adopción supuestos específicos | Pago único | fam. numerosa / monoparental / discapacidad ≥ 65% |
| PARTO_MULT | Parto/adopción múltiples | Pago único | 2→4×SMI, 3→8×SMI, 4+→12×SMI |

---

## Log de sesiones

### Sesión 1 — 2026-06-08
**Hecho:**
- Leídos ambos documentos Word del proyecto (enunciado + planificación fase 0)
- Creada estructura completa de carpetas (39 directorios)
- Creados todos los archivos de documentación y skeleton del proyecto
- Repo GitHub creado y código subido

**Bloqueantes/notas:**
- Las imágenes del ER y el cronograma están embebidas en los .docx — exportarlas manualmente a `docs/assets/`
- El backend y frontend son esqueletos listos para implementar — no tienen lógica de negocio aún
- Empezar por Semana 1: verificar que `docker-compose up` levanta los tres servicios

**Próxima sesión:**
- Verificar arranque con docker-compose
- Implementar conexión real BD + Flyway (Semana 1)
- Si falla algo, revisar primero `application.yml` y `docker-compose.yml`

---
<!-- INSTRUCCIONES: Al cerrar sesión, añade una nueva entrada al Log con fecha, hecho, bloqueantes y próxima sesión -->
