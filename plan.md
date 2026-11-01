# plan.md — Sistema de Gestión de Ayudas GV

> Plan de trabajo vivo (Spec-Driven Development). Contexto y metodología en [spec.md](spec.md).
> Reglas: nada se implementa sin su punto aquí · al terminar se marca `[x]` con fecha ·
> las tareas grandes se desglosan en bloques antes de empezar.
> El detalle punto por punto de lo completado vive en el historial de git y en [docs/reportes/](docs/reportes/).
> Marcadores de estado en texto (sin emojis): **COMPLETADA · EN CURSO · PENDIENTE**.

---

## EN PAUSA (2026-11-01)

El proyecto queda **parado temporalmente** desde el 2026-11-01: el autor empieza unas prácticas a jornada
completa hasta principios de febrero de 2027 y cancela la suscripción de Claude durante ese tiempo.
Nada se ha borrado ni hay nada desplegado (ni nube ni cobros); el clúster de minikube no existe en este equipo.

**Para retomar (checklist):**
- [ ] `git pull` y leer este plan + [spec.md](spec.md) §3 y §6
- [ ] Corregir `JAVA_HOME` → `C:\Program Files\Java\jdk-21.0.9` y comprobar `mvn -v`
- [ ] Docker Desktop + `docker compose up --build`; pasar `mvn test` (107) y `npm test` en `frontend/`
- [ ] Revisar si hay versiones nuevas de Spring Boot 4.x / Spring AI 2.x antes de abrir la Etapa 5
- [ ] Reindexar el grafo (`codebase-memory-mcp`) y rehacer [docs/reportes/itinerario.md](docs/reportes/itinerario.md)
- [ ] Seguir por **Etapa 4 — Bloque 4.2**

---

## Cómo está organizado este plan (reordenado 2026-10-21)

Hay **una sola secuencia de Etapas**, que es el orden real de ejecución. No hay fases paralelas.

- Las **fases del enunciado** (Fase 0–4 y la evolución a microservicios) conservan su nombre y su orden
  original: son la columna vertebral académica del proyecto.
- Las **tecnologías nuevas** de la hoja de ruta del portfolio (Spring AI, MCP, hexagonal, Kafka, WebFlux,
  OpenShift) no son fases aparte: entran **de una en una**, en la etapa donde resuelven un problema del
  dominio. Regla anti-colapso: **ninguna etapa introduce más de una tecnología nueva** y cada una se cierra
  (pruebas en verde + demo) antes de abrir la siguiente.
- Reparto de la ruta de formación entre proyectos: **ayudas_gv** = stack senior · **REXIA** = sector público ·
  **BAKO** = agentes.

## Estado actual (2026-10-21)

| Etapa | Contenido | Tecnología nueva | Estado |
|-------|-----------|------------------|--------|
| 1 | Fase 0 — MVP (S1–S6) | Spring Boot, JWT, SQL explícito, i18n | COMPLETADA (2026-07-17) |
| 2 | Fase K8s — Docker + Kubernetes | Kubernetes (minikube) | COMPLETADA (2026-07-29) |
| 3 | Fase 1 — Tramitación | — | COMPLETADA (2026-07-29) |
| **4** | **Puesta al día de la plataforma** | Spring Boot 4 | **EN CURSO (en pausa desde 2026-11-01)** — Boot 4 hecho; falta el clúster |
| 5 | Asistente de normativa | Spring AI (RAG) + servidor MCP | PENDIENTE — **siguiente** |
| 6 | Fase 2 — Alta y registro de solicitudes | — (consolidación) | PENDIENTE |
| 7 | Fase 3 — Resolución y notificaciones | — | PENDIENTE |
| 8 | Fase 4 — Reglas de negocio y cuantías | — | PENDIENTE |
| 9 | Microservicios I — `ms-tramitacion` | Arquitectura hexagonal | PENDIENTE |
| 10 | Microservicios II — notificaciones asíncronas | Kafka | PENDIENTE |
| 11 | Microservicios III — `gateway` | WebFlux (Spring Cloud Gateway) | PENDIENTE |
| 12 | Despliegue final | OpenShift | PENDIENTE |

