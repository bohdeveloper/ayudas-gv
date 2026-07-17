# ROADMAP — Sistema de Gestión de Ayudas GV

---

## Seguimiento de ramas

| Rama | Semana | Estado | Merge a main |
|------|--------|--------|--------------|
| `main` | Setup (S1) | ✅ Mergeada — Hito M1 el 2026-06-09 | — |
| `feat/semana2-auth` | S2 — Auth backend | ✅ Completada — Hito M2 el 2026-06-12 | ✅ 2026-07-03 |
| `feat/semana3-login-frontend` | S3 — Login UI | ✅ Completada — Hito M3 el 2026-06-26 | ✅ 2026-07-03 |
| `feat/semana4-busqueda` | S4 — Búsqueda | ✅ Completada — Hito M4 el 2026-07-03 | ✅ 2026-07-03 |
| `feat/semana5-detalle-secciones` | S5 — Detalle | ✅ Completada — Hito M5 el 2026-07-10 | ✅ 2026-07-03 |
| `feat/semana6-transversales` | S6 — Transversales | ✅ Completada — Hito M6 el 2026-07-17 | ✅ 2026-07-17 |

---

## Auditoría final de Fase 0 frente al enunciado (2026-07-17)

Antes de dar por cerrada la Fase 0 se contrastó el código, migración por migración y
endpoint por endpoint, contra `docs/instrucciones-iniciales/enunciado-proyecto-ayudas-gv.docx`.
Se encontraron y cerraron 3 desviaciones puntuales sobre el enunciado:

- **Sección 8.4** — los ficheros `messages_es/eu.properties` existían pero no se usaban:
  `GlobalExceptionHandler` tenía los mensajes de error escritos directamente en español.
  Ahora se resuelven vía `MessageSource` según `Accept-Language` (`RecursoNoEncontradoException`
  y `BadCredentialsException` llevan la *clave* del mensaje, no el texto ya traducido; los
  `@NotBlank` de `LoginRequest` usan `{clave}` para que Hibernate Validator también resuelva
  contra el `MessageSource`). Se añadió además un `messages.properties` raíz (sin sufijo de
  idioma) porque Spring Boot solo activa `MessageSourceAutoConfiguration` si ese fichero existe.
- **Sección 10** — no había pruebas de repositorio/SQL contra una BD real. Se añadieron 25
  pruebas nuevas (`@JdbcTest` + H2 en modo compatibilidad PostgreSQL, ejecutando las migraciones
  Flyway reales) para `UsuarioRepository`, `ExpedienteRepository` y `AuditoriaRepository`.
- Un texto de `LoginPage` ("Gestión de expedientes") no pasaba por `i18next` — corregido con
  la clave `auth.tagline`.

Backend: 52 pruebas en verde (27 previas + 25 de repositorio). Frontend: 16 pruebas y build
sin cambios. Detalle completo de la auditoría en el historial de conversación de esta sesión.

---

## Fase 0 — MVP (6 semanas) ✅ COMPLETADA

**Objetivo:** Una persona autenticada puede buscar y consultar expedientes organizados por secciones.

---

### Semana 1 — Entorno, esqueleto y base de datos ✅ COMPLETADA
> Rama: `main` (setup inicial directo en main) | Hito M1 alcanzado: 2026-06-09

**Tareas:**
- [x] JDK 21, Node 20, Docker instalados y configurados
- [x] Scaffold completo: backend (Spring Boot 3.5.14) + frontend (React 18 + Vite + TS)
- [x] Docker Compose con 3 servicios configurados (postgres, backend, frontend)
- [x] pom.xml y package.json con todas las dependencias
- [x] Flyway V1: tablas de seguridad (usuario, rol, usuario_rol) con datos semilla bilingües
- [x] Flyway V1: catálogos (tipo_ayuda, estado_expediente, tipo_tramite) con datos semilla bilingües
- [x] Configuración i18n backend (messages_es/eu) y frontend (es.json/eu.json)
- [x] Tipos TypeScript del dominio completos (types/index.ts)
- [x] Dockerfiles multi-stage backend y frontend
- [x] `docker-compose up --build` levanta los 3 servicios sin errores
- [x] Swagger UI accesible en `/api/swagger-ui.html` (200 siguiendo redirect)

**Aprende:** Estructura del proyecto y build (Maven, npm/Vite), migraciones con Flyway.

