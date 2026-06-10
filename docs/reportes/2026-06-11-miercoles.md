# Reporte diario — Miércoles 11/06/2026

## ✅ Completado

### Estudio: estructura de proyecto y sistema de build

- Explorado el árbol completo de carpetas: `backend/src/main/java`, `backend/src/main/resources`, `frontend/src`
- Leído `pom.xml` entero: identificadas todas las dependencias y para qué sirve cada una
  - `spring-boot-starter-security` → filtros de seguridad HTTP
  - `spring-boot-starter-jdbc` + `flyway-database-postgresql` → acceso a BD y migraciones
  - `jjwt-api / jjwt-impl` → generación y validación de tokens JWT (semana 2)
  - `springdoc-openapi-starter-webmvc-ui` → Swagger UI automático
  - `lombok` → elimina código boilerplate (getters, constructores)
- Leído `package.json`: entendidos los scripts `dev`, `build`, `preview` y `test`
- Estudiado `docker-compose.yml`: servicios `postgres`, `backend`, `frontend`, redes y volúmenes
- Estudiado `Dockerfile` del backend: build multi-stage con Maven wrapper + imagen JRE 21 final
- Estudiado `Dockerfile` del frontend: build Vite + Nginx como servidor estático

### Arranque del entorno y verificación

- `docker-compose up --build` ejecutado: 3 contenedores levantados correctamente
- Verificado en Docker Desktop que `postgres`, `backend` y `frontend` están en estado `running`
- Comprobado acceso a frontend en `http://localhost:3000` → pantalla inicial React (HTTP 200)
- Comprobado acceso a Swagger UI en `http://localhost:8080/api/swagger-ui.html` (HTTP 200)

## 🔜 Pendiente

- Profundizar en las migraciones Flyway y el modelo de datos (mañana)

## 🚧 Bloqueos

Ninguno.

---
*Hito del día: Estructura del proyecto y flujo de build comprendidos de punta a punta.*
