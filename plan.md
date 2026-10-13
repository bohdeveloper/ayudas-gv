# plan.md — Sistema de Gestión de Ayudas GV

> Plan de trabajo vivo (Spec-Driven Development). Contexto y metodología en [spec.md](spec.md).
> Reglas: nada se implementa sin su punto aquí · al terminar se marca `[x]` con fecha ·
> las tareas grandes se desglosan en fases antes de empezar.
> El detalle punto por punto de lo completado vive en el historial de git.
> Marcadores de estado en texto (sin emojis): **COMPLETADA · EN CURSO · PENDIENTE**.

---

## Estado actual (2026-07-29)

| Fase / hito | Estado |
|-------------|--------|
| **Fase 0 — MVP** (S1–S6) | COMPLETADA (Hito M6, 2026-07-17) |
| **Fase K8s — Docker + Kubernetes (learning track)** | COMPLETADA (2026-07-29, Bloques 0–4) |
| **Fase 1 — Tramitación** | COMPLETADA (2026-07-29) |
| **Fase IA — Spring AI (asistente RAG sobre normativa)** | PENDIENTE — **siguiente** (decidido 2026-10-13: tras K8s y antes de Fase 1; Fase 1 ya cerrada, así que va antes de la Fase 2) |
| Fase 2 — Alta y registro de solicitudes | PENDIENTE (tras Fase IA) |
| Fase 3 — Resolución y notificaciones | PENDIENTE |
| Fase 4 — Reglas de negocio y cálculo de cuantías | PENDIENTE |

**Próxima sesión — Fase IA (Spring AI), Bloque 0; después la Fase 2 (alta y registro de solicitudes).**
Punto de entrada de la Fase 2: formulario de alta con
validación en cliente y servidor, reutilizando el sistema de diseño y los patrones de la sección Tramitación
(formularios condicionados por rol, errores del backend ya traducidos). Antes de empezar, decidir con el
usuario **qué roles pueden dar de alta** — es la única casilla de la Fase 2 sin respuesta.

- **Estado del clúster (2026-07-29):** minikube con `ayudas-dev` desplegado a partir de las imágenes
  `ayudas-gv-backend:2.2` / `ayudas-gv-frontend:2.0`, probes de Actuator y el segundo entorno
  `ayudas-staging` en [k8s/staging/](k8s/staging/). Se levanta con `minikube start` + `kubectl apply -f k8s/`.
- **Herramental:** Docker Desktop + Compose, minikube v1.38.1 / K8s v1.35.1, Java 21, Maven 3.9, Node.
- Calendario de lo que queda: [docs/reportes/itinerario.md](docs/reportes/itinerario.md).

> **Decisión de secuencia (usuario, 2026-07-21):** primero dominar Docker + Kubernetes sobre este
> proyecto; la Fase 1 (Tramitación) después. Cumplido: ambas cerradas el 2026-07-29.

---

## Fase K8s — Docker + Kubernetes (learning track) — COMPLETADA (2026-07-29)

> **Objetivo:** llevar Ayudas GV de `docker compose` a un clúster de Kubernetes local (minikube) y
> dominar las operaciones de orquestación (escalar, autocurar, actualizar sin caída), como preparación
> para un nuevo proyecto profesional con este stack.
> **Método:** guía didáctica con teoría mínima + práctica + ejercicios en
> [docs/kubernetes-aprendizaje.md](docs/docker-k8s/kubernetes-aprendizaje.md). Este apartado es el checklist; el
> "cómo" y el "por qué" están en la guía.

### Bloque 0 — Repaso de Docker (consolidación) — COMPLETADA (2026-07-22)
- [x] Levantar el stack con `docker compose up --build` y verificar los 3 contenedores (ej. 1.1–1.4 de la guía) — 2026-07-22: 3 contenedores Up (postgres healthy), Flyway V1–V5 aplicadas, login `admin` OK vía Nginx
- [x] Explicar por escrito el multi-stage build de `backend/Dockerfile` y el de `frontend/Dockerfile` — 2026-07-22: [docs/docker-guia.md](docs/docker-k8s/docker-guia.md) §3.1–3.2
- [x] Interiorizar la red por nombre de servicio (`postgres`, `backend`) — base del DNS de K8s — 2026-07-22: [docs/docker-guia.md](docs/docker-k8s/docker-guia.md) §4 + demostrado en vivo (Flyway→`postgres:5432`, Nginx→`backend:8080`)

