# Itinerario de fases restantes — Ayudas GV

> Plan de calendario para cerrar el proyecto desde el estado actual (Fase 0 completa,
> Fase K8s Bloque 0 hecho). Fuente de alcance: [../../plan.md](../../plan.md). Este documento
> **estima y calendariza**; el detalle vivo del avance se marca en `plan.md` y en los reportes diarios.
>
> **Ritmo asumido:** media jornada, **~4h/día laborable** (una "sesión" = ~4h). Decisión del usuario (2026-07-23).
> **Anclas fijadas por el usuario:** 22/07 Bloque 0 · 23/07 Bloques 1+2 · 24/07 Bloques 3+4 · 27/07 arranca Fase 1.
> **Real (al 28/07):** 23/07 Bloque 1 completo + Bloque 2 iniciado · 24/07 Bloque 2 completo + material de repaso
> para reunión (no previsto) · 27/07 Bloque 3 completo (modo aprendizaje paso a paso) · 28/07 **día sin sesión de
> proyecto** (entrevista técnica + preparación de la reunión); solo material divulgativo de K8s e higiene del repo
> por la mañana. El **Bloque 4** y el arranque de la Fase 1 se reslotean al **miércoles 29/07**; el resto se
> desplaza **1 día laborable más** y el fin proyectado pasa de 11/08 a **12/08**.
>
> **La fecha de cierre no es un compromiso rígido** (decisión del usuario, 2026-07-28): si hay que ampliarla,
> se amplía. Este documento sigue calendarizando para tener visibilidad del avance, pero un corrimiento no es
> un problema a resolver ni motivo para recortar alcance o bajar la profundidad del aprendizaje.
> Sin emojis (convención del proyecto): marcadores `COMPLETADA · EN CURSO · PENDIENTE`.

---

## Resumen ejecutivo

- **Punto de partida:** jueves 23/07/2026 (Fase 0 MVP cerrada; Fase K8s Bloque 0 cerrado el 22/07).
- **Trabajo restante (desde el 29/07):** 11 sesiones de ~4h ≈ **~44 horas**.
- **Fin proyectado:** **miércoles 12/08/2026** (demo final y cierre) — corrido 1 día tras el reslot del 28/07.
- **Duración natural:** ~3 semanas laborables (23/07 → 12/08), fines de semana excluidos.

| Fase restante | Sesiones (~4h) | Ventana de fechas |
|---------------|:--------------:|-------------------|
| Fase K8s — Bloques 1–4 | 3 | jue 23/07 – mié 29/07 (Bloque 4 el 29) |
| Fase 1 — Tramitación | 2 | mié 29/07 – jue 30/07 (arranca la tarde del 29) |
| Fase 2 — Alta y registro | 2 | vie 31/07 – lun 03/08 |
| Fase 3 — Resolución y notificaciones | 2 | mar 04/08 – mié 05/08 |
| Fase 4 — Reglas de negocio y cuantías | 2 | jue 06/08 – vie 07/08 |
| Evolución arquitectónica (microservicios en K8s) | 3 | lun 10/08 – mié 12/08 |
| **Total** | **14** | **jue 23/07 – mié 12/08** |

> El **28/07 no consume sesión**: fue un día sin desarrollo (entrevista técnica y preparación de la reunión).
> Por eso el total de sesiones no cambia aunque el calendario se corra un día.

---

## Calendario día a día

Cada día = una sesión de ~4h. El reporte diario se escribe al terminar la sesión (trabajo real, no agenda).
La demo/cierre semanal cae el viernes (o último día laborable de la ventana).

### Semana 7 (20–24 jul) — cierre de la Fase K8s

| Día | Sesión | Entregable del día |
|-----|--------|--------------------|
| lun 20/07 | *(hecho)* Migración a PC nuevo + herramental (Docker Desktop, Java/Maven/Node) | — |
| mar 21/07 | *(hecho)* Verificación del stack + fijar punto de entrada K8s | — |
| mié 22/07 | *(hecho)* **Bloque 0** — repaso Docker consolidado ([docker-guia.md](../docker-guia.md)) | COMPLETADA |
| **jue 23/07** | **Bloque 1** (completo) + `.dockerignore` + **migración graphify→codebase-memory-mcp** (no prevista) + Bloque 2 iniciado (namespace). El Bloque 2 se desbordó a mañana | COMPLETADA (parcial) |
| **vie 24/07** | **Bloque 2** (completo) — desplegar en `k8s/` (namespace→config/secret→postgres→backend→frontend→ingress, 3 Pods Running + login e2e + DNS interno + fallo provocado diagnosticado) + **material de repaso Docker/K8s para reunión** (no previsto). Los Bloques 3+4 se reslotean al lunes | COMPLETADA (parcial) |

