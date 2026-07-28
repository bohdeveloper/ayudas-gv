# Documentación — Ayudas GV

Índice de esta carpeta. La **fuente de verdad viva** del proyecto está en la raíz:
[spec.md](../spec.md) (qué es, arquitectura, decisiones) y [plan.md](../plan.md) (trabajo pendiente).
Lo de aquí es material de apoyo: referencia técnica, aprendizaje y registro del avance.

## Documentación del sistema

| Documento | Contenido |
|-----------|-----------|
| [arquitectura.md](arquitectura.md) | Capas, módulos/bounded contexts, flujo de autenticación, endpoints y decisiones técnicas |
| [modelo-datos.md](modelo-datos.md) | Esquema entidad-relación completo por contextos, índices y notas de diseño |
| [tecnologias.md](tecnologias.md) | El stack explicado a fondo: Docker, PostgreSQL/Flyway, Spring Boot, React/Vite, Swagger, testing |
| [fase0-planificacion.md](fase0-planificacion.md) | Plan semana a semana de la Fase 0 (MVP) — **histórico**, ya completado |

## Docker y Kubernetes — [docker-k8s/](docker-k8s/)

Todo el material de la Fase K8s (learning track actual).

| Documento | Para qué |
|-----------|----------|
| [kubernetes-aprendizaje.md](docker-k8s/kubernetes-aprendizaje.md) | **El manual de trabajo**: guía paso a paso con ejercicios, comandos y manifiestos de referencia (Apéndice B). Explicada con 25 analogías cotidianas |
| [kubernetes-desde-0.md](docker-k8s/kubernetes-desde-0.md) | **El relato divulgativo**: qué se hizo en cada bloque, por qué y qué se aprendió, sin dar nada por sabido. Se lee de corrido, sin ejecutar nada |
| [docker-guia.md](docker-k8s/docker-guia.md) | Referencia de Docker (Bloque 0): instalación, cada archivo Docker del proyecto y la red entre contenedores |
| [entrevista-docker-k8s.md](docker-k8s/entrevista-docker-k8s.md) | Preparación para explicarlo en voz alta: modelo visual en diagramas Mermaid + 30 preguntas con respuesta modelo + chuleta de comandos |

**Por dónde empezar:** si no has tocado Kubernetes nunca → `kubernetes-desde-0.md`. Si vas a ejecutar los
ejercicios → `kubernetes-aprendizaje.md`. Si tienes una reunión mañana → `entrevista-docker-k8s.md`.

## Diagramas — [diagramas/](diagramas/)

Ficheros `.excalidraw` (abrir en [excalidraw.com](https://excalidraw.com) o con la extensión de VS Code).

| Diagrama | Contenido |
|----------|-----------|
| [docker-entorno.excalidraw](diagramas/docker-entorno.excalidraw) | El stack levantado con Docker Compose |
| [kubernetes-entorno.excalidraw](diagramas/kubernetes-entorno.excalidraw) | El mismo stack desplegado en el clúster |

Los diagramas **Mermaid** (versionables y renderizables en GitHub/GitLab) están dentro de
[entrevista-docker-k8s.md](docker-k8s/entrevista-docker-k8s.md) § Parte 1.

## Reportes — [reportes/](reportes/)

- [itinerario.md](reportes/itinerario.md) — calendario de las fases restantes y fin proyectado
- `semanaN/` — reporte diario de cada sesión (`AAAA-MM-DD-diadelasemana.md`)
- `semanas/` — reporte semanal en HTML, generado los viernes

## Instrucciones iniciales — [instrucciones-iniciales/](instrucciones-iniciales/)

Enunciado original del proyecto y planificación de la Fase 0 en `.docx`. **Fuente de verdad del alcance**:
ante una duda sobre qué hay que construir, manda el enunciado.

---

*Reorganizada el 2026-07-28: los cuatro documentos de Docker/K8s pasaron a `docker-k8s/`, los `.excalidraw`
a `diagramas/`, y `docker-k8s-diagramas.md` + `entrevista-docker-k8s-preguntas.md` se fusionaron en
`entrevista-docker-k8s.md` sin perder contenido. Los reportes anteriores a esa fecha citan las rutas viejas.*
