# Planificación Fase 0 — MVP

**Sistema de Gestión y Tramitación de Expedientes de Ayudas**
**Alcance:** Autenticación · Selección de idioma · Búsqueda · Consulta de expedientes

---

## Cronograma e hitos

| Semana | Foco | Hito | Resultado demostrable |
|--------|------|------|----------------------|
| S1 | Entorno, esqueleto y BD | M1 | Entorno + BD listos |
| S2 | Autenticación (backend) | M2 | Login con JWT |
| S3 | Login frontend + idiomas | M3 | Login UI + idiomas |
| S4 | Modelo de expedientes + búsqueda | M4 | Búsqueda funcional |
| S5 | Consulta por secciones | M5 | Consulta por secciones |
| S6 | Transversales, pruebas y demo | M6 | MVP + auditoría/pruebas |

---

## Premisas y buenas prácticas

- Trabajar en **ramas** por funcionalidad; commits pequeños y frecuentes.
- **Reporte diario**: qué se completó, qué queda, posibles bloqueos.
- Cerrar cada semana con una **demo** del resultado demostrable.
- Cada bloque incluye: *Tareas*, *Aprende*, *Hecho cuando*, *Hitos diarios*.

---

## Semana 1 — Entorno, esqueleto y base de datos

**Aprende:** Estructura de proyecto Maven/npm, build con Vite, migraciones Flyway.

**Hecho cuando:** Ambos proyectos arrancan, migraciones crean las tablas con sus datos y Swagger UI carga.

### Hitos diarios

| Día | Objetivo |
|-----|----------|
| D1 | Herramientas instaladas (JDK 17, Node 20, Docker, IDE) + repositorio Git creado con estructura `backend/` `frontend/` |
| D2 | PostgreSQL en Docker Compose levantado; backend Spring Boot arranca y conecta a la BD |
| D3 | Flyway configurado; primera migración aplicada con tablas de seguridad (`usuario`, `rol`, `usuario_rol`) |
| D4 | Migración de catálogos (`tipo_ayuda`, `estado_expediente`, `tipo_tramite`) con datos semilla bilingües (es/eu) |
| D5 | Frontend React+Vite arranca con pantalla inicial; Swagger UI accesible **→ Hito M1** |

---

## Semana 2 — Autenticación (backend)

**Aprende:** Spring Security (cadena de filtros), JWT, hashing de contraseñas con BCrypt.

**Hecho cuando:** Desde Swagger se obtiene un token con credenciales válidas y, con ese token, se accede al endpoint protegido (sin token → 401).

### Hitos diarios

| Día | Objetivo |
|-----|----------|
| D1 | Repositorio de usuario con SQL (consulta por `username`) probado contra la BD |
| D2 | Spring Security configurado; verificación de contraseña con BCrypt (credenciales válidas/inválidas) |
| D3 | Endpoint `POST /api/v1/auth/login` emite JWT firmado cuando las credenciales son correctas |
| D4 | Filtro de validación del token + endpoint protegido de prueba (200 con token, 401 sin él) |
| D5 | Login y endpoint documentados en Swagger con esquema Bearer; prueba E2E **→ Hito M2** |

---

## Semana 3 — Login en frontend + idiomas

**Aprende:** React Router, gestión de sesión en cliente, i18next.

**Hecho cuando:** Se inicia sesión desde la UI, las rutas privadas exigen estar autenticado y la interfaz cambia de idioma sin recargar.

### Hitos diarios

| Día | Objetivo |
|-----|----------|
| D1 | Estructura del frontend (rutas y layout) + cliente HTTP (axios) con interceptor que envía el token |
| D2 | Pantalla de login que llama al backend, guarda el token y muestra error de credenciales |
| D3 | Rutas protegidas + logout + redirección a login al expirar la sesión |
| D4 | i18n con `react-i18next`: ficheros `es.json`/`eu.json` y textos de login/menú externalizados |
| D5 | Selector de idioma con persistencia y conmutación en caliente **→ Hito M3** |

---

## Semana 4 — Modelo de expedientes y búsqueda

**Aprende:** JOINs y paginación en SQL, consultas dinámicas parametrizadas, tablas/paginación en React.

**Hecho cuando:** Se buscan expedientes por varios filtros y se ven los resultados paginados en el idioma activo.

### Hitos diarios

| Día | Objetivo |
|-----|----------|
| D1 | Migración de `persona`, `expediente` y `expediente_interesado` con datos de ejemplo |
| D2 | Migración de `datos_contacto`, `solicitud_ayuda` y tablas de detalle con más datos de ejemplo |
| D3 | Endpoint de búsqueda con filtros básicos (número, tipo, estado) en SQL parametrizado |
| D4 | Paginación + ordenación + descripción de catálogo según idioma en el resultado (backend) |
| D5 | Frontend: formulario de filtros + tabla de resultados paginada conectada al backend **→ Hito M4** |

---

## Semana 5 — Consulta del expediente por secciones

**Aprende:** Diseño de DTOs por sección, composición de vistas en React con pestañas.

**Hecho cuando:** Desde un resultado de búsqueda se abre el expediente y se navega por sus secciones, todas traducidas.

### Hitos diarios

| Día | Objetivo |
|-----|----------|
| D1 | Endpoint y DTO de la sección **datos generales** del expediente |
| D2 | Endpoints/DTOs de **interesados** y de **contacto y aviso** |
| D3 | Endpoint/DTO de **datos de la ayuda** (diferenciado por tipo de ayuda) |
| D4 | Frontend: vista de detalle con pestañas/paneles que cargan cada sección de forma independiente |
| D5 | Etiquetas del detalle internacionalizadas + enlace búsqueda → detalle **→ Hito M5** |

---

## Semana 6 — Transversales, pulido y demo

**Aprende:** AOP/interceptores, pruebas backend (JUnit 5, Mockito, `@WebMvcTest`), pruebas frontend (Vitest, `@testing-library/react`, `vi.mock`).

**Hecho cuando:** Las acciones relevantes quedan auditadas, la suite de pruebas pasa y el MVP se puede demostrar de principio a fin.

### Hitos diarios

| Día | Objetivo |
|-----|----------|
| D1 | Auditoría transversal (interceptor/AOP) registrando login y consultas en la tabla `auditoria` |
| D2 | Logging estructurado con `correlation-id` + manejo de errores homogéneo (`@ControllerAdvice`) |
| D3 | Pruebas unitarias de la capa de servicios (JUnit 5 + Mockito) sobre la lógica clave |
| D4 | Prueba de repositorio/controlador + prueba de componente frontend + repaso de autorización por rol |
| D5 | README de arranque verificado + repaso final + **demo del MVP completa → Hito M6** |

---

## Nota de ritmo y riesgos

> **La semana 4 es la más densa** (modelo de datos + búsqueda completa). Si se acumula retraso, mover la tabla de resultados del frontend a la semana 5 y deslizar el resto. **No recortar la semana 6**: auditoría y pruebas son parte del entregable.

| Riesgo | Mitigación |
|--------|-----------|
| Spring Security + JWT lleva más tiempo del esperado (semana 2) | Reservar margen; hacer la configuración básica primero sin JWT y añadirlo después |
| i18n integrado tarde genera trabajo de "retraducir" pantallas ya hechas | Integrar desde semana 3; nunca dejar para el final |
| Acumulación de pruebas solo en semana 6 | Escribir pruebas a medida que se avanza; la semana 6 consolida, no empieza desde cero |
