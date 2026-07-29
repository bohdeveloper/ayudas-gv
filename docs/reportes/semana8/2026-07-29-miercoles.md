# Reporte diario — Miércoles 29/07/2026

> Plan del día (itinerario): **Bloque 4** (cierre de la Fase K8s) y **Fase 1 (Tramitación)**, del tirón.
> Resultado real: **ambos completados**. Se cierra la Fase K8s entera y la Fase 1 funcional, verificada
> end-to-end dentro del clúster. Se recupera así el retraso que dejó el día atípico del 28/07.

## Completado

### Bloque 4 — cierre de la Fase K8s

Los seis ejercicios integradores de la Parte 6 de la guía, ejecutados de verdad en minikube, más los dos
arreglos que el Bloque 3 (27/07) dejó anotados.

- **6.1 — Segundo entorno.** [`k8s/staging/ayudas-staging.yaml`](../../../k8s/staging/ayudas-staging.yaml):
  el mismo stack en el namespace `ayudas-staging`, con **los mismos nombres de objeto**. Aislamiento
  demostrado en vivo: `postgres` resuelve a `10.111.20.147` en dev y a `10.109.85.152` en staging, y tras las
  pruebas del día dev tenía 19 trámites frente a los 11 de fábrica de staging. `JWT_EXPIRATION` de 24 h en dev
  y 1 h en staging, sin tocar código
- **6.2 — Probes de verdad.** Añadido `spring-boot-starter-actuator`. Las probes TCP (que solo comprobaban
  "el puerto está abierto" — y Tomcat lo abre antes de estar listo) pasan a `httpGet`, y ahora son **tres**:
  `startupProbe` (margen de arranque, hasta 150 s), `readinessProbe` (**incluye la BD**) y `livenessProbe`
  (**solo el proceso**). Esa asimetría es la decisión de fondo: meter la BD en la liveness convertiría una
  caída de PostgreSQL en un bucle de reinicios de todos los backends
- **6.3 — De Compose a K8s de memoria.** Ejercicio recogido en la guía con la lista de lo que más se olvida
  (`selector`↔`labels`, `imagePullPolicy`, `port` vs `targetPort`)
- **6.4 — Resiliencia con JWT stateless.** Backend a 3 réplicas, sesión iniciada **una sola vez**, Pod borrado
  mientras se navegaba: **40 peticiones, 40 correctas, 0 fallos**, sin volver a pedir credenciales. Y el Pod
  nuevo no recibió ni una petición hasta pasar su readiness. Es `SessionCreationPolicy.STATELESS` de
  `SecurityConfig` pagando dividendos en infraestructura
- **6.5 — Límites de recursos.** `requests`/`limits` en los tres Deployments, con la diferencia clave
  documentada: pasarse de **memoria** → OOMKilled (el kernel mata, el Pod reinicia); pasarse de **CPU** →
  throttling (va lento, nadie muere)
- **6.6 — Respuestas orales de 30 s** a las seis preguntas típicas, escritas en la guía
- **Los dos arreglos del 27/07:** `strategy: Recreate` + `accessModes: ReadWriteOncePod` en postgres, y
  `kubernetes.io/change-cause` anotado en los Deployments (el `rollout history` ya es legible)

**Piedra en el camino, y la más útil del día:** el `spec` de un PVC es **inmutable**.

```
The PersistentVolumeClaim "postgres-pvc" is invalid: spec: Forbidden:
spec is immutable after creation except resources.requests and volumeAttributesClassName
```

Cambiar `ReadWriteOnce` → `ReadWriteOncePod` obligó a **borrar y recrear el PVC**, y con él el disco. En dev
da igual (Flyway reconstruye), pero deja la lección: eso se decide **antes** de crear el PVC, no después.

### Fase 1 — Tramitación

Módulo `tramitacion` completo, siguiendo los patrones de la Fase 0 (SQL explícito, DTO+Mapper, i18n,
auditoría AOP).

- **Backend:** `TramiteRepository` (SQL parametrizado), `TramitacionService` (todas las reglas),
  `TramiteController` + `InstructorController`. Cuatro endpoints documentados en Swagger:
  `GET`/`POST /v1/expedientes/{id}/tramites`, `PUT /v1/expedientes/{id}/instructor`, `GET /v1/instructores`