**Por qué este orden** (coherencia académica):

- **4 antes que nada:** Spring AI 2.x se compila contra Spring Boot 4.1. Hacer la IA sobre Boot 3.5 obligaba
  a migrar dos veces. Y el clúster tiene que volver a existir antes de añadir piezas nuevas.
- **5 justo tras la plataforma:** decisión del usuario (2026-10-13, "tras K8s"). Además encaja académicamente:
  el asistente obliga a estudiar los arts. 351–362 y 373 del TRLGSS, que son exactamente las reglas que la
  etapa 8 implementa. Trabaja solo sobre normativa pública, así que no depende de las fases 2–4.
- **6–8 en el orden del enunciado,** sin tecnología nueva: completan el dominio. Sin alta, resolución y reglas
  no hay nada que valga la pena repartir en microservicios.
- **9–11 = la "evolución a microservicios" del enunciado §4,** ahora con hexagonal, Kafka y WebFlux. Cada una
  rehace algo que ya funciona en el monolito, lo que permite comparar antes/después (lo más valioso para
  explicarlo en una entrevista).
- **12 al final:** OpenShift solo tiene sentido cuando hay varios servicios que desplegar.

**Próxima sesión — Etapa 4, Bloque 4.2:** instalar `minikube` + `kubectl` en este equipo (no están; el clúster
de julio vivía en otra instalación) y desplegar la imagen con Spring Boot 4.

