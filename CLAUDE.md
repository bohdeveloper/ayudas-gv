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

- **codebase-memory-mcp** — grafo de código consultable vía sus tools MCP (config en `.mcp.json`, índice
  **local** en `.codebase-memory/`). Para preguntas sobre el código/arquitectura, usar primero sus tools
  (`search_graph`, `trace_path`, `query_graph`, `get_architecture`, `search_code`, `get_code_snippet`) antes
  de grep/lectura amplia. El índice se mantiene solo (watcher incremental tras editar); reindexar solo tras un
  `git pull` grande. Grafo visual navegable en `.codebase-memory/graph.html` — **regenerar al cerrar cada fase**
  (o tras cambios grandes). Trigger `/grafo-designer` → invocar la Skill `grafo-designer`.
  **`graph.db.zst` y `artifact.json` NO se versionan** (2026-07-28): `artifact.json` guarda el hash de HEAD, de
  modo que cada commit disparaba un reindexado que volvía a ensuciarlos — la cola de git nunca se vaciaba. Sí se
  versionan `graph.html` y `.gitattributes`.

## Cola de git limpia (OBLIGATORIO al cerrar sesión)

Antes de terminar una sesión, `git status` debe quedar **limpio o consciente**. Ningún fichero se queda
indefinidamente en la cola. Para cada cambio pendiente, decidir y **actuar**, no dejarlo pasar:

1. **¿Aporta al proyecto o a su memoria?** → commit (estilo `tipo: descripción`) y push si el usuario lo pidió.
2. **¿Es ruido regenerable o se re-ensucia solo?** → `.gitignore` (+ `git rm --cached` si estaba trackeado) y
   documentar el motivo en el propio `.gitignore`.
3. **¿Es un temporal de trabajo?** → borrarlo (los temporales van al scratchpad, no al repo).
4. **¿Es del usuario y no está claro?** → leerlo antes de decidir y decírselo; nunca commitear a ciegas.

Si al empezar una sesión ya hay ficheros en la cola, aplicar lo mismo antes de ponerse con la tarea.
- Skills y agentes disponibles: resumen y cuándo usarlos en `spec.md` §6 (tabla de skills).

## Convenciones de entorno

- **Documentación sin emojis como iconos.** Marcadores de estado en texto (`COMPLETADA`/`EN CURSO`/
  `PENDIENTE`) y checkboxes Markdown `[x]`/`[ ]`. Iconos = SVG propios con `currentColor` (también en
  frontend). Ver `spec.md` §4.
- **Puertos:** frontend Docker `3000`, dev `5173`; backend `8080`; PostgreSQL externo `5433` (interno `5432`).
- **Reportes de trabajo:** por sesión en `docs/reportes/` (estructura semanal). Demo al cerrar cada fase.