### Bloque 1 — Clúster local (minikube)
- [x] **Prep (2026-07-23):** `.dockerignore` en `backend/` y `frontend/` para reducir el contexto de build
  antes de construir dentro de minikube — verificado con build real (contexto 102,83 kB / 31,05 kB)
- [x] Instalar `minikube` + `kubectl` (Windows/winget) y `minikube start --driver=docker` — 2026-07-23: minikube v1.38.1, K8s v1.35.1 (driver docker, 2 CPU / 8100 MB)
- [x] `kubectl get nodes` → 1 nodo Ready — 2026-07-23: nodo `minikube` Ready (control-plane); explorado con `cluster-info` y `get pods -A` (7 Pods de `kube-system` Running)
- [x] Construir `ayudas-gv-backend:1.0` y `ayudas-gv-frontend:1.0` **dentro** del Docker de minikube — 2026-07-23: vía `docker-env`; backend 245 MB, frontend 62.7 MB
- [x] Entender por qué sin ese paso el Pod queda en `ImagePullBackOff` — 2026-07-23: dos demonios Docker separados; el nodo busca la imagen en su propio Docker, no en el de Windows

### Bloque 2 — Desplegar la app (crear `k8s/`) — COMPLETADA (2026-07-24)
- [x] `k8s/00-namespace.yaml` → namespace `ayudas-dev` y fijarlo como contexto por defecto — 2026-07-24
- [x] `k8s/10-backend-config.yaml` → ConfigMap (no sensible) + Secret (credenciales) desde `docker-compose.yml` — 2026-07-24
- [x] `k8s/20-postgres.yaml` → Deployment + PVC (persistencia) + Service ClusterIP `postgres` — 2026-07-24
- [x] `k8s/30-backend.yaml` → Deployment (envFrom Config+Secret, probes 8080) + Service `backend` — 2026-07-24
- [x] `k8s/40-frontend.yaml` → Deployment (Nginx:80) + Service `frontend` — 2026-07-24
- [x] `k8s/50-ingress.yaml` → Ingress (variante A: todo `/`→frontend, cuyo Nginx proxea `/api`→`backend:8080`) con addon ingress habilitado — 2026-07-24
- [x] Los 3 Pods en `Running`; login `admin/admin123` funcionando de extremo a extremo dentro del clúster — 2026-07-24: HTTP 200 + JWT vía port-forward al frontend y vía Ingress
- [x] Demostrar el DNS interno (`getent hosts postgres` desde el Pod del backend) — 2026-07-24: `postgres` → 10.111.20.147 (ClusterIP del Service)
- [x] Diagnosticar un fallo provocado (Secret con contraseña mal) con `describe` + `logs` — 2026-07-24: `FATAL: password authentication failed for user "ayudasgv"`; `describe` mostró Readiness probe failed + BackOff; arreglado restaurando el Secret