**Hecho cuando:** Ambos proyectos arrancan, las migraciones crean las tablas con sus datos y Swagger UI carga.

**Hitos diarios:**
- Día 1 — herramientas instaladas (JDK 21, Node, Git, IDE) y repositorio Git creado con estructura inicial ✅
- Día 2 — PostgreSQL en Docker Compose levantado; backend Spring Boot arranca y conecta a BD ✅
- Día 3 — Flyway configurado y primera migración aplicada con tablas de seguridad (usuario, rol, usuario_rol) ✅
- Día 4 — migración de catálogos (tipo_ayuda, estado_expediente, tipo_tramite) con datos semilla bilingües ✅
- Día 5 — frontend React (Vite) arranca (HTTP 200); Swagger UI accesible (HTTP 200) ✅

**🏁 Hito M1 — Resultado demostrable:** Backend y frontend arrancan; migraciones aplicadas; Swagger visible

---

### Semana 2 — Autenticación (backend) ✅ COMPLETADA
> Rama: `feat/semana2-auth` | Hito M2 alcanzado: 2026-06-12

**Tareas:**
- [x] Spring Security configurado (cadena de filtros, rutas públicas y protegidas)
- [x] Repositorio de usuario con SQL — `NamedParameterJdbcTemplate`, consulta por username + roles con JOIN
- [x] `POST /api/v1/auth/login` → valida credenciales con BCrypt y emite JWT firmado (HS384)
- [x] Filtro de validación del token en cada petición protegida (`OncePerRequestFilter`)
- [x] Endpoint protegido de prueba (200 con token válido, 401 sin él)
- [x] Login y endpoint protegido documentados en Swagger con esquema Bearer

**Aprende:** Spring Security (cadena de filtros), JWT, hashing de contraseñas con BCrypt.

**Hecho cuando:** Desde Swagger se obtiene un token con credenciales válidas y, con ese token, se accede al endpoint protegido. Sin token, responde 401.

**Hitos diarios:**
- Día 1 — repositorio de usuario con SQL (consulta por username) probado contra la BD
- Día 2 — Spring Security configurado y verificación de contraseña con BCrypt (válidas/inválidas)
- Día 3 — endpoint de login emite un JWT firmado cuando las credenciales son correctas
- Día 4 — filtro de validación del token y endpoint protegido de prueba (200 con token, 401 sin él)
- Día 5 — login y endpoint documentados en Swagger con esquema Bearer; prueba de extremo a extremo

**🏁 Hito M2 — Resultado demostrable:** Login real que devuelve un JWT y protege un endpoint

---

### Semana 3 — Login en frontend + idiomas ✅ COMPLETADA
> Rama: `feat/semana3-login-frontend` | Hito M3 alcanzado: 2026-06-26

**Tareas:**
- [x] Estructura del frontend (rutas y layout) y cliente HTTP (axios) con interceptor que envía el token
- [x] Pantalla de login en React que llama al backend, guarda el token y muestra error de credenciales
- [x] Rutas protegidas, logout y redirección a login al expirar la sesión
- [x] i18n con `react-i18next`: ficheros `es.json`/`eu.json` y textos de login y menú externalizados
- [x] Selector de idioma con persistencia en `localStorage` y conmutación en caliente

**Aprende:** React Router, gestión de sesión en el cliente, i18next.

**Hecho cuando:** Se inicia sesión desde la UI, las rutas privadas exigen autenticación y la interfaz cambia de idioma sin recargar.

**Hitos diarios:**
- Día 1 — estructura del frontend (rutas y layout) y cliente HTTP con interceptor que envía el token ✅
- Día 2 — pantalla de login conectada al backend, guarda el token y muestra error de credenciales ✅
- Día 3 — rutas protegidas, logout y redirección a login al expirar la sesión ✅
- Día 4 — i18n con react-i18next: ficheros es/eu y textos de login y menú externalizados ✅
- Día 5 — selector de idioma con persistencia y conmutación en caliente ✅

**🏁 Hito M3 — Resultado demostrable:** Iniciar sesión desde la UI y cambiar entre castellano y euskera

---

### Semana 4 — Modelo de expedientes y búsqueda ✅ COMPLETADA
> Rama: `feat/semana4-busqueda` | Hito M4 alcanzado: 2026-07-03

