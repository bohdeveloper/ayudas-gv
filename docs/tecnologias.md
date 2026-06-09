# Tecnologías, ecosistema y comunicaciones

> **Documento vivo** — se actualiza al finalizar cada semana de desarrollo con las nuevas piezas incorporadas al sistema.
>
> Última actualización: **Semana 1 — 2026-06-09**

---

## Índice

1. [Por qué este stack](#1-por-qué-este-stack)
2. [El ecosistema Docker](#2-el-ecosistema-docker)
3. [Comunicaciones entre contenedores](#3-comunicaciones-entre-contenedores)
4. [Base de datos: PostgreSQL y Flyway](#4-base-de-datos-postgresql-y-flyway)
5. [Backend: Spring Boot](#5-backend-spring-boot)
6. [Frontend: React + Vite](#6-frontend-react--vite)
7. [API docs: Swagger UI](#7-api-docs-swagger-ui)
8. [Configuración por entorno](#8-configuración-por-entorno)

---

## 1. Por qué este stack

| Decisión | Alternativa descartada | Razón |
|----------|----------------------|-------|
| **JdbcTemplate** (SQL explícito) | JPA/Hibernate | Requisito del proyecto para aprender SQL real sin magia |
| **Zustand** (estado global React) | Redux Toolkit | Menos boilerplate, API más simple para proyectos de tamaño medio |
| **jjwt** (librería JWT) | Spring Security OAuth2 | Más control explícito sobre la generación y validación del token |
| **react-i18next** | i18n-js | Estándar de facto en React, integración nativa con hooks |
| **Columnas `_es`/`_eu`** en BD | Tabla separada de traducciones | Solución más simple para exactamente 2 idiomas fijos |
| **Monolito modular** | Microservicios desde el día 1 | El enunciado lo recomienda explícitamente; facilita el aprendizaje |
| **PostgreSQL 15** | MySQL/MariaDB | Mejor soporte de tipos avanzados, extensiones y JSON |
| **Flyway** (migraciones) | Liquibase | Más simple, basado en SQL puro sin XML/YAML |

---

## 2. El ecosistema Docker

### Qué es Docker Compose

Docker Compose es una herramienta que permite definir y arrancar **varios contenedores como si fueran uno solo**. En lugar de lanzar tres comandos manuales, un solo `docker compose up` levanta todo el sistema coordinado.

Un **contenedor** es como una caja sellada que contiene una aplicación y todo lo que necesita para funcionar (sistema operativo mínimo, runtime, librerías). Esto garantiza que el entorno es idéntico en cualquier máquina.

### Los tres contenedores del proyecto

```
docker-compose.yml
│
├── postgres        ← Servidor de base de datos PostgreSQL 15
├── backend         ← Aplicación Spring Boot (Java 21)
└── frontend        ← Nginx sirviendo el build de React
```

#### `postgres` (imagen oficial `postgres:15-alpine`)

- **Qué hace**: servidor de base de datos relacional.
- **`alpine`**: variante ultraligera de Linux (~5 MB en lugar de los ~200 MB de Debian). Suficiente para un servidor de BD.
- **Persistencia**: el volumen `postgres_data` guarda los datos en disco aunque el contenedor se destruya y recree. Sin volumen, cada `docker compose up` empezaría con una BD vacía.
- **`healthcheck`**: Docker comprueba periódicamente con `pg_isready` si PostgreSQL ya acepta conexiones. El backend no arranca hasta que este check pase. Esto evita el error clásico de "la app arranca antes que la BD".

#### `backend` (imagen construida desde `./backend/Dockerfile`)

- **Qué hace**: sirve la API REST en el puerto 8080.
- **Multi-stage build** (explicado en el apartado 5): el `Dockerfile` usa dos etapas para que la imagen final sea pequeña y no incluya Maven ni el código fuente.
- **`depends_on: condition: service_healthy`**: espera explícitamente a que el healthcheck de postgres esté verde antes de arrancar.

#### `frontend` (imagen construida desde `./frontend/Dockerfile`)

- **Qué hace**: sirve la aplicación React compilada a través de **Nginx** en el puerto 80 (mapeado al 3000 en tu máquina).
- React es una SPA (Single Page Application): en producción se compila a HTML + JS + CSS estáticos. Nginx los sirve de forma eficiente.
- **`depends_on: backend`**: espera a que el contenedor backend esté levantado (no necesita healthcheck, solo que exista).

---

## 3. Comunicaciones entre contenedores

### La red Docker interna

Docker Compose crea automáticamente una **red privada** (`ayudas_gv_default`) a la que se conectan todos los servicios. Dentro de esa red:

- Cada contenedor tiene un **hostname igual a su nombre de servicio**.
- `backend` puede alcanzar a `postgres` mediante `postgres:5432`.
- `frontend` (Nginx) puede llegar a `backend` mediante `backend:8080`.
- Los contenedores **no son accesibles desde fuera** salvo por los puertos mapeados en `ports:`.

### Diagrama de comunicaciones

```
┌─────────────────────────────────────────────────────────┐
│                   Tu máquina (host)                     │
│                                                         │
│   Navegador          mvn spring-boot:run    DBeaver/psql │
│   :5173 (dev)        (solo dev local)       :5433*      │
│   :3000 (docker)                            *5432=PG18  │
└──────┬─────────────────────────┬──────────────┬─────────┘
       │ :3000→:80               │ :8080→:8080  │ :5433→:5432
       ▼                         ▼              ▼
┌─────────────────────────────────────────────────────────┐
│              Red Docker: ayudas_gv_default              │
│                                                         │
│  ┌─────────────────┐    ┌─────────────────┐            │
│  │    frontend      │    │    backend       │            │
│  │  Nginx :80       │    │  Spring Boot     │            │
│  │                  │    │  :8080           │            │
│  │  /api/* ──────────────►  /api/v1/...    │            │
│  │  /* → index.html │    │                 │            │
│  └─────────────────┘    └────────┬────────┘            │
│                                   │ postgres:5432        │
│                          ┌────────▼────────┐            │
│                          │    postgres      │            │
│                          │  PostgreSQL 15   │            │
│                          │  :5432           │            │
│                          └─────────────────┘            │
└─────────────────────────────────────────────────────────┘
```

### Flujo de una petición desde el navegador (en Docker)

```
1. Navegador → http://localhost:3000/expedientes
   └─ Nginx recibe la petición
   └─ No empieza por /api → sirve index.html (la SPA)
   └─ React Router toma el control en el navegador

2. React hace fetch → http://localhost:3000/api/v1/expedientes
   └─ Nginx: empieza por /api → proxy_pass http://backend:8080
   └─ Spring Boot procesa la petición
   └─ Consulta a postgres:5432 con JDBC
   └─ Devuelve JSON al frontend
   └─ React renderiza los datos
```

### Flujo en desarrollo local (sin Docker para el código)

```
1. Navegador → http://localhost:5173/expedientes
   └─ Vite dev server sirve la SPA en caliente (HMR)

2. React hace fetch → http://localhost:5173/api/v1/expedientes
   └─ vite.config.ts: proxy /api/* → http://localhost:8080
   └─ Spring Boot (lanzado con mvn spring-boot:run) procesa
   └─ Consulta a localhost:5433 (PostgreSQL en Docker — puerto externo)
   └─ Devuelve JSON
```

### Por qué el perfil `dev` no funciona dentro de Docker

`application-dev.yml` sobreescribe la URL de la BD con `localhost:5432`. Dentro del contenedor `backend`, `localhost` es **el propio contenedor** — ahí no hay PostgreSQL. La BD está en otro contenedor, accesible solo como `postgres:5432` (nombre de servicio Docker).

`application.yml` base usa `${DB_URL:jdbc:postgresql://localhost:5432/...}`: si existe la variable de entorno `DB_URL`, la usa; si no, cae al default. `docker-compose.yml` inyecta `DB_URL=jdbc:postgresql://postgres:5432/ayudas_gv`, que sí funciona dentro de la red Docker.

**Regla**: perfil `dev` = desarrollo local (fuera de Docker). Docker = perfil por defecto + variables de entorno del `docker-compose.yml`.

---

## 4. Base de datos: PostgreSQL y Flyway

### PostgreSQL 15

PostgreSQL es el sistema gestor de base de datos relacional (RDBMS) del proyecto. Se eligió sobre MySQL/MariaDB por su mejor soporte de:
- Tipos avanzados (`JSONB`, `UUID`, arrays)
- Integridad referencial estricta
- Extensiones (`pg_trgm` para búsqueda textual, útil en fases futuras)

### Flyway — migraciones versionadas

Flyway resuelve el problema de **sincronizar el esquema de la BD con el código**. Sin Flyway, hay que ejecutar scripts SQL manualmente cada vez que cambia el esquema. Con Flyway, es automático.

#### Cómo funciona Flyway (arranque del backend)

```
Spring Boot arranca
       │
       ▼
Flyway conecta a la BD
       │
       ▼
¿Existe tabla flyway_schema_history?
       ├── No → la crea
       └── Sí → lee qué versiones ya están aplicadas
       │
       ▼
Lee los archivos en classpath:db/migration/
  V1__security_catalogs.sql
  V2__expedientes.sql
  V3__auditoria_indices.sql
  V4__tramitacion.sql
       │
       ▼
Compara versiones aplicadas vs. archivos en disco
       │
       ▼
Ejecuta solo las pendientes, en orden
       │
       ▼
Spring Boot continúa el arranque normal
```

#### Convención de nombres

```
V{version}__{descripcion}.sql
│            │
│            └── Doble guión bajo obligatorio
└── V + número de versión (entero o decimal: V1, V2, V1.1...)
```

**Importante**: una vez aplicado un archivo, Flyway guarda su checksum (hash). Si modificas un archivo ya aplicado, Flyway lo detecta y **falla el arranque**. Esto protege la integridad: los cambios siempre se hacen con un nuevo archivo de migración.

#### Las 4 migraciones del proyecto

| Archivo | Qué crea |
|---------|---------|
| `V1__security_catalogs.sql` | `usuario`, `rol`, `usuario_rol`, `tipo_ayuda`, `estado_expediente`, `tipo_tramite` + datos semilla |
| `V2__expedientes.sql` | `persona`, `expediente`, `expediente_interesado`, `datos_contacto`, `solicitud_ayuda`, tablas de detalle por tipo de ayuda + datos de ejemplo |
| `V3__auditoria_indices.sql` | `auditoria` + índices de rendimiento en las tablas principales |
| `V4__tramitacion.sql` | `tramite` + índices (preparado para Fase 1) |

---

## 5. Backend: Spring Boot

### Arquitectura multi-stage del Dockerfile

```dockerfile
# Etapa 1: compilación (imagen grande con Maven + JDK)
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q    # descarga dependencias (cacheado por Docker)
COPY src ./src
RUN mvn package -DskipTests -q      # compila y empaqueta → target/app.jar

# Etapa 2: runtime (imagen mínima solo con JRE)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar   # copia solo el JAR
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Por qué dos etapas**: La imagen de compilación pesa ~600 MB (Maven + JDK completo + código fuente). La imagen de runtime pesa ~100 MB (solo JRE + el JAR). El resultado final es la imagen pequeña — Maven y el código fuente no llegan a producción.

**Por qué se copia `pom.xml` antes que el `src/`**: Docker cachea cada capa. Si solo cambian archivos `.java` pero no el `pom.xml`, la capa de `mvn dependency:go-offline` se reutiliza del caché y la build tarda mucho menos.

### Módulos de la aplicación (Spring Boot)

```
com.ayudasgv/
├── AyudasGvApplication.java    ← punto de entrada (@SpringBootApplication)
├── config/
│   ├── OpenApiConfig.java      ← configura título y descripción de Swagger
│   └── SecurityConfig.java     ← define qué rutas son públicas/protegidas
├── auth/                       ← Semana 2: login, JWT, filtros de seguridad
├── expedientes/                ← Semanas 4-5: búsqueda y consulta
├── tramitacion/                ← Fase 1: trámites
└── commons/                    ← Semana 6: auditoría AOP, errores globales
```

### Spring Security (estado actual — Semana 1)

Con solo `SecurityConfig.java` configurado:

```
Petición entrante
       │
       ▼
¿La ruta empieza por /v3/api-docs/**, /swagger-ui/**?
       ├── Sí → acceso libre (permitAll)
       └── No → requiere autenticación (authenticated)
              │
              └── Sin configurar aún → Spring usa autenticación
                  básica con usuario "user" y contraseña generada
                  en cada arranque (visible en los logs)
```

En la **Semana 2** reemplazaremos la autenticación básica por JWT real.

### Cómo Spring Boot lee la configuración

Spring Boot combina propiedades de múltiples fuentes, en orden de prioridad (mayor a menor):

1. Variables de entorno (del sistema o del `docker-compose.yml`)
2. `application-{profile}.yml` (perfil activo)
3. `application.yml` (base)

Ejemplo: `DB_URL` en el entorno sobreescribe `spring.datasource.url` en el YAML.

---

## 6. Frontend: React + Vite

### Multi-stage del Dockerfile

```dockerfile
# Etapa 1: build (Node + npm)
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./          # copia package.json Y package-lock.json
RUN npm ci                     # instala dependencias exactas del lockfile
COPY . .
RUN npm run build              # tsc + vite build → genera /app/dist/

# Etapa 2: servidor estático (Nginx ultraligero)
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

**`npm ci` vs `npm install`**: `ci` (clean install) exige que exista `package-lock.json` y lo respeta estrictamente — todas las máquinas y el contenedor instalan exactamente las mismas versiones. `npm install` puede actualizar versiones menores, lo que puede introducir diferencias entre entornos.

**Por qué Nginx y no Node en producción**: Vite es un servidor de desarrollo con recarga en caliente (HMR). No está diseñado para producción. El comando `npm run build` genera archivos estáticos optimizados (HTML + JS minificado + CSS). Nginx los sirve de forma eficiente sin necesitar Node en runtime.

### Cómo Nginx gestiona la SPA y el proxy

```nginx
# nginx.conf (simplificado)
location /api/ {
    proxy_pass http://backend:8080;   # redirige /api/* al contenedor backend
}

location / {
    try_files $uri $uri/ /index.html; # cualquier otra ruta → sirve index.html
}
```

`try_files` es la clave del funcionamiento de las SPA. Cuando el usuario navega directamente a `/expedientes/123`, Nginx no encuentra ese archivo en disco (es una ruta de React Router, no un fichero real), así que sirve `index.html`. React carga, lee la URL del navegador y renderiza la vista correcta.

### TypeScript 6 y configuración

```json
// tsconfig.json (puntos clave)
{
  "moduleResolution": "bundler",  // delega la resolución al bundler (Vite)
  "ignoreDeprecations": "6.0",    // silencia aviso de baseUrl en TS 6.x
  "baseUrl": ".",                 // base para los paths alias
  "paths": { "@/*": ["src/*"] }  // import de "@/components/X" → "src/components/X"
}
```

**Por qué `moduleResolution: bundler`**: Vite gestiona los imports durante el build. Este modo le dice a TypeScript que confíe en el bundler para la resolución de módulos, en lugar de aplicar las reglas de Node.js.

---

## 7. API docs: Swagger UI

Swagger UI se integra a través de la dependencia `springdoc-openapi-starter-webmvc-ui`. Al arrancar, springdoc:

1. Escanea todos los `@RestController` de la aplicación.
2. Genera automáticamente el contrato OpenAPI 3.0 en formato JSON: `GET /api/v3/api-docs`.
3. Sirve la interfaz visual en `/api/swagger-ui.html` (redirige a `/api/swagger-ui/index.html`).

La clase `OpenApiConfig.java` añade los metadatos del proyecto (título, descripción, versión) que aparecen en la cabecera de Swagger UI.

En la **Semana 2** añadiremos el esquema Bearer para que Swagger UI permita enviar el token JWT en las peticiones protegidas.

---

## 8. Configuración por entorno

| Situación | Perfil activo | Fuente de DB_URL |
|-----------|--------------|-----------------|
| Docker Compose | ninguno (default) | Variable de entorno inyectada por docker-compose.yml → `postgres:5432` (red interna) |
| Desarrollo local | `dev` | `application-dev.yml` hardcodeado → `localhost:5432` (PG local) |
| DBeaver / psql externo | — | Conectar a `localhost:5433` (puerto externo del contenedor; 5432 lo ocupa PG18 local) |

```bash
# Arranque local con perfil dev
mvn spring-boot:run -Dspring.profiles.active=dev

# Arranque en Docker (sin perfil → usa application.yml + variables de entorno)
# docker-compose.yml inyecta: DB_URL, DB_USER, DB_PASSWORD, JWT_SECRET, SERVER_PORT
```

---

*Este documento se actualizará al finalizar cada semana con las nuevas tecnologías y patrones incorporados.*

| Semana | Añadido |
|--------|---------|
| S1 | Docker Compose, PostgreSQL, Flyway, Spring Boot básico, React+Nginx, Swagger UI |
| S2 | Spring Security, JWT, BCrypt, filtros de autenticación *(próximamente)* |
| S3 | React Router, Zustand, axios interceptors, react-i18next *(próximamente)* |
| S4 | JdbcTemplate avanzado, paginación SQL, búsqueda dinámica *(próximamente)* |
| S5 | Diseño de DTOs por sección, composición de vistas React *(próximamente)* |
| S6 | Spring AOP, MDC/correlation-id, JUnit 5, Mockito, React Testing Library *(próximamente)* |