### Bloque 3 — Operar (la esencia de orquestar) — COMPLETADA (2026-07-27)
- [x] Escalar el backend a 3 réplicas y comprobar los 3 endpoints del Service — 2026-07-27: `kubectl scale` → 3 endpoints `true` en el EndpointSlice; balanceo real demostrado (6 logins repartidos 3/1/2 entre los Pods, vía logs `Login exitoso`)
- [x] Self-healing: matar un Pod y ver cómo el Deployment lo recrea (sin afectar al frontend) — 2026-07-27: Pod nuevo en 2s, recuperación completa en 77s, 4 logins HTTP 200 durante la ventana; el Pod moribundo pasó a `ready=false` en los endpoints antes de morir y el que arrancaba recibió 0 peticiones; frontend y postgres intactos
- [x] Rolling update `:1.0`→`:1.1` con `kubectl set image` y `rollout status`; luego `rollout undo` — 2026-07-27: imagen `:1.1` construida con `minikube image build` (derivada de `:1.0` + marca `APP_VERSION`); ReplicaSet nuevo `5b48dd7c5c`, surge de 1 Pod (maxSurge 25%→1, maxUnavailable 25%→0), cero caída; `undo` reinfló el RS viejo `fd8f6dd8b` y añadió la revisión 5
- [x] Razonar por qué postgres (PVC ReadWriteOnce) no se replica como el backend → StatefulSet — 2026-07-27: probado en vivo escalando a 2 réplicas. **RWO limita por nodo, no por Pod**: en un solo nodo ambos Pods montaron el PVC; los namespaces de PID hicieron que el 2.º postmaster (también PID 1) tomara el `postmaster.pid` ajeno por un lock huérfano suyo, lo borrara y arrancara → dos postmasters ~76s sobre el mismo directorio. Al bajar a 1 réplica, el original hizo `immediate shutdown because data directory lock file is invalid` y reinició. BD íntegra (Flyway V1–V5 `success`, datos OK). Conclusiones: `ReadWriteOncePod` sí lo impediría; el StatefulSet resuelve identidad y disco por Pod (`volumeClaimTemplates`) pero **no** da replicación de Postgres (eso es un operador tipo CloudNativePG o BD gestionada)

### Bloque 4 — Ejercicios integradores (blindaje entrevista) — COMPLETADA (2026-07-29)
- [x] Segundo entorno con namespace `ayudas-staging` (mismo stack, ConfigMap distinto) — 2026-07-29:
  [k8s/staging/ayudas-staging.yaml](k8s/staging/ayudas-staging.yaml). Mismos nombres de objeto
  (`postgres`, `backend`, `frontend`) en otro namespace; `JWT_EXPIRATION` de 1 h frente a 24 h y contraseña
  de BD distinta. Ingress por host (`staging.ayudas.local`) conviviendo con el catch-all de dev
- [x] Probes "de verdad": añadir `spring-boot-starter-actuator` y pasar a `httpGet /actuator/health` — 2026-07-29:
  `startupProbe` + `readinessProbe` (incluye la BD: `readiness.include=readinessState,db`) + `livenessProbe`
  (solo `livenessState`: si la BD cae, reiniciar el backend no arregla nada). Los dos endpoints son públicos
  en `SecurityConfig` porque los lanza el kubelet, que no tiene JWT, y van con `show-details: never`
- [x] Reescribir de memoria el Deployment+Service del backend y comparar — 2026-07-29: ejercicio recogido en la
  guía (Parte 6.3) con la lista de lo que más se olvida
- [x] Resiliencia con JWT stateless: borrar un Pod mientras se navega, sin perder sesión — 2026-07-29
- [x] `resources.requests/limits` y diferencia OOMKilled (memoria) vs throttling (CPU) — 2026-07-29:
  añadidos a los tres Deployments; la explicación queda en la guía y en el propio manifiesto
- [x] Preparar respuestas orales de 30s a las preguntas típicas (lista en la guía, Parte 6) — 2026-07-29
- [x] **Endurecer `k8s/20-postgres.yaml`** (hallazgo del Bloque 3, 2026-07-27): `strategy: Recreate` +
  `accessModes: ["ReadWriteOncePod"]` — 2026-07-29. **Aprendizaje extra:** `spec` de un PVC es **inmutable**
  una vez creado (`Forbidden: spec is immutable after creation`), así que cambiar el accessMode obligó a
  borrar y recrear el PVC — y con él, el disco. En dev no importa (Flyway reconstruye), en producción es
  justo la decisión que hay que tomar antes de crear el PVC, no después
- [x] Anotar `kubernetes.io/change-cause` en los Deployments para que `rollout history` sea legible — 2026-07-29

### Material didáctico (divulgación de la Fase K8s) — COMPLETADA (2026-07-28)