**Tareas:**
- [x] Migraciones V2: `persona`, `expediente`, `expediente_interesado`, `datos_contacto`, `solicitud_ayuda` y tablas de detalle — con datos de ejemplo *(verificado con docker-compose: V1-V4 aplicadas sin errores)*
- [x] `GET /api/v1/expedientes` con filtros (número, tipo, estado, documento, fechas), paginación y ordenación mediante SQL parametrizado
- [x] Descripción de catálogo devuelta según el idioma de la petición (`Accept-Language`)
- [x] Frontend: formulario de filtros y tabla de resultados paginada conectada al backend

**Aprende:** JOINs y paginación en SQL, consultas dinámicas seguras (parametrizadas), tablas y paginación en React.

**Hecho cuando:** Se buscan expedientes por varios filtros y se ven los resultados paginados en el idioma activo.

**Hitos diarios:**
- Día 1 — migración de `persona`, `expediente` y `expediente_interesado` con datos de ejemplo ✅
- Día 2 — migración de `datos_contacto`, `solicitud_ayuda` y tablas de detalle con más datos de ejemplo ✅
- Día 3 — endpoint de búsqueda con filtros básicos (número, tipo, estado) en SQL parametrizado ✅
- Día 4 — paginación, ordenación y descripción de catálogo según idioma en el resultado (backend) ✅
- Día 5 — frontend: formulario de filtros y tabla de resultados paginada conectada al backend ✅

**🏁 Hito M4 — Resultado demostrable:** Buscar expedientes con filtros y ver resultados paginados

---

### Semana 5 — Consulta del expediente por secciones ✅ COMPLETADA
> Rama: `feat/semana5-detalle-secciones` | Hito M5 alcanzado: 2026-07-10

**Tareas:**
- [x] `GET /api/v1/expedientes/{id}/generales` — DTO de datos generales
- [x] `GET /api/v1/expedientes/{id}/interesados` — DTO de interesados
- [x] `GET /api/v1/expedientes/{id}/contacto` — DTO de contacto y aviso
- [x] `GET /api/v1/expedientes/{id}/ayuda` — DTO diferenciado por tipo de ayuda (ASIG_DISCAP / PAGO_NAC / PARTO_MULT)
- [x] Frontend: vista de detalle con pestañas que cargan cada sección de forma independiente
- [x] Etiquetas de la UI internacionalizadas; enlace búsqueda → detalle

**Aprende:** Diseño de DTOs por sección, composición de vistas en React.

**Hecho cuando:** Desde un resultado de búsqueda se abre el expediente y se navega por sus secciones, todas traducidas.

**Hitos diarios:**
- Día 1 — endpoint y DTO de la sección datos generales del expediente ✅
- Día 2 — endpoints/DTOs de interesados y de contacto y aviso ✅
- Día 3 — endpoint/DTO de datos de la ayuda (según tipo de ayuda) ✅
- Día 4 — frontend: vista de detalle con pestañas que cargan cada sección de forma independiente ✅
- Día 5 — etiquetas del detalle internacionalizadas y enlace búsqueda → detalle ✅

**🏁 Hito M5 — Resultado demostrable:** Abrir un expediente y navegar sus secciones

---

### Semana 6 — Transversales, pulido y demo ✅ COMPLETADA
> Rama: `feat/semana6-transversales` | Hito M6 alcanzado: 2026-07-17

**Tareas:**
- [x] Auditoría transversal (AOP) que registra login y consultas en la tabla `auditoria`
- [x] Logging estructurado con `correlation-id` propagado por petición
- [x] `@ControllerAdvice` — respuestas de error homogéneas
- [x] Swagger completo y documentado
- [x] Pruebas unitarias de capa de servicios (JUnit 5 + Mockito) sobre la lógica clave
- [x] Prueba de repositorio/controlador (`@WebMvcTest` / MockMvc)
- [x] Pruebas de componentes frontend con Vitest + `@testing-library/react`: `LoginPage`, `TablaExpedientes`, `ExpedienteDetalle`
- [x] Pruebas de hooks con `renderHook`: `useAuthStore` (login, logout) + interceptor de axios (token expirado)
- [x] Repaso de autorización por rol
- [x] README de arranque verificado y demo del MVP de principio a fin

**Aprende:** AOP/interceptores, pruebas backend (JUnit 5 + Mockito + `@WebMvcTest`), pruebas frontend (Vitest + `@testing-library/react` + `vi.mock`).