- **Regla clave (spec.md §3, invariante 4)** — *un instructor solo tramita lo que tiene asignado* —
  implementada en **doble capa**: `@PreAuthorize` filtra por **rol** (barato, corta antes de tocar la BD) y el
  servicio filtra por **dato** (el rol no sabe de quién es el expediente)
- **Códigos de estado con criterio:** 403 cuando el rol autoriza pero no sobre *ese* expediente
  (`OperacionNoPermitidaException`), 409 cuando la petición es correcta pero el estado del recurso no la
  admite (`ReglaNegocioException`), 404 para recursos inexistentes
- **Migración V6** con el histórico de trámites de ejemplo (3 expedientes, uno con el flujo completo) y un
  **segundo instructor** — sin él no se puede demostrar la regla ni tiene sentido el desplegable de asignación
- **Frontend:** 5.ª pestaña del detalle con línea de tiempo (quién / cuándo / resultado), formulario de
  registro y de asignación, ambos condicionados al rol. Traducciones es/eu completas
- **`CatalogoDto` y `ColumnaCatalogo` movidos a `commons/dto`** — así `tramitacion` no depende del paquete de
  DTOs de `expedientes` y la frontera queda limpia de cara a extraer microservicios

**Pruebas: backend 52 → 107, frontend 16 → 26.** La regla del instructor se escribió como prueba antes que
como código, tal y como pedía el plan.

### El bug que solo aparece fuera de las pruebas

Probando la Fase 1 en el clúster, un 403 legítimo devolvió `"tramite.resolucion.rol"` — la clave técnica en
vez del mensaje. Causa: las claves nuevas se añadieron a `messages_es` y `messages_eu` pero **no al bundle
raíz `messages.properties`**, que es el fallback cuando la petición no manda `Accept-Language`.

Ninguna prueba lo veía, porque las excepciones llevan la clave, no el texto — y las pruebas de controlador sí
mandaban cabecera. Corregido, y añadido **`MessagesParidadTest`**, que falla si los tres bundles dejan de
tener exactamente las mismas claves. Es el tipo de fallo que justifica probar en el entorno real y no solo
en verde local.

### Verificación end-to-end en el clúster

Con `instructor1`, `instructor2`, `admin` y `consultor1` reales sobre el despliegue de minikube:

| Caso | Resultado |
|------|-----------|
| `instructor1` (asignado) registra REVISION | **201** + estado del expediente → EN_REVISION |
| `instructor2` (no asignado) intenta lo mismo | **403** "Solo puede tramitar los expedientes que tiene asignados" |
| El mismo rechazo con `Accept-Language: eu` | **403** "Esleitutako espedienteak soilik izapide ditzakezu" |
| `instructor1` intenta RESOLVER | **403** "Solo el personal responsable puede resolver un expediente" |
| `consultor1` intenta tramitar | **403** (corta `@PreAuthorize`, sin llegar al servicio) |
| PROPUESTA sin resultado | **409** con el motivo concreto |
| RESPONSABLE asigna instructor | **200** + estado ASIGNADO + trámite ASIGNACION en el histórico |
| Asignar a quien no es instructor | **409** "El usuario 3 no es un instructor activo" |
| Flujo completo hasta RESUELTO | 201 en cadena; después REVISION → **409**, NOTIFICACION → **201** |

## Fuera de alcance (consciente)

La casilla **"notificación al interesado en su idioma"** de la Fase 1 queda abierta a propósito: el trámite
`NOTIFICACION` se **registra** y deja traza, pero la *emisión* por canal (EMAIL/SMS/POSTAL) y en el idioma del
interesado es literalmente el objetivo de la **Fase 3**. Hacerla aquí habría duplicado ese trabajo. Es la
única casilla de la Fase 1 sin marcar, y está anotada como tal en `plan.md`.

## Estado al cerrar

- **Fase K8s: COMPLETADA** (Bloques 0–4). **Fase 1: COMPLETADA** salvo la casilla anterior
- Clúster funcionando: `ayudas-dev` (3 réplicas de backend) + `ayudas-staging`
- Backend 107 pruebas en verde, frontend 26, `npm run build` sin errores
- Registrado en `plan.md`, `spec.md`, `README.md`, `docs/arquitectura.md` y las dos guías de K8s

## Siguiente

**Fase 2 — alta y registro de solicitudes.** Antes de empezar hay que decidir con el usuario **qué roles
pueden dar de alta**: es la única casilla de la Fase 2 sin respuesta.