- [x] Reescribir [docs/kubernetes-aprendizaje.md](docs/docker-k8s/kubernetes-aprendizaje.md) en lenguaje llano: mantener
  el vocabulario técnico (Pod, Namespace, Service...) pero explicar cada concepto con una **analogía cotidiana**
  y prosa sencilla. No tocar el Apéndice B (manifiestos de referencia) — 2026-07-28: **25 analogías** en
  recuadros `> **Imagínalo así.**` (Partes 0–5 y 7); corregido además el ejercicio 5.4, que daba por hecho un
  resultado que el Bloque 3 demostró falso
- [x] Crear [docs/kubernetes-desde-0.md](docs/docker-k8s/kubernetes-desde-0.md): resumen de los Bloques 1–4 contado desde
  cero, sin dar por sabido nada, con las mismas analogías y con lo que realmente pasó en cada sesión (incluido
  el incidente de postgres del 27/07). Público: alguien que no ha tocado Kubernetes nunca — 2026-07-28:
  343 líneas, 13 analogías, cierra con las 6 preguntas de entrevista respondidas en 30 segundos

**Hecho cuando:** el usuario puede, de memoria, desplegar el stack en minikube, escalarlo, matar un Pod
y verlo recuperarse, y hacer un rolling update con rollback; y explica con soltura Pod/Deployment/Service/
Ingress/ConfigMap/Secret/Namespace. Entregable físico: carpeta `k8s/` versionada + ejercicios marcados
+ los dos documentos divulgativos.

---

## Higiene del repositorio (transversal) — COMPLETADA (2026-07-28)

- [x] **Cortar el bucle del snapshot del grafo** — 2026-07-28: `artifact.json` guardaba el hash de HEAD, así
  que cada commit disparaba un reindexado que volvía a ensuciar los dos ficheros: la cola de git nunca podía
  quedar vacía. Además pesaban 6,5 MB de los 10,58 MB del repo (8 copias de ~880 kB de binario sin diff).
  `git rm --cached` + `.gitignore` con el motivo documentado. Se conservan `graph.html` y `.gitattributes`;
  el grafo sigue en local y el watcher lo mantiene. Decisión razonada en `spec.md` §6
- [x] **Política "cola de git limpia"** en `CLAUDE.md` — 2026-07-28: al cerrar sesión, cada fichero pendiente
  se commitea, se ignora (documentando el motivo) o se borra. Ninguno se queda indefinidamente
- [x] **Hook `Stop` que la vigila** — 2026-07-28: `.claude/hooks/git-cola-limpia.sh` (+ `.claude/settings.json`).
  Avisa solo de los **rezagados** (sin tocar en 45 min) para no generar ruido con el trabajo en curso; los
  borrados sin commit se reportan siempre. Probado en crudo: JSON válido y filtro por antigüedad correcto
- [x] **Reorganizar `docs/`** — 2026-07-28: la carpeta acumulaba 9 documentos sueltos en la raíz más dos
  `.excalidraw` mezclados. Ahora: [docs/docker-k8s/](docs/docker-k8s/) (los 4 documentos de la Fase K8s),
  [docs/diagramas/](docs/diagramas/) (los `.excalidraw`) y un índice [docs/README.md](docs/README.md) con
  tabla por bloque y "por dónde empezar". Los documentos del sistema (`arquitectura`, `modelo-datos`,
  `tecnologias`, `fase0-planificacion`) se quedan en la raíz de `docs/`
- [x] **Fusionar el material de la reunión** — 2026-07-28: `docker-k8s-diagramas.md` (230 líneas) +
  `entrevista-docker-k8s-preguntas.md` (212) → [docs/docker-k8s/entrevista-docker-k8s.md](docs/docker-k8s/entrevista-docker-k8s.md)
  (453). Mismo día de origen (24/07), mismo propósito y uno ya enlazaba al otro como "modelo visual de apoyo".
  Parte 1 = 10 diagramas Mermaid, Parte 2 = 30 preguntas con respuesta modelo + chuleta. Contenido íntegro de
  ambos; solo se reescribió la cabecera. Enlaces relativos corregidos y referencias actualizadas en
  `README.md`, `spec.md`, `plan.md` e itinerario — sin enlaces rotos

---

## Fase IA — Spring AI: asistente RAG sobre la normativa — PENDIENTE (planificada 2026-10-13)