**Hecho cuando:** Las acciones relevantes quedan auditadas, la suite de pruebas pasa y el MVP se puede demostrar de principio a fin.

**Hitos diarios:**
- Día 1 — auditoría transversal (AOP) registrando login y consultas en tabla `auditoria` ✅
- Día 2 — logging estructurado con correlation-id y manejo de errores homogéneo (`@ControllerAdvice`) ✅
- Día 3 — pruebas unitarias de la capa de servicios (JUnit + Mockito) sobre la lógica clave ✅
- Día 4 — prueba de repositorio/controlador (`@WebMvcTest`), pruebas de componentes y hooks frontend con Vitest + Testing Library, repaso de autorización por rol ✅
- Día 5 — README de arranque, repaso final y demo del MVP completa ✅

**🏁 Hito M6 — Resultado demostrable:** MVP cerrado: auditoría, pruebas, documentación y demo

> **Nota:** el proyecto no tiene un hook `useAuth` separado — la sesión vive en el store de Zustand `useAuthStore`, que es lo que se probó con `renderHook`. El componente `LoginForm` mencionado originalmente es en realidad `LoginPage` (no hay un subcomponente de formulario aislado).

---

## ⚠️ Nota de ritmo y riesgos (retrospectiva Fase 0, ya cerrada)

- **Semana 4 es la más densa** (modelo de datos + búsqueda completa). Si hay retraso, lo natural es mover la tabla de resultados del frontend a la semana 5 y deslizar el resto. No recortar la semana 6 — auditoría y pruebas son parte del entregable, no un extra.
- **Spring Security + JWT (semana 2)** suele llevar más tiempo del esperado la primera vez. Reservar margen.
- **No dejar la internacionalización para el final**: integrarla desde la semana 3 evita tener que "retraducir" pantallas ya hechas.
- **Escribir pruebas a medida que se avanza**, no todas al final, aunque el grueso se consolide en la semana 6.

---

## Fase 1 — Tramitación ⏳

**Objetivo:** El personal instructor puede hacer avanzar los expedientes.

- Asignación de instructor a un expediente (rol `RESPONSABLE`)
- Registro de trámites según flujo:
  ```
  REGISTRO → ASIGNACION → REVISION → PROPUESTA → RESOLUCION → NOTIFICACION
  ```
- Cambio de estado del expediente asociado a cada trámite
- Sección **Tramitación** visible en el detalle del expediente
- Regla de negocio: un instructor solo puede tramitar expedientes asignados a él (validado en backend)
- Notificación al interesado en su `idioma_notificacion`

---

## Fase 2 — Alta y registro de solicitudes ⏳

**Objetivo:** Crear nuevos expedientes y solicitudes desde la aplicación.

- Formulario de alta de expediente con validaciones
- Gestión de interesados: solicitante, representante (opcional), causantes
- Datos de contacto y aviso
- Formulario específico por tipo de ayuda (ASIG_DISCAP / PAGO_NAC / PARTO_MULT)

---

## Fase 3 — Resolución y notificaciones ⏳

**Objetivo:** Generación de documentos y comunicación al interesado.

- Generación de documentos de resolución (favorable/desfavorable)
- Notificaciones por canal configurado: EMAIL / SMS / POSTAL
- Idioma del documento según `datos_contacto.idioma_notificacion`

---

## Fase 4 — Reglas de negocio y cálculo de cuantías ⏳

**Objetivo:** Validación automática de requisitos y cálculo orientativo de prestaciones.

| Ayuda | Regla |
|-------|-------|
| ASIG_DISCAP | Grado ≥ 33% (< 18 años) o ≥ 65% (≥ 18 años); cuantía especial si ≥ 75% + necesita tercera persona |
| PAGO_NAC | Validación de ingresos anuales + supuesto habilitante; reducción parcial en franja de ingresos |
| PARTO_MULT | Cálculo = 4/8/12 × SMI según nº de nacidos (2/3/4+) |

- Incompatibilidad entre progenitores (arts. 361–362 TRLGSS)
- Incompatibilidad con prestaciones análogas de otros regímenes públicos

---

## Entregables por fase

Cada fase cierra con:
1. Código en rama Git con PR revisado
2. Migraciones Flyway aplicadas y versionadas
3. Swagger UI operativo y probado
4. Suite de pruebas en verde
5. Demo funcional de lo entregado