### Semana 8 (27–31 jul) — cierre de la Fase K8s, Tramitación y arranque del Alta

| Día | Sesión | Entregable del día |
|-----|--------|--------------------|
| **lun 27/07** | **Bloque 3** (completo) — escalar a 3 réplicas con balanceo verificado (3/1/2) · self-healing con cero caídas · rolling update `:1.0`→`:1.1` + `rollout undo` · limitación de postgres **demostrada rompiéndola en vivo** (dos postmasters sobre el mismo PVC; BD recuperada íntegra). Trabajado paso a paso en modo aprendizaje; el Bloque 4 se resloteó a mañana | COMPLETADA (parcial) |
| **mar 28/07** | **Día sin sesión de proyecto** — entrevista técnica y preparación de la reunión. Lo único desarrollado (mañana, no previsto): material divulgativo de la Fase K8s (`kubernetes-aprendizaje.md` reescrita con 25 analogías + `kubernetes-desde-0.md` + 2 diagramas del entorno) e higiene del repositorio (snapshot del grafo fuera del control de versiones + política de cola limpia + hook `Stop`). El Bloque 4 y el arranque de la Fase 1 se reslotean a mañana | SIN SESIÓN |
| mié 29/07 | **Fase K8s — Bloque 4** (cierre): namespace `ayudas-staging` · probes con actuator · resiliencia JWT · `resources.requests/limits` · endurecer `20-postgres.yaml` (`Recreate` + `ReadWriteOncePod`) · `change-cause` · respuestas orales → **cierre Fase K8s**. Luego **Fase 1 (1/2, arranque)** — asignación de instructor (rol RESPONSABLE) · registro de trámites por flujo · regla "instructor solo tramita lo asignado" (TDD) | PENDIENTE |
| jue 30/07 | **Fase 1 (2/2)** — sección Tramitación en el detalle (histórico + instructor) · notificación en idioma del interesado · endpoints REST en Swagger · pruebas en verde → **cierre Fase 1** | PENDIENTE |
| vie 31/07 | **Fase 2 (1/2)** — formulario de alta de expediente (validación cliente + servidor) · gestión de interesados (SOLICITANTE / REPRESENTANTE / CAUSANTE) · datos de contacto y aviso + **demo semanal** | PENDIENTE |

### Semana 9 (03–07 ago) — Alta, Resolución y Reglas de negocio

| Día | Sesión | Entregable del día |
|-----|--------|--------------------|
| lun 03/08 | **Fase 2 (2/2)** — formulario específico por tipo (`ASIG_DISCAP` / `PAGO_NAC` / `PARTO_MULT`) · persistencia con SQL parametrizado · autorización de alta por rol → **cierre Fase 2** | PENDIENTE |
| mar 04/08 | **Fase 3 (1/2)** — generación de documentos de resolución (favorable/desfavorable) · idioma del documento según `idioma_notificacion` · traza en `auditoria` | PENDIENTE |
| mié 05/08 | **Fase 3 (2/2)** — notificaciones por canal (EMAIL / SMS / POSTAL) · traza de emisión → **cierre Fase 3** | PENDIENTE |
| jue 06/08 | **Fase 4 (1/2)** — validación de requisitos por tipo de ayuda (grados, supuestos habilitantes, límites de ingresos) antes de proponer/resolver | PENDIENTE |
| vie 07/08 | **Fase 4 (2/2)** — cálculo orientativo de cuantías (SMI, referencia 1.000 €) · incompatibilidades (progenitores, prestaciones análogas) · pruebas → **cierre Fase 4** + **demo semanal** | PENDIENTE |

### Semana 10 (10–12 ago) — microservicios y cierre