- **Herramental verificado (2026-10-21):** Docker Desktop, Java 21 (`C:\Program Files\Java\jdk-21.0.9`),
  Maven 3.9, Node. **Ojo:** la variable de sistema `JAVA_HOME` apunta a `C:\eclipse\` y Maven falla desde la
  terminal hasta corregirla. **Faltan** `minikube` y `kubectl`.
- [docs/reportes/itinerario.md](docs/reportes/itinerario.md) es histórico hasta el 29/07; sus fechas de agosto ya
  no valen. Se rehace al empezar la Etapa 5 con este orden.

---

## Etapa 4 — Puesta al día de la plataforma — EN CURSO

> **Objetivo:** dejar la base técnica al día antes de añadir tecnología nueva: Spring Boot con soporte y el
> clúster de Kubernetes funcionando otra vez en este equipo con esa versión.

### Bloque 4.1 — Migración a Spring Boot 4 — COMPLETADA (2026-10-21)
- [x] Spring Boot 3.5.16 → **4.1.1** (misma versión con la que se compila Spring AI 2.0.1), springdoc 2.7.0 →
  3.1.1, jjwt 0.12.5 → 0.13.0 — 2026-10-21
- [x] Starters renombrados o divididos en Boot 4: `web` → `webmvc`, `aop` → `aspectj`, `flyway-core` suelto →
  `spring-boot-starter-flyway` (sin él Flyway ya no migra al arrancar). Los *test slices* pasan a módulos
  propios (`webmvc-test`, `jdbc-test`, `flyway-test`, `security-test`) y cambian de paquete — 2026-10-21
- [x] Jackson 3: en las pruebas, `ObjectMapper` pasa a `tools.jackson.databind` (las anotaciones siguen en
  `com.fasterxml.jackson.annotation`) — 2026-10-21
- [x] Limpieza de avisos del IDE: `@MockBean` → `@MockitoBean`, imports sin uso, `@Bean` sin `public` — 2026-10-21
- [x] **Backend 107 pruebas en verde** y arranque real en Docker Compose contra PostgreSQL: Flyway hasta V6,
  probes de Actuator 200, Swagger 200, 401 sin token, login + búsqueda vía Nginx, errores en euskera — 2026-10-21

### Bloque 4.2 — Kubernetes otra vez en marcha (el tramo de K8s que queda) — PENDIENTE
> La Fase K8s se cerró entera el 2026-07-29 (Bloques 0–4, ver Histórico). Lo que queda no es K8s
> sin terminar, sino **reconstruir el clúster en este equipo** y desplegar en él la versión nueva. Sirve de
> repaso de memoria de toda la Fase K8s.
- [ ] Instalar `minikube` + `kubectl` (`winget install Kubernetes.minikube Kubernetes.kubectl`) y
  `minikube start --driver=docker`; `addons enable ingress`
- [ ] Construir `ayudas-gv-backend:3.0` (Boot 4) y `ayudas-gv-frontend:3.0` dentro del Docker de minikube
- [ ] `kubectl apply -f k8s/` → `ayudas-dev` con los 3 Pods `Running` y las probes de Actuator en verde
- [ ] Ejercicio de repaso: desplegar primero `:2.2`/`:2.0` y hacer **rolling update a `:3.0`** con `change-cause`
  (Boot 3 → Boot 4 sin caída), comprobar `rollout history` y practicar `rollout undo`
- [ ] Desplegar `ayudas-staging` ([k8s/staging/](k8s/staging/)) y verificar el aislamiento entre los dos entornos
- [ ] Login + búsqueda + tramitación de extremo a extremo por el Ingress, en es y eu
- [ ] Actualizar las etiquetas de imagen en `k8s/` y la tabla de herramental de este plan

**Hecho cuando:** el stack con Spring Boot 4 corre en minikube (dev y staging) y la actualización desde la
versión anterior se ha hecho con rolling update, sin caída.

---

## Etapa 5 — Asistente de normativa con Spring AI (RAG + MCP) — PENDIENTE (siguiente)

> **Objetivo:** que un usuario interno pregunte dudas sobre las 3 ayudas y reciba una respuesta
> **fundamentada en la normativa (TRLGSS arts. 351–362 y 373) con las fuentes citadas**, en es/eu; y exponer
> ese mismo asistente como **servidor MCP** para agentes de IA.
> **Decisiones del usuario (2026-10-13):** LLM Anthropic Claude · vector store PGvector · alcance solo
> normativa pública (sin acceso del LLM a expedientes). Registradas en [spec.md](spec.md) §3, invariante 11.
> **Versión:** Spring AI 2.0.1 (BOM `spring-ai-bom`), alineada con Spring Boot 4.1.1.

**Punto abierto a cerrar en el Bloque 5.0:** Anthropic **no ofrece modelo de embeddings**, así que el RAG
necesita un `EmbeddingModel` aparte. Opciones: ONNX local multilingüe con `spring-ai-starter-model-transformers`
(sin clave extra y los datos no salen del equipo; recomendado por defecto), Ollama o un proveedor externo.
Validar la calidad en euskera con unas preguntas de prueba antes de fijarlo.

### Bloque 5.0 — Fundamentos y preparación
- [ ] Leer la documentación oficial de Spring AI (ChatClient, advisors, RAG, MCP) y anotar dudas
- [ ] Elegir el modelo de embeddings (punto abierto de arriba)
- [ ] Reunir la **fuente normativa** en texto (arts. 351–362 y 373 TRLGSS, es y eu si existe) en `docs/normativa/`
- [ ] API key de Anthropic: variable `ANTHROPIC_API_KEY` (Compose + Secret en K8s), nunca en el repo

### Bloque 5.1 — Chat básico con `ChatClient`
- [ ] `spring-ai-starter-model-anthropic` + configuración en `application.yml` (modelo, temperatura, max-tokens)
- [ ] Módulo `asistente` (paquete propio, frontera limpia) con prompt de sistema: solo normativa, citar
  artículos, responder en el idioma de la petición
- [ ] `POST /api/v1/asistente/consultas` protegido (rol `CONSULTOR` o superior), validación y límite de longitud
- [ ] Prueba con `ChatModel` simulado (sin llamar a la API real) y documentación en Swagger

### Bloque 5.2 — RAG con PGvector
- [ ] Imagen `postgres:15` → `pgvector/pgvector:pg15` en `docker-compose.yml`, `k8s/20-postgres.yaml` y
  `k8s/staging/ayudas-staging.yaml`
- [ ] Migración Flyway V7: `CREATE EXTENSION vector` + tabla de embeddings (esquema por Flyway, no por
  `initialize-schema`)
- [ ] Ingesta: lector de `docs/normativa/` → `TokenTextSplitter` → `VectorStore.add` (idempotente, lanzada por ADMIN)
- [ ] `QuestionAnswerAdvisor` con `topK` y umbral de similitud; respuesta con **fuentes** (artículo y fragmento)
- [ ] Si la normativa no contiene la respuesta, decirlo con honestidad, sin inventar
- [ ] Pruebas: ingesta y recuperación contra PGvector (Testcontainers) y preguntas de prueba en es/eu

### Bloque 5.3 — Integración en la app
- [ ] Panel de chat en el frontend (i18n es/eu, sistema de diseño existente, iconos SVG propios, carga y errores)
- [ ] Auditoría de cada consulta en `auditoria` (usuario, idioma, correlation-id; **sin** datos personales en logs)
- [ ] Límites de coste y abuso: rate limiting por usuario y tope de tokens
- [ ] Evaluación: batería de ~15 preguntas con respuesta esperada y revisión manual de las citas

### Bloque 5.4 — Servidor MCP
- [ ] `spring-ai-starter-mcp-server-webmvc` con una tool `consultar_normativa` (mismos datos públicos que el RAG)
- [ ] Autenticación del endpoint MCP con el JWT existente; auditoría de cada invocación
- [ ] Demo: Claude Code o Claude Desktop conectado al MCP de Ayudas GV, respondiendo con citas
- [ ] Tools sobre expedientes: fuera de alcance; solo con decisión explícita (invariante 11) y `/security-review`

### Cierre de la etapa
- [ ] `/code-review` + `/security-review` (endpoint nuevo, API key, entrada de usuario hacia un LLM), verificación
  end-to-end en Compose y en minikube
- [ ] README, `docs/arquitectura.md` y un texto breve para CV/LinkedIn que describa solo lo que existe

**Hecho cuando:** desde la UI, un usuario pregunta p. ej. "¿qué grado de discapacidad exige la asignación
económica?" y recibe una respuesta correcta con los artículos citados, en el idioma elegido y con la consulta
auditada; la misma pregunta funciona desde un cliente MCP; y el usuario explica de memoria el flujo
ingesta → embeddings → búsqueda → prompt → respuesta.

---

## Etapa 6 — Fase 2: Alta y registro de solicitudes — PENDIENTE

> **Objetivo:** crear nuevos expedientes y solicitudes desde la aplicación. Fuente: enunciado §7 (Fase 2).
> Sin tecnología nueva a propósito: etapa de consolidación sobre los patrones ya probados en la Fase 1
> (formularios condicionados por rol, errores del backend ya traducidos).

- [ ] **Decidir con el usuario qué roles pueden dar de alta** (única pregunta abierta; hacerlo antes de empezar)
- [ ] Formulario de alta de expediente con validaciones (cliente + servidor)
- [ ] Gestión de interesados: `SOLICITANTE`, `REPRESENTANTE` (opcional), `CAUSANTE`(s)
- [ ] Datos de contacto y aviso (canal, idioma de notificación)
- [ ] Formulario específico por tipo de ayuda: `ASIG_DISCAP` / `PAGO_NAC` / `PARTO_MULT`
- [ ] Persistencia con SQL parametrizado en los repositorios correspondientes
- [ ] Autorización en backend según la decisión del primer punto

---

## Etapa 7 — Fase 3: Resolución y notificaciones — PENDIENTE

> **Objetivo:** generación de documentos y comunicación al interesado. Fuente: enunciado §7 (Fase 3).
> Recoge la casilla que quedó abierta en la Fase 1 (emitir la notificación, no solo registrarla).
> Se hace **síncrona y dentro del monolito**; la Etapa 10 la convierte en asíncrona con Kafka.

- [ ] Generación de documentos de resolución (favorable / desfavorable)
- [ ] Notificaciones por el canal configurado: `EMAIL` / `SMS` / `POSTAL` (integraciones reales simuladas en local)
- [ ] Idioma del documento según `datos_contacto.idioma_notificacion` (independiente de la interfaz)
- [ ] Traza en `auditoria` de la emisión de resolución y de la notificación

---

## Etapa 8 — Fase 4: Reglas de negocio y cálculo de cuantías — PENDIENTE

> **Objetivo:** validación automática de requisitos y cálculo orientativo de prestaciones.
> Fuente: enunciado §7 (Fase 4) + Anexo A. La normativa reunida en la Etapa 5 es la referencia.

| Ayuda | Regla de validación / cálculo |
|-------|-------------------------------|
| `ASIG_DISCAP` | Grado ≥ 33 % (menor de 18) o ≥ 65 % (mayor de edad); cuantía especial si ≥ 75 % + necesita tercera persona; requiere residencia legal y no tener prestación análoga |
| `PAGO_NAC` | Supuesto habilitante (familia numerosa / monoparental / progenitor con discapacidad ≥ 65 %) + límite de ingresos anuales (reducción parcial en franja); referencia 1.000 € |
| `PARTO_MULT` | Cuantía = 4 / 8 / 12 × SMI según nº de nacidos (2 / 3 / 4+) |

- [ ] Validación de requisitos por tipo de ayuda antes de proponer/resolver
- [ ] Cálculo orientativo de cuantías por ayuda
- [ ] Incompatibilidad entre progenitores (solo una persona beneficiaria) — arts. 361–362 TRLGSS
- [ ] Incompatibilidad con prestaciones análogas de otros regímenes públicos

---

## Etapas 9–11 — Evolución a microservicios (enunciado §4)

> El enunciado prevé pasar de monolito modular a microservicios. Se hace en tres etapas, cada una con
> **una** tecnología nueva, y cada servicio se despliega como Deployment + Service en `k8s/`.
> Reglas fijadas en [spec.md](spec.md) §3, invariante 12. Cada etapa se desglosa en bloques al empezarla.

### Etapa 9 — Microservicios I: `ms-tramitacion` con arquitectura hexagonal — PENDIENTE
- [ ] Extraer `ms-tramitacion` (Maven multi-módulo o repo propio, a decidir) con capas `domain` / `application`
  (puertos de entrada y salida) / `infrastructure` (adaptadores REST y JDBC)
- [ ] El adaptador de persistencia sigue siendo `NamedParameterJdbcTemplate` con SQL explícito (invariante 1)
- [ ] Dominio sin dependencias de Spring; reglas (instructor asignado, reglas de la Etapa 8) probadas sin contexto
- [ ] Prueba de arquitectura con ArchUnit que impida que `domain` dependa de `infrastructure`
- [ ] Deployment + Service propios en `k8s/`

### Etapa 10 — Microservicios II: notificaciones asíncronas con Kafka — PENDIENTE
- [ ] Kafka en Compose (modo KRaft, sin ZooKeeper) y en K8s con el operador Strimzi
- [ ] `ms-tramitacion` publica `TramiteRegistrado` / `ExpedienteResuelto` con **patrón outbox** (tabla por Flyway
  + SQL explícito)
- [ ] `ms-notificaciones` consume y notifica en `idioma_notificacion`, sustituyendo el envío síncrono de la Etapa 7
- [ ] Idempotencia del consumidor, reintentos y dead-letter topic; pruebas con Testcontainers

### Etapa 11 — Microservicios III: `gateway` reactivo con WebFlux — PENDIENTE
- [ ] `gateway` con Spring Cloud Gateway (WebFlux, versión de Spring Cloud compatible con Boot 4.1): enrutado
  `/api/v1/**`, validación del JWT y propagación del correlation-id
- [ ] Llamadas entre servicios con `WebClient`; timeouts y circuit breaker (Resilience4j)
- [ ] Los servicios JDBC existentes **no** se reescriben en reactivo; si un servicio reactivo necesita BD, R2DBC
  `DatabaseClient` con SQL explícito (invariante 1)
- [ ] Pruebas con `WebTestClient` y `StepVerifier`
- [ ] Extraer `ms-autenticacion` / `ms-expedientes` solo si aporta valor (decidir aquí)

---

## Etapa 12 — Despliegue final en OpenShift — PENDIENTE

> **Objetivo:** llevar el sistema completo (servicios + Kafka + PostgreSQL) a OpenShift y saber explicar en qué
> se diferencia de Kubernetes "a pelo".

- [ ] Entorno: OpenShift Local (CRC) o Developer Sandbox de Red Hat (gratis)
- [ ] Imágenes compatibles con la SCC `restricted` (UID arbitrario, no root): `nginx-unprivileged` en 8080 para el
  frontend y permisos de grupo 0 en los servicios Java
- [ ] `Route` en lugar de `Ingress`; Kustomize con overlays `minikube` / `openshift` sobre la base de `k8s/`
- [ ] Comparar por escrito K8s vs OpenShift (Routes, SCC, Projects, ImageStreams, consola) para la entrevista

**Hecho cuando** (de toda la ruta): cada etapa tiene su demo y una línea de CV que se pueda defender con el
código delante.

---

## Entregables por etapa

Cada etapa funcional o tecnológica cierra con:
1. Código en rama Git con PR revisado (`/code-review` sobre el diff; `/security-review` si toca auth/datos).
2. Migraciones Flyway aplicadas y versionadas.
3. Swagger UI operativo y probado.
4. Suite de pruebas en verde.
5. Despliegue verificado en Compose y en minikube.
6. Demo funcional de lo entregado y reporte en `docs/reportes/`.

---

## Histórico de etapas completadas

<details>
<summary><b>Etapa 3 — Fase 1: Tramitación — COMPLETADA (2026-07-29)</b></summary>

> **Objetivo cumplido:** el personal instructor puede hacer avanzar los expedientes.
> Fuente: enunciado §7.2. Tablas `tramite` y `tipo_tramite` en [docs/modelo-datos.md](docs/modelo-datos.md).

```
REGISTRO/Entrada  → expediente registrado
ASIGNACION        → se asigna instructor
REVISION/Subsanación → comprobación de requisitos; si falta algo, se requiere subsanación
PROPUESTA         → propuesta de resolución (favorable | desfavorable)
RESOLUCION        → concesión o denegación (rol RESPONSABLE)
NOTIFICACION      → comunicación al interesado por su canal de aviso y en su idioma de notificación
```

- [x] Asignación de instructor (rol `RESPONSABLE`) — `PUT /api/v1/expedientes/{id}/instructor`, transaccional
  (asigna + cambia estado + trámite `ASIGNACION`); valida instructor **activo** (409 si no). `GET /api/v1/instructores`
- [x] Registro de trámites — `POST /api/v1/expedientes/{id}/tramites`
- [x] Cada trámite guarda quién, cuándo y resultado — autor del JWT y reloj del servidor; `@Auditable(accion="TRAMITE")`
- [x] Cambio de estado por trámite — `REGISTRO`→REGISTRADO, `REVISION`/`PROPUESTA`→EN_REVISION,
  `RESOLUCION`→RESUELTO (+ `fecha_resolucion`), `NOTIFICACION`→sin cambio
- [x] Regla: **un instructor solo tramita lo asignado** — doble capa (`@PreAuthorize` por rol + servicio por dato);
  probada en el clúster: asignado 201, no asignado 403 con motivo en es/eu
- [x] Sección **Tramitación** en el detalle — 5.ª pestaña con línea de tiempo y formularios por rol
- [x] 4 endpoints documentados en Swagger (403/404/409 explicados); tabla en [docs/arquitectura.md](docs/arquitectura.md)
- [x] Pruebas: backend 107 (era 52), frontend 26 (era 16)
- [x] Extra: bug de i18n del bundle raíz `messages.properties` cazado en el clúster; corregido + `MessagesParidadTest`
- La emisión real de la notificación pasó a la Etapa 7 (Fase 3): en la Fase 1 el trámite `NOTIFICACION` se
  registra, pero no se envía nada.

</details>

<details>
<summary><b>Higiene del repositorio — COMPLETADA (2026-07-28)</b></summary>

- [x] Snapshot del grafo (`graph.db.zst`, `artifact.json`) fuera de git: cada commit lo re-ensuciaba; 6,5 MB de 10,58 MB
- [x] Política "cola de git limpia" en `CLAUDE.md` + hook `Stop` (`.claude/hooks/git-cola-limpia.sh`)
- [x] `docs/` reorganizado: [docs/docker-k8s/](docs/docker-k8s/), [docs/diagramas/](docs/diagramas/) e índice [docs/README.md](docs/README.md)
- [x] Material de la reunión fusionado en [docs/docker-k8s/entrevista-docker-k8s.md](docs/docker-k8s/entrevista-docker-k8s.md)

</details>

<details>
<summary><b>Etapa 2 — Fase K8s: Docker + Kubernetes — COMPLETADA (2026-07-29)</b></summary>

> **Objetivo cumplido:** llevar Ayudas GV de `docker compose` a minikube y dominar escalar, autocurar y actualizar
> sin caída. Guía: [docs/docker-k8s/kubernetes-aprendizaje.md](docs/docker-k8s/kubernetes-aprendizaje.md).
> Entregables: carpeta [k8s/](k8s/), ejercicios marcados y los documentos divulgativos.

**Bloque 0 — Repaso de Docker (2026-07-22)**
- [x] Stack con `docker compose up --build`: 3 contenedores, Flyway V1–V5, login vía Nginx
- [x] Multi-stage builds explicados en [docs/docker-k8s/docker-guia.md](docs/docker-k8s/docker-guia.md) §3
- [x] Red por nombre de servicio (base del DNS de K8s), demostrada en vivo

**Bloque 1 — Clúster local (2026-07-23)**
- [x] `.dockerignore` en backend y frontend
- [x] minikube v1.38.1 / K8s v1.35.1 (driver docker); nodo Ready
- [x] Imágenes construidas dentro del Docker de minikube; entendido el `ImagePullBackOff`

**Bloque 2 — Desplegar la app (2026-07-24)**
- [x] `k8s/00`–`50`: namespace `ayudas-dev`, ConfigMap + Secret, postgres (PVC), backend, frontend, Ingress
- [x] 3 Pods Running, login de extremo a extremo; DNS interno demostrado; fallo de Secret diagnosticado

**Bloque 3 — Operar (2026-07-27)**
- [x] Escalado a 3 réplicas con balanceo real; self-healing sin afectar al frontend
- [x] Rolling update `:1.0`→`:1.1` sin caída y `rollout undo`
- [x] Postgres con 2 réplicas sobre un PVC RWO: dos postmasters sobre el mismo directorio → conclusión:
  `ReadWriteOncePod`, StatefulSet no da replicación (operador tipo CloudNativePG)

**Bloque 4 — Ejercicios integradores (2026-07-29)**
- [x] Segundo entorno `ayudas-staging` ([k8s/staging/](k8s/staging/))
- [x] Probes de Actuator (startup / readiness con BD / liveness sin BD)
- [x] Resiliencia JWT stateless con un Pod borrado; `resources.requests/limits`
- [x] Postgres endurecido (`Recreate` + `ReadWriteOncePod`; el `spec` del PVC es inmutable)
- [x] `change-cause` en los Deployments; respuestas orales preparadas

**Material didáctico (2026-07-28)**
- [x] Guía reescrita con 25 analogías y [docs/docker-k8s/kubernetes-desde-0.md](docs/docker-k8s/kubernetes-desde-0.md)

</details>

<details>
<summary><b>Etapa 1 — Fase 0: MVP (S1–S6) — COMPLETADA (Hito M6, 2026-07-17)</b></summary>

**Objetivo cumplido:** una persona autenticada puede buscar y consultar expedientes organizados por
secciones, en castellano y euskera. Construido como monolito modular con fronteras limpias.

**Seguimiento de ramas (todas mergeadas a `main`):**

| Rama | Semana | Hito |
|------|--------|------|
| `main` | S1 — Setup | M1 · 2026-06-09 |
| `feat/semana2-auth` | S2 — Auth backend | M2 · 2026-06-12 |
| `feat/semana3-login-frontend` | S3 — Login UI + i18n | M3 · 2026-06-26 |
| `feat/semana4-busqueda` | S4 — Modelo + búsqueda | M4 · 2026-07-03 |
| `feat/semana5-detalle-secciones` | S5 — Detalle por secciones | M5 · 2026-07-10 |
| `feat/semana6-transversales` | S6 — Transversales + demo | M6 · 2026-07-17 |

**Qué se construyó, semana a semana:**

- **S1 — Entorno, esqueleto y BD.** Scaffold backend (Spring Boot 3.5, Java 21) + frontend (React 18 +
  Vite + TS). Docker Compose con 3 servicios. Flyway V1: seguridad (`usuario`, `rol`, `usuario_rol`) y
  catálogos (`tipo_ayuda`, `estado_expediente`, `tipo_tramite`) con semilla bilingüe. i18n base
  (messages_es/eu, es/eu.json). Dockerfiles multi-stage. Swagger UI accesible.
- **S2 — Autenticación (backend).** Spring Security (cadena de filtros, rutas públicas/protegidas).
  `UsuarioRepository` con SQL (`NamedParameterJdbcTemplate`, JOIN roles). `POST /api/v1/auth/login` valida
  con BCrypt y emite JWT (HS384). `OncePerRequestFilter` valida el token. Endpoint protegido + Swagger Bearer.
- **S3 — Login frontend + idiomas.** Rutas y layout, axios con interceptor JWT. Pantalla de login, rutas
  protegidas, logout, redirección al expirar. `react-i18next` (es/eu), selector con persistencia en
  `localStorage` y conmutación en caliente.
- **S4 — Modelo de expedientes + búsqueda.** Migraciones V2: `persona`, `expediente`,
  `expediente_interesado`, `datos_contacto`, `solicitud_ayuda` y detalles, con datos de ejemplo.
  `GET /api/v1/expedientes` con filtros (número, tipo, estado, documento, fechas), paginación y orden en
  SQL parametrizado; descripción de catálogo según `Accept-Language`. Frontend: filtros + tabla paginada.
- **S5 — Consulta por secciones.** Endpoints/DTOs `/{id}/generales`, `/interesados`, `/contacto`, `/ayuda`
  (este último diferenciado por tipo de ayuda). Vista de detalle con pestañas que cargan cada sección de
  forma independiente; etiquetas i18n; enlace búsqueda→detalle.
- **S6 — Transversales, pulido y demo.** Auditoría AOP (login + consultas → `auditoria`), logging con
  correlation-id, `@ControllerAdvice` de errores homogéneos, Swagger completo. Pruebas backend
  (JUnit 5 + Mockito + `@WebMvcTest`) y frontend (Vitest + Testing Library: `LoginPage`,
  `TablaExpedientes`, `ExpedienteDetalle`; hooks con `renderHook` sobre `useAuthStore` + interceptor axios).
  Repaso de autorización por rol. README de arranque y demo end-to-end.

**Auditoría final vs enunciado (2026-07-17):** contraste migración por migración y endpoint por endpoint
contra el enunciado. Se cerraron 3 desviaciones: (1) §8.4 — `messages_es/eu.properties` no se usaban;
ahora los errores se resuelven vía `MessageSource` según `Accept-Language` (+ `messages.properties` raíz
para activar `MessageSourceAutoConfiguration`); (2) §10 — faltaban pruebas de repositorio/SQL contra BD
real: +25 pruebas `@JdbcTest` + H2 (modo PostgreSQL) ejecutando las migraciones Flyway reales; (3) un
literal de `LoginPage` no pasaba por i18next (clave `auth.tagline`). Resultado: backend 52 pruebas en
verde (27+25), frontend 16 pruebas y build sin cambios.

**Notas de la retrospectiva:** la S4 fue la más densa (modelo + búsqueda). Spring Security + JWT (S2) llevó
más tiempo del esperado. La i18n integrada desde S3 evitó "retraducir". Las pruebas se escribieron a medida,
consolidándose en S6. No hay hook `useAuth` separado: la sesión vive en `useAuthStore` (Zustand);
`LoginForm` es en realidad `LoginPage` (no hay subcomponente de formulario aislado).

</details>
