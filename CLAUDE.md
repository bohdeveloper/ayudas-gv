# CLAUDE.md — Reglas de trabajo (Ayudas GV)

Este archivo se carga en cada sesión. Contiene **solo reglas de trabajo**; el conocimiento del proyecto
(qué es, arquitectura, decisiones) vive en [spec.md](spec.md) y el trabajo pendiente en [plan.md](plan.md).

## Spec-Driven Development (OBLIGATORIO)

1. **Al empezar:** leer `spec.md` (§3 invariantes y §6 metodología) y `plan.md`. Confirmar que la tarea
   está en el plan; si no está, añadirla primero.
2. **Nada se implementa fuera de `plan.md`.** Tareas grandes → desglosar en fases antes de empezar.
3. **Contrastar con los invariantes de `spec.md` §3** (SQL explícito sin JPA, bilingüismo es/eu,
   autorización en backend, API `/api/v1`, etc.) y avisar si algo choca.
4. **Al terminar una feature:** typecheck/build de lo tocado → `/code-review` sobre el diff (obligatorio)
   → `/security-review` si toca auth/privacidad/auditoría → verificar end-to-end → registrar:
   marcar `plan.md` con fecha, decisiones nuevas a `spec.md`, cambios de alcance/stack a `README.md`.
5. **Commit solo cuando el usuario lo pida**, en el estilo del `git log` (`tipo: descripción`).

## Herramientas del proyecto

- **graphify** — grafo de código en `graphify-out/`. Para preguntas sobre el código/arquitectura, usar
  primero `graphify query "<pregunta>"` (y `graphify path`/`graphify explain`) antes de grep/lectura amplia.
  Tras modificar código, `graphify update .` mantiene el grafo (el hook PostToolUse ya lo dispara).
  Trigger `/graphify` → invocar la Skill `graphify`.
- Skills y agentes disponibles: resumen y cuándo usarlos en `spec.md` §6 (tabla de skills).

## Convenciones de entorno

- **Documentación sin emojis como iconos.** Marcadores de estado en texto (`COMPLETADA`/`EN CURSO`/
  `PENDIENTE`) y checkboxes Markdown `[x]`/`[ ]`. Iconos = SVG propios con `currentColor` (también en
  frontend). Ver `spec.md` §4.
- **Puertos:** frontend Docker `3000`, dev `5173`; backend `8080`; PostgreSQL externo `5433` (interno `5432`).
- **Reportes de trabajo:** por sesión en `docs/reportes/` (estructura semanal). Demo al cerrar cada fase.