> **Objetivo:** que un usuario interno pueda preguntar dudas sobre las 3 ayudas y obtener una respuesta
> **fundamentada en la normativa (TRLGSS arts. 351–362 y 373) con las fuentes citadas**, en es/eu.
> **Doble propósito:** aprender Spring AI sobre un caso real y dejar una función demostrable para
> portfolio/CV ("Spring AI + RAG + PGvector + Anthropic Claude").
> **Decisiones del usuario (2026-10-13):** proveedor Anthropic Claude · vector store PGvector ·
> alcance v1 solo RAG sobre normativa (sin tools sobre expedientes) · secuencia: tras K8s, antes de Fase 1 — ambas ya cerradas el 2026-07-29;
> el usuario confirma el 2026-10-13 que la Fase IA va antes de la Fase 2.
> Registradas en [spec.md](spec.md) §3 (invariante 11) y §2.

**Punto abierto a cerrar en el Bloque 0:** Anthropic **no ofrece modelo de embeddings**, así que el RAG
necesita un `EmbeddingModel` aparte. Opciones: ONNX local multilingüe con `spring-ai-starter-model-transformers`
(sin clave extra, datos en local; recomendado por defecto), Ollama (`nomic-embed-text`/`bge-m3`) o un
proveedor externo. Validar calidad en euskera con unas preguntas de prueba antes de fijarlo.

### Bloque 0 — Fundamentos y preparación
- [ ] Hacer el curso de Anthropic Academy / leer la doc oficial de Spring AI (ChatClient, advisors, RAG) y anotar dudas
- [ ] Fijar versión de Spring AI compatible con Spring Boot 3.5.x (BOM `spring-ai-bom`) y modelo de embeddings
- [ ] Reunir la **fuente normativa** en texto (arts. 351–362 y 373 TRLGSS, es y eu si existe) en `docs/normativa/`
- [ ] Gestión de la API key de Anthropic: variable de entorno `ANTHROPIC_API_KEY` (compose + futuro Secret), nunca en el repo

### Bloque 1 — Chat básico con `ChatClient`
- [ ] Dependencia `spring-ai-starter-model-anthropic` + configuración en `application.yml` (modelo, temperatura, max-tokens)
- [ ] Servicio `asistente` (paquete propio, frontera limpia) con prompt de sistema: solo normativa, citar artículos, responder en el idioma pedido
- [ ] `POST /api/v1/asistente/consultas` protegido (rol `CONSULTOR` o superior), validación de entrada y límite de longitud
- [ ] Prueba con `ChatModel` simulado (sin llamar a la API real) y documentación en Swagger

### Bloque 2 — RAG con PGvector
- [ ] Cambiar la imagen de `postgres:15` a `pgvector/pgvector:pg15` en `docker-compose.yml`, `k8s/20-postgres.yaml` y `k8s/staging/ayudas-staging.yaml`
- [ ] Migración Flyway (siguiente número libre tras V6): `CREATE EXTENSION vector` + tabla de embeddings (esquema gestionado por Flyway, no por `initialize-schema`)
- [ ] Pipeline de ingesta: lector de `docs/normativa/` → `TokenTextSplitter` → `VectorStore.add` (idempotente, ejecutable por perfil/endpoint ADMIN)
- [ ] `QuestionAnswerAdvisor` con `VectorStore` + `topK`/umbral de similitud; respuesta con **fuentes** (artículo/fragmento)
- [ ] Comportamiento ante "no hay información en la normativa": respuesta honesta, sin inventar
- [ ] Pruebas: ingesta + recuperación contra PGvector (Testcontainers o equivalente) y casos de preguntas es/eu

### Bloque 3 — Integración, calidad y cierre
- [ ] Panel de chat en el frontend (i18n es/eu, sistema de diseño existente, iconos SVG propios, estado de carga y errores)
- [ ] Auditoría de cada consulta en `auditoria` (usuario, idioma, correlation-id; **sin** volcar datos personales en logs)
- [ ] Límites de coste/abuso: rate limiting básico por usuario y tope de tokens
- [ ] Evaluación sencilla: batería de ~15 preguntas con respuesta esperada y comprobación manual de citas
- [ ] `/code-review` + `/security-review` (endpoint nuevo, API key, entrada de usuario hacia un LLM) y verificación end-to-end
- [ ] Documentar en README (feature + stack) y `docs/arquitectura.md`; redactar texto breve para CV/LinkedIn con lo que hay de verdad

