# PROJECT MEMORY — ayudas-gv

> Lee este archivo al inicio de cada sesión para tener contexto completo del proyecto.
> Actualiza siempre la sección "Estado actual" y el "Log de sesiones" al terminar.

---

## Identidad del proyecto

- **Nombre:** Sistema de Gestión y Tramitación de Expedientes de Ayudas
- **Cliente:** Gobierno Vasco — Dirección de Prestaciones Familiares
- **Tipo:** Proyecto de formación DAW — entregable evaluado
- **Repo GitHub:** https://github.com/bohdeveloper/ayudas-gv
- **Stack:** React 18 + Vite + TypeScript + react-i18next + Zustand | Spring Boot 3.5.14 + Java 21 + JDBC + PostgreSQL + Flyway | Docker Compose

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

**Fecha última actualización:** 2026-06-09 (fin de jornada)
**Fase activa:** Fase 0 — MVP
**Semana activa:** Semana 2 (módulo auth backend)

### Completado hasta ahora

- [x] Estructura de carpetas creada (backend + frontend + docs)
- [x] README.md, ROADMAP.md, .gitignore
- [x] docs/arquitectura.md, docs/modelo-datos.md, docs/fase0-planificacion.md
- [x] docs/reportes/ con reporte de lunes y martes
- [x] docker-compose.yml (3 servicios: postgres + backend + frontend)
- [x] backend/pom.xml — Spring Boot 3.5.14, Java 21, todas las dependencias
- [x] backend/AyudasGvApplication.java + config/OpenApiConfig.java
- [x] backend/application.yml + application-dev.yml (claves logging escapadas)
- [x] META-INF/additional-spring-configuration-metadata.json (propiedades app.jwt.*)
- [x] V1__security_catalogs.sql (tablas + datos semilla bilingües es/eu)
- [x] V2__expedientes.sql (tablas + datos de ejemplo)
- [x] V3__auditoria_indices.sql (auditoría + índices)
- [x] V4__tramitacion.sql (Fase 1 — preparado)
- [x] messages_es.properties + messages_eu.properties
- [x] backend/Dockerfile (multi-stage Maven → JRE)
- [x] frontend/package.json, tsconfig.json (ignoreDeprecations 6.0), vite.config.ts
- [x] frontend/index.html, main.tsx, App.tsx
- [x] frontend/src/router/AppRouter.tsx (skeleton)
- [x] frontend/src/i18n/index.ts + locales/es.json + locales/eu.json
- [x] frontend/src/types/index.ts (todos los tipos TypeScript)
- [x] frontend/Dockerfile + nginx.conf
- [x] Repo GitHub: https://github.com/bohdeveloper/ayudas-gv

### Pendiente próxima sesión

- [ ] Crear rama `feat/semana2-auth`
- [ ] Semana 2: módulo `auth` backend completo (AuthController, AuthService, JwtProvider, UsuarioRepository, SecurityFilterChain, JwtAuthenticationFilter)
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
│   ├── pom.xml                    ← Spring Boot 3.5.14, Java 21, JDBC, JWT, Flyway, Swagger
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

### Sesión 2 — 2026-06-09
**Hecho:**
- Corregidos todos los errores de configuración del proyecto (sin código de negocio)
- Spring Boot actualizado 3.2.5 → 3.5.14, Java 17 → 21
- Resuelto error crítico `flyway-database-postgresql` sin versión (artefacto de Flyway 10.x, gestionado por el BOM desde Spring Boot 3.3+)
- Creado `OpenApiConfig.java` con `@OpenAPIDefinition` (sustituye `springdoc.info` inválido en YAML)
- Añadido `additional-spring-configuration-metadata.json` para propiedades `app.jwt.*`
- Corregidas claves con puntos en YAML de logging (escapadas con `[]`) en `application.yml` y `application-dev.yml`
- Corregido `tsconfig.json`: añadido `ignoreDeprecations: "6.0"` para `baseUrl` deprecado en TS 6+
- Definidas premisas de trabajo (ramas, reporte diario, demo viernes, estructura bloques)
- Sistema de reportes diarios establecido en `docs/reportes/`
- URL real del repo actualizada en PROJECT_MEMORY

**Bloqueantes/notas:**
- Maven pendiente de recargar en VSCode (acción manual tras cambios en pom.xml)
- Confirmar instalación de Java 21 JDK en el equipo
- Hito M1 (docker-compose up levanta todo) aún no verificado

**Próxima sesión:**
- Crear rama `feat/semana2-auth`
- Implementar módulo auth backend completo (Semana 2)
- Continuar con demo del Hito M1 si hace falta (todo validado y corriendo)

### Actualización fin de jornada — 2026-06-09
**Añadido en la segunda parte de la sesión:**
- `docs/tecnologias.md` creado (ecosistema Docker, Flyway, Nginx, Swagger explicados en profundidad)
- `README.md` actualizado completo con stack real y accesos
- `ROADMAP.md` expandido con estructura Tareas/Aprende/Hecho cuando/Hitos diarios
- Hito M1 completado y validado en vivo (3 contenedores, Flyway OK, Swagger OK)
- Puerto PostgreSQL externo cambiado a 5433 (PostgreSQL 18 local ocupa 5432)
- DBeaver conectado y tablas visibles
- Documentos Word movidos a `docs/instrucciones-iniciales/`

---
<!-- INSTRUCCIONES: Al cerrar sesión, añade una nueva entrada al Log con fecha, hecho, bloqueantes y próxima sesión -->
