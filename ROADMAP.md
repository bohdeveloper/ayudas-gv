# ROADMAP — Sistema de Gestión de Ayudas GV

---

## Fase 0 — MVP (6 semanas) 🔄

**Objetivo:** Una persona autenticada puede buscar y consultar expedientes organizados por secciones.

### Semana 1 — Entorno, esqueleto y base de datos
- [ ] JDK 17, Node 20, Docker instalados
- [ ] Docker Compose con PostgreSQL levantado
- [ ] Backend Spring Boot arranca y conecta a BD
- [ ] Frontend React + Vite arranca
- [ ] Flyway: tablas de seguridad + catálogos con datos semilla bilingües
- [ ] Swagger UI accesible en `/api/swagger-ui.html`

**Hito M1:** Entorno + BD listos — backend y frontend arrancan, migraciones aplicadas, Swagger visible

### Semana 2 — Autenticación (backend)
- [ ] Spring Security configurado
- [ ] Repositorio de usuario con SQL (JdbcTemplate)
- [ ] `POST /api/v1/auth/login` → emite JWT firmado
- [ ] Filtro de validación del token en cada petición protegida
- [ ] Endpoint protegido de prueba (200 con token, 401 sin él)
- [ ] Swagger con esquema Bearer configurado

**Hito M2:** Login con JWT — credenciales válidas devuelven token; endpoint protegido responde correctamente

### Semana 3 — Login en frontend + idiomas
- [ ] Pantalla de login en React
- [ ] Interceptor HTTP (axios) que envía el token en cada petición
- [ ] Rutas protegidas + logout + manejo de expiración de sesión
- [ ] `react-i18next` configurado con ficheros `es.json` / `eu.json`
- [ ] Selector de idioma con persistencia en `localStorage`

**Hito M3:** Login UI + idiomas — inicio de sesión desde UI, rutas privadas protegidas, cambio de idioma en caliente

### Semana 4 — Modelo de expedientes + búsqueda
- [ ] Migraciones: `persona`, `expediente`, `expediente_interesado`, `datos_contacto`, `solicitud_ayuda`, tablas de detalle
- [ ] Datos de ejemplo insertados (5+ expedientes variados)
- [ ] `GET /api/v1/expedientes` con filtros (número, tipo, estado, documento, fechas), paginación y ordenación
- [ ] Descripción de catálogo devuelta en el idioma de la petición
- [ ] Formulario de filtros + tabla de resultados paginada en frontend

**Hito M4:** Búsqueda funcional — expedientes filtrables, paginados y en el idioma activo

### Semana 5 — Consulta del expediente por secciones
- [ ] `GET /api/v1/expedientes/{id}/generales`
- [ ] `GET /api/v1/expedientes/{id}/interesados`
- [ ] `GET /api/v1/expedientes/{id}/contacto`
- [ ] `GET /api/v1/expedientes/{id}/ayuda` (diferenciado por tipo de ayuda)
- [ ] Vista de detalle con pestañas/paneles que cargan cada sección de forma independiente
- [ ] Etiquetas de la UI internacionalizadas

**Hito M5:** Consulta por secciones — expediente navegable por pestañas, todo traducido

### Semana 6 — Transversales, pulido y demo
- [ ] Auditoría transversal (AOP) → tabla `auditoria` (login + consultas)
- [ ] Logging estructurado con `correlation-id` propagado por petición
- [ ] `@ControllerAdvice` — respuestas de error homogéneas
- [ ] Pruebas unitarias de capa de servicios (JUnit 5 + Mockito) ≥ 70% cobertura
- [ ] Prueba de controlador (`@WebMvcTest` / MockMvc)
- [ ] Prueba de componente frontend (React Testing Library)
- [ ] Swagger completo y probado
- [ ] README de arranque verificado
- [ ] Demo del MVP de principio a fin

**Hito M6:** MVP cerrado — auditoría activa, suite de pruebas verde, documentación completa

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