**Hecho cuando:** desde la UI, un usuario autenticado pregunta p. ej. "¿qué grado de discapacidad exige la
asignación económica?" y recibe una respuesta correcta con artículos citados, en el idioma elegido, con la
consulta auditada; y puedes explicar de memoria el flujo ingesta → embeddings → búsqueda → prompt → respuesta.

**Fuera de alcance v1:** function calling sobre expedientes, MCP server, memoria de conversación persistente.
Candidatos para una v2 una vez cerrada la Fase 4 (reglas de negocio), cuando haya más lógica que exponer.

---

## Fase 1 — Tramitación — COMPLETADA (2026-07-29)

> **Objetivo:** el personal instructor puede hacer avanzar los expedientes.
> Fuente: enunciado §7.2 (verificado literal, 2026-07-21). Modelo de datos ya previsto: tablas
> `tramite` y `tipo_tramite` en [docs/modelo-datos.md](docs/modelo-datos.md).

Modelo básico de tramitación (cada trámite cambia el estado del expediente y deja traza en `auditoria`):

```
REGISTRO/Entrada  → expediente registrado
ASIGNACION        → se asigna instructor
REVISION/Subsanación → comprobación de requisitos; si falta algo, se requiere subsanación
PROPUESTA         → propuesta de resolución (favorable | desfavorable)
RESOLUCION        → concesión o denegación (rol RESPONSABLE)
NOTIFICACION      → comunicación al interesado por su canal de aviso y en su idioma de notificación
```

- [x] Asignación de instructor a un expediente (rol `RESPONSABLE`) — cambia estado a `ASIGNADO` — 2026-07-29:
  `PUT /api/v1/expedientes/{id}/instructor`, transaccional (asigna + cambia estado + deja el trámite
  `ASIGNACION`); valida que el destinatario sea un instructor **activo** (409 si no). `GET /api/v1/instructores`
  alimenta el desplegable, con el mismo filtro de rol
- [x] Registro de trámites según el flujo, con `tipo_tramite`, resultado y observaciones — 2026-07-29:
  `POST /api/v1/expedientes/{id}/tramites`
- [x] Cada trámite guarda **quién** lo hizo, **cuándo** y el **resultado** — 2026-07-29: autor del JWT y fecha
  del reloj del servidor, nunca del cuerpo de la petición; `@Auditable(accion="TRAMITE")` deja además la traza
  en `auditoria`
- [x] Cambio de estado del expediente asociado a cada trámite — 2026-07-29: `REGISTRO`→REGISTRADO,
  `REVISION`/`PROPUESTA`→EN_REVISION, `RESOLUCION`→RESUELTO (+ sella `fecha_resolucion`), `NOTIFICACION`→sin cambio
- [x] Regla de negocio: **un instructor solo puede tramitar expedientes asignados a él** (validado en backend) —
  2026-07-29: doble capa — `@PreAuthorize` filtra por rol, el servicio filtra por dato. Probada en el clúster:
  `instructor1` (asignado) 201, `instructor2` (no asignado) 403 con el motivo concreto en es y en eu
- [x] Sección **Tramitación** visible en el detalle del expediente (histórico de trámites + instructor) —
  2026-07-29: 5.ª pestaña, línea de tiempo con quién/cuándo/resultado + formulario de registro y de asignación,
  ambos condicionados al rol
- [x] Endpoints REST documentados en Swagger — 2026-07-29: 4 endpoints con `@Operation`/`@ApiResponses`
  (incluidos 403/404/409 y qué significa cada uno). Tabla en [docs/arquitectura.md](docs/arquitectura.md)
- [x] Pruebas: la regla "instructor no asignado no puede tramitar" (empezar por la prueba, luego el código) —
  2026-07-29: **backend 107** (era 52) y **frontend 26** (era 16)
