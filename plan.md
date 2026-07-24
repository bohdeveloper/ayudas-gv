# plan.md — Sistema de Gestión de Ayudas GV

> Plan de trabajo vivo (Spec-Driven Development). Contexto y metodología en [spec.md](spec.md).
> Reglas: nada se implementa sin su punto aquí · al terminar se marca `[x]` con fecha ·
> las tareas grandes se desglosan en fases antes de empezar.
> El detalle punto por punto de lo completado vive en el historial de git.
> Marcadores de estado en texto (sin emojis): **COMPLETADA · EN CURSO · PENDIENTE**.

---

## Estado actual (2026-07-21)

| Fase / hito | Estado |
|-------------|--------|
| **Fase 0 — MVP** (S1–S6) | COMPLETADA (Hito M6, 2026-07-17) |
| **Fase K8s — Docker + Kubernetes (learning track)** | EN CURSO — **prioridad actual** |
| Fase 1 — Tramitación | PENDIENTE (tras K8s) |
| Fase 2 — Alta y registro de solicitudes | PENDIENTE |
| Fase 3 — Resolución y notificaciones | PENDIENTE |
| Fase 4 — Reglas de negocio y cálculo de cuantías | PENDIENTE |

**Próxima sesión (viernes 24/07) — Bloque 2 de la Fase K8s** (desplegar en `k8s/`), y si da tiempo Bloques 3 y 4.
Bloque 1 completado el 23/07 (clúster minikube + imágenes `:1.0` dentro del clúster). Ya existe `k8s/00-namespace.yaml`
(pendiente de aplicar). Guía: [docs/kubernetes-aprendizaje.md](docs/kubernetes-aprendizaje.md) → Parte 4.
Calendario completo de lo que queda (fin proyectado 10/08/2026): [docs/reportes/itinerario.md](docs/reportes/itinerario.md).

- **Punto de entrada exacto:** instalar `minikube` (`winget install Kubernetes.minikube`) y
  `minikube start --driver=docker`; luego `kubectl get nodes`.
- **Herramental (2026-07-22, PC nuevo `c:\aplic\ayudas_gv`):** Docker Desktop 29.6.2 + Compose v5.3.1
  instalados y verificados (stack completo levantado y login OK). Java 21 + Maven 3.9 + Node presentes.
  **Faltan `kubectl` y `minikube`** — se instalan al empezar el Bloque 1.
- Estado del repo: clonado desde GitHub (`origin` → `github.com/bohdeveloper/ayudas-gv`), rama `main`
  al día (GitHub ya contiene todo hasta commit `86cdfc9`; la nota previa de "push pendiente" queda resuelta).

> **Decisión de secuencia (usuario, 2026-07-21):** primero dominar Docker + Kubernetes sobre este
> proyecto; la Fase 1 (Tramitación) se aborda **después**, cuando el clúster esté montado y entendido.

---

## Fase K8s — Docker + Kubernetes (learning track) — EN CURSO

> **Objetivo:** llevar Ayudas GV de `docker compose` a un clúster de Kubernetes local (minikube) y
> dominar las operaciones de orquestación (escalar, autocurar, actualizar sin caída), como preparación
> para un nuevo proyecto profesional con este stack.
> **Método:** guía didáctica con teoría mínima + práctica + ejercicios en
> [docs/kubernetes-aprendizaje.md](docs/kubernetes-aprendizaje.md). Este apartado es el checklist; el
> "cómo" y el "por qué" están en la guía.

### Bloque 0 — Repaso de Docker (consolidación) — COMPLETADA (2026-07-22)
- [x] Levantar el stack con `docker compose up --build` y verificar los 3 contenedores (ej. 1.1–1.4 de la guía) — 2026-07-22: 3 contenedores Up (postgres healthy), Flyway V1–V5 aplicadas, login `admin` OK vía Nginx
- [x] Explicar por escrito el multi-stage build de `backend/Dockerfile` y el de `frontend/Dockerfile` — 2026-07-22: [docs/docker-guia.md](docs/docker-guia.md) §3.1–3.2
- [x] Interiorizar la red por nombre de servicio (`postgres`, `backend`) — base del DNS de K8s — 2026-07-22: [docs/docker-guia.md](docs/docker-guia.md) §4 + demostrado en vivo (Flyway→`postgres:5432`, Nginx→`backend:8080`)

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

### Bloque 3 — Operar (la esencia de orquestar)
- [ ] Escalar el backend a 3 réplicas y comprobar los 3 endpoints del Service
- [ ] Self-healing: matar un Pod y ver cómo el Deployment lo recrea (sin afectar al frontend)
- [ ] Rolling update `:1.0`→`:1.1` con `kubectl set image` y `rollout status`; luego `rollout undo`
- [ ] Razonar por qué postgres (PVC ReadWriteOnce) no se replica como el backend → StatefulSet

### Bloque 4 — Ejercicios integradores (blindaje entrevista)
- [ ] Segundo entorno con namespace `ayudas-staging` (mismo stack, ConfigMap distinto)
- [ ] Probes "de verdad": añadir `spring-boot-starter-actuator` y pasar a `httpGet /actuator/health`
- [ ] Reescribir de memoria el Deployment+Service del backend y comparar
- [ ] Resiliencia con JWT stateless: borrar un Pod mientras se navega, sin perder sesión
- [ ] `resources.requests/limits` y diferencia OOMKilled (memoria) vs throttling (CPU)
- [ ] Preparar respuestas orales de 30s a las preguntas típicas (lista en la guía, Parte 6)

**Hecho cuando:** el usuario puede, de memoria, desplegar el stack en minikube, escalarlo, matar un Pod
y verlo recuperarse, y hacer un rolling update con rollback; y explica con soltura Pod/Deployment/Service/
Ingress/ConfigMap/Secret/Namespace. Entregable físico: carpeta `k8s/` versionada + ejercicios marcados.

---

## Fase 1 — Tramitación — PENDIENTE (tras K8s)

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

- [ ] Asignación de instructor a un expediente (rol `RESPONSABLE`) — cambia estado a `ASIGNADO`
- [ ] Registro de trámites según el flujo, con `tipo_tramite`, resultado y observaciones
- [ ] Cada trámite guarda **quién** lo hizo, **cuándo** y el **resultado** (enlaza con auditoría, §8)
- [ ] Cambio de estado del expediente asociado a cada trámite
- [ ] Regla de negocio: **un instructor solo puede tramitar expedientes asignados a él** (validado en backend)
- [ ] Notificación al interesado en su `datos_contacto.idioma_notificacion`
- [ ] Sección **Tramitación** visible en el detalle del expediente (histórico de trámites + instructor)
- [ ] Endpoints REST (`/api/v1/expedientes/{id}/tramites`, asignación) documentados en Swagger
- [ ] Pruebas: la regla "instructor no asignado no puede tramitar" (empezar por la prueba, luego el código)

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
