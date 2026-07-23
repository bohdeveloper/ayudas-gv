# Reporte diario — Miércoles 22/07/2026

> Fase K8s, Bloque 0: repaso de Docker consolidado sobre el proyecto real, antes de dar el salto a Kubernetes.
> Verificación del herramental en el PC nuevo y cierre del bloque. Un commit (19:47).

## Completado

### Verificación del herramental (PC nuevo)

- Docker Desktop 29.6.2 + Compose v5.3.1 instalados y **verificados**: stack completo levantado y login OK
- Java 21 + Maven 3.9 + Node presentes. Anotado que **faltan `kubectl` y `minikube`** — se instalarán al
  empezar el Bloque 1

### Bloque 0 — Repaso de Docker (consolidación)

- **Stack en marcha:** `docker compose up --build` → 3 contenedores `Up` (postgres `healthy`), migraciones
  Flyway V1–V5 aplicadas, login `admin` OK a través de Nginx
- **Multi-stage builds explicados por escrito** (`docker-guia.md` §3.1–3.2): qué copia cada Dockerfile de la
  etapa de build a la final y por qué la imagen final no lleva Maven/Node (imagen pequeña, sin herramientas
  de build, menor superficie de ataque)
- **Red por nombre de servicio interiorizada** (`docker-guia.md` §4) — base del DNS de Kubernetes:
  demostrado en vivo que Flyway conecta a `postgres:5432` y Nginx proxea a `backend:8080` por el nombre del
  servicio, no por `localhost`
- Redactada `docs/docker-guia.md` (340 líneas): referencia de instalación, cada archivo Docker del proyecto y
  la comunicación entre contenedores. Añadida a la tabla de documentación en `spec.md`
- Los tres puntos del Bloque 0 marcados `[x]` en `plan.md`; bloque **COMPLETADA (2026-07-22)** — (commit `7667e49`)

## Pendiente (Fase K8s)

- **Bloque 1** (próxima sesión): instalar minikube + kubectl, `minikube start --driver=docker`,
  `kubectl get nodes`, construir las imágenes `:1.0` dentro del Docker de minikube
- Bloques 2–4: crear `k8s/` y desplegar; operar (escalar, self-healing, rolling update); ejercicios integradores

## Bloqueos

Ninguno.

---
*Hito del día: **Bloque 0 de la Fase K8s cerrado.** Stack Docker verificado en el PC nuevo (3 contenedores,
Flyway V1–V5, login OK), multi-stage builds y red por nombre de servicio documentados en `docker-guia.md` —
listo el modelo mental de Docker para dar el salto a Kubernetes.*

<!-- evidencias
Commit 7667e49: +docs/docker-guia.md (340), plan.md (Bloque 0 COMPLETADA), spec.md (+referencia docker-guia)
docker compose ps: 3 contenedores Up, postgres healthy; login admin OK vía Nginx
-->