- [x] **Extra — bug de i18n cazado en el clúster (2026-07-29).** Las claves nuevas se añadieron a
  `messages_es`/`messages_eu` pero no al bundle raíz `messages.properties`, que es el fallback cuando la
  petición no manda `Accept-Language`. Síntoma: el usuario recibía `tramite.resolucion.rol` en vez del mensaje.
  Ninguna prueba lo veía, porque las excepciones llevan la clave, no el texto. Corregido + `MessagesParidadTest`,
  que falla si los tres bundles dejan de tener exactamente las mismas claves

- [ ] Notificación al interesado en su `datos_contacto.idioma_notificacion` — **NO hecho, movido a Fase 3.**
  En Fase 1 el trámite `NOTIFICACION` se **registra** y deja traza, pero no se genera ni se envía nada. La
  emisión por canal (`EMAIL`/`SMS`/`POSTAL`) y en el idioma del interesado es literalmente el objetivo de la
  Fase 3; hacerla aquí habría sido duplicar ese trabajo. Es la única casilla de la Fase 1 que queda abierta

---

## Fase 2 — Alta y registro de solicitudes — PENDIENTE

> **Objetivo:** crear nuevos expedientes y solicitudes desde la aplicación. Fuente: enunciado §7 (Fase 2).

- [ ] Formulario de alta de expediente con validaciones (cliente + servidor)
- [ ] Gestión de interesados: `SOLICITANTE`, `REPRESENTANTE` (opcional), `CAUSANTE`(s)
- [ ] Datos de contacto y aviso (canal, idioma de notificación)
- [ ] Formulario específico por tipo de ayuda: `ASIG_DISCAP` / `PAGO_NAC` / `PARTO_MULT`
- [ ] Persistencia con SQL parametrizado en los repositorios correspondientes
- [ ] Autorización: qué roles pueden dar de alta (a definir con el usuario)

---

## Fase 3 — Resolución y notificaciones — PENDIENTE

> **Objetivo:** generación de documentos y comunicación al interesado. Fuente: enunciado §7 (Fase 3).

- [ ] Generación de documentos de resolución (favorable / desfavorable)
- [ ] Notificaciones por el canal configurado: `EMAIL` / `SMS` / `POSTAL`
- [ ] Idioma del documento según `datos_contacto.idioma_notificacion` (independiente de la interfaz)
- [ ] Traza en `auditoria` de la emisión de resolución y de la notificación

---

## Fase 4 — Reglas de negocio y cálculo de cuantías — PENDIENTE

> **Objetivo:** validación automática de requisitos y cálculo orientativo de prestaciones.
> Fuente: enunciado §7 (Fase 4) + Anexo A (resumen funcional de las tres ayudas).

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

## Evolución arquitectónica (transversal, a partir de Fase 1)

> El enunciado §4 prevé pasar de monolito modular a microservicios. Se materializa progresivamente y se
> apoya en la Fase K8s (cada microservicio = Deployment + Service en el clúster). No es una fase aparte:
> se acomete al extraer cada bounded context.

- [ ] Extraer `ms-tramitacion` como primer microservicio (coincide con Fase 1)
- [ ] Introducir `gateway` (enrutado + propagación de token) delante de los servicios
- [ ] Extraer `ms-autenticacion` y `ms-expedientes` cuando aporte valor
- [ ] Desplegar cada servicio con su Deployment/Service en `k8s/` (reutilizando lo aprendido)

---

## Entregables por fase

Cada fase (funcional) cierra con:
1. Código en rama Git con PR revisado (`/code-review` sobre el diff).
2. Migraciones Flyway aplicadas y versionadas.
3. Swagger UI operativo y probado.
4. Suite de pruebas en verde.
5. Demo funcional de lo entregado.

La Fase K8s cierra con: carpeta `k8s/` versionada, ejercicios de la guía marcados y demo en vivo
(desplegar, escalar, matar un Pod, rolling update).

---

## Histórico de fases completadas

<details>
<summary><b>Fase 0 — MVP (S1–S6) — COMPLETADA (Hito M6, 2026-07-17)</b></summary>

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