| Día | Sesión | Entregable del día |
|-----|--------|--------------------|
| lun 10/08 | **Microservicios (1/3)** — extraer `ms-tramitacion` como primer bounded context (coincide con Fase 1) | PENDIENTE |
| mar 11/08 | **Microservicios (2/3)** — `gateway` (enrutado + propagación de token) delante de los servicios | PENDIENTE |
| mié 12/08 | **Microservicios (3/3)** — desplegar cada servicio como Deployment+Service en `k8s/` (reutilizando la Fase K8s) · pruebas e2e · **demo final y cierre del proyecto** | PENDIENTE |

---

## Cómo se estimó cada fase

| Fase | Base de la estimación |
|------|-----------------------|
| **K8s 1–4** | Guía didáctica ya escrita ([kubernetes-aprendizaje.md](../kubernetes-aprendizaje.md)); manifiestos de referencia listos en el Apéndice B → sobre todo ejecución + interiorización. Estimadas 2 sesiones (anclas del usuario); **reales 3**: el modo aprendizaje paso a paso (ejecutar, observar y razonar cada ejercicio) rinde más conocimiento por sesión pero cubre menos puntos. |
| **Fase 1** | Modelo de datos ya previsto (`tramite`, `tipo_tramite`); auth, auditoría e i18n ya existen de Fase 0. El grueso es lógica de tramitación + una regla de autorización en backend + una sección de frontend. 2 sesiones. |
| **Fase 2** | Formularios y validación doble + 3 variantes por tipo de ayuda. El modelo (`persona`, `expediente`, `solicitud_ayuda`) ya está de Fase 0 → sobre todo escritura (INSERT parametrizado) y UI. 2 sesiones. |
| **Fase 3** | Generación de documentos + notificaciones por canal. Riesgo: integración real de EMAIL/SMS puede quedar simulada (stub) en local. 2 sesiones. |
| **Fase 4** | Reglas bien especificadas en el enunciado (Anexo A) → traducibles a código y test con claridad. 2 sesiones. |
| **Microservicios** | Extracción del primer bounded context + gateway + despliegue en el clúster ya montado en la Fase K8s. Es la parte con más incógnitas de infraestructura. 3 sesiones. |

**Riesgos y holguras.** Las fases 1 y 4 son las que con más probabilidad se desbordan a una 3.ª sesión
(regla de negocio y cálculo de cuantías, respectivamente). Si ocurre, el fin proyectado se corre 1–2 días
laborables (→ 13–14/08 desde el 12/08 actual). La integración real de notificaciones (Fase 3) puede quedar como
stub local y completarse en la fase de microservicios si aporta valor.

> **Aviso de holgura (2026-07-27).** Los desplazamientos previos (Bloque 2 al 24/07, Bloques 3+4 al 27/07) se
> absorbieron sin mover el fin. El de hoy **ya no**: consumida la holgura, el fin pasa de 10/08 a 11/08. A
> partir de aquí, cada desbordamiento se traslada 1:1 a la fecha de cierre.
>
> **Segundo corrimiento (2026-07-28).** Confirmado lo anterior: el 28/07 no hubo sesión de proyecto (entrevista
> técnica + preparación de la reunión) y el fin pasa de 11/08 a **12/08**. Este no es un desbordamiento de
> alcance sino un día de calendario perdido; el trabajo restante sigue siendo el mismo (11 sesiones).
>
> **Criterio del usuario (2026-07-28):** si hace falta ampliar la fecha de cierre, se amplía. Los corrimientos
> se anotan para saber dónde estamos, no para forzar el ritmo: prevalece completar bien cada fase.

---

## Notas de seguimiento

- Cada fase funcional cierra con lo de `plan.md` §Entregables: código en rama con `/code-review` sobre el
  diff, migraciones Flyway versionadas, Swagger operativo, suite en verde y demo.
- La Fase K8s cierra con la carpeta `k8s/` versionada, ejercicios de la guía marcados y demo en vivo.
- **Reportes:** diario en `docs/reportes/semanaN/` al terminar cada sesión; semanal (`.html`) el viernes.
- **Backfill hecho (2026-07-23):** reportes diarios de 20/07 (migración de PC), 21/07 (adopción de SDD + guía
  K8s) y 22/07 (Bloque 0) reconstruidos en `docs/reportes/semana7/`. Los del 21 y 22 anclados a git; el del
  20 es reconstruido (ese día no dejó commits) y queda a confirmar por el usuario.
