# docker-guia.md — Docker en Ayudas GV (repaso Bloque 0)

> Guía de referencia del entorno de contenedores del proyecto: qué se instala y por qué, qué hace
> cada archivo Docker y cómo se comunican las tres piezas entre sí.
> Es el material escrito del **Bloque 0 de la Fase K8s** (ver [plan.md](../plan.md)).
> El "cómo operar el clúster" está en [kubernetes-aprendizaje.md](kubernetes-aprendizaje.md).

---

## 1. Qué resuelve Docker aquí

El proyecto tiene tres piezas que normalmente instalarías a mano (una base de datos PostgreSQL,
un backend Java y un frontend servido por un servidor web). Docker empaqueta cada una en un
**contenedor**: una caja aislada con su propio sistema de ficheros, sus dependencias y su versión
exacta de software. Así no instalas Postgres ni Java-como-servidor ni Nginx en Windows: los ejecutas
dentro de contenedores idénticos a los de cualquier otro PC. "En mi máquina funciona" deja de ser
un problema.

### Vocabulario mínimo

| Término | Qué es |
|---|---|
| **Imagen** | Plantilla inmutable (el "molde"): un sistema de ficheros + instrucciones de arranque. Se construye una vez. |
| **Contenedor** | Instancia en ejecución de una imagen (el "objeto" del molde). Efímero. |
| **Dockerfile** | Receta para *construir* una imagen. |
| **docker-compose.yml** | Director de orquesta: define varios contenedores y cómo se relacionan. |
| **Volumen** | Almacenamiento persistente que sobrevive aunque borres el contenedor (para que la BD no pierda datos). |
| **Red (network)** | LAN privada que Docker crea para que los contenedores se hablen por nombre. |

---

## 2. Instalación en Windows (paso a paso)

El stack de desarrollo (Java 21, Maven, Node) permite el modo dev sin Docker. Para el modo
contenedores solo hace falta **Docker Desktop**, que en Windows trae de un tiro el motor Docker +
`docker compose` + una versión de `kubectl`. `minikube` se instala más adelante (Bloque 1).

### Por qué Docker Desktop necesita WSL2

Docker es tecnología de Linux (usa el kernel de Linux para aislar contenedores). Windows no tiene ese
kernel, así que Docker Desktop se apoya en **WSL2** (Windows Subsystem for Linux 2), un kernel de
Linux real integrado en Windows. Docker Desktop mete su motor dentro de WSL2 y lo presenta en Windows
de forma transparente. Orden correcto: primero WSL2, luego Docker Desktop.

### Pasos

1. **Habilitar WSL2** — PowerShell **como Administrador**:
   ```powershell
   wsl --install
   ```
   Instala WSL2 + Ubuntu y activa la virtualización. Reiniciar el PC cuando lo pida.
   (Si ya había WSL: `wsl --update` y `wsl --set-default-version 2`.)

2. **Instalar Docker Desktop** — PowerShell normal:
   ```powershell
   winget install -e --id Docker.DockerDesktop
   ```

3. **Arrancar Docker Desktop** una vez desde el menú de inicio. Esperar a que el icono de la ballena
   deje de animarse (motor listo). El motor solo corre mientras Docker Desktop está abierto.

4. **Verificar**:
   ```powershell
   docker --version
   docker compose version
   docker run --rm hello-world
   ```

### Detalle práctico: el PATH de las terminales ya abiertas

Docker Desktop añade su carpeta `bin` al PATH del sistema **al instalarse**. Una terminal que ya
estuviera abierta **antes** de la instalación conserva el PATH antiguo y no encuentra el comando
`docker`. Solución: abrir una terminal nueva, o refrescar el PATH en la sesión actual:
```powershell
$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" +
            [System.Environment]::GetEnvironmentVariable("Path","User")
```

---

## 3. Los archivos Docker del proyecto, uno a uno

Cuatro archivos relevantes. (No hay `.dockerignore` en `backend/` ni en `frontend/` — mejora pendiente
apuntada al final.)

### 3.1 `backend/Dockerfile` — imagen del backend Java

```dockerfile
FROM maven:3.9-eclipse-temurin-21-alpine AS build   # (1) ETAPA 1: compilar
WORKDIR /app
COPY pom.xml .                                       # (2) copiar SOLO el pom primero
RUN mvn dependency:go-offline -q                     # (3) descargar dependencias
COPY src ./src                                       # (4) ahora sí, el código fuente
RUN mvn package -DskipTests -q                       # (5) compilar → genera el .jar

FROM eclipse-temurin:21-jre-alpine                   # (6) ETAPA 2: ejecutar (imagen limpia)
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar          # (7) traer SOLO el .jar de la etapa 1
EXPOSE 8080                                           # (8) documentar el puerto
ENTRYPOINT ["java", "-jar", "app.jar"]               # (9) comando de arranque
```

Es un **multi-stage build**:

- **Etapa 1 (`build`)** usa una imagen gorda con Maven + JDK 21 completo (todo lo necesario para
  *compilar*). Produce `target/*.jar`.
- **Etapa 2** parte de una imagen limpia con solo el **JRE** (para *ejecutar*, no compilar). Con
  `COPY --from=build` se lleva únicamente el `.jar` y descarta todo lo demás.

**Por qué:** la imagen final no contiene Maven, ni el código fuente, ni el `.m2`, ni el JDK completo
— solo JRE + tu `.jar`. Más pequeña, arranca antes y con menos superficie de ataque (menos software =
menos vulnerabilidades). Encaja con el invariante de seguridad de [spec.md §3](../spec.md).

**Caché de capas (líneas 2-3-4):** Docker construye por capas y cachea cada instrucción. Copiar
primero solo `pom.xml` y bajar dependencias, y *después* copiar `src`, hace que si cambias código Java
pero no las dependencias, Docker reutiliza la capa cacheada de dependencias sin volver a descargarlas.

- `-DskipTests`: los tests se ejecutan en tu máquina / CI, no al construir el artefacto de despliegue.
- `EXPOSE 8080`: es documentación (declara el puerto); no abre nada por sí solo.
- `ENTRYPOINT`: lanza el jar de Spring Boot al arrancar el contenedor.

### 3.2 `frontend/Dockerfile` — imagen del frontend

```dockerfile
FROM node:20-alpine AS build                # (1) ETAPA 1: construir la SPA
WORKDIR /app
COPY package*.json ./                        # (2) package.json + lock primero (caché)
RUN npm ci                                    # (3) instalar dependencias exactas del lock
COPY . .                                      # (4) copiar el resto del código
RUN npm run build                             # (5) Vite compila → /app/dist (estáticos)

FROM nginx:alpine                             # (6) ETAPA 2: servir los estáticos
COPY --from=build /app/dist /usr/share/nginx/html          # (7) el build al webroot de Nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf              # (8) nuestra config de Nginx
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]            # (9) arrancar Nginx en primer plano
```

Mismo patrón, con un matiz conceptual:

- **Etapa 1** usa Node solo para **compilar** React+Vite+TS en ficheros estáticos (`dist/`). Node
  **no** sirve la app en producción; es solo el compilador.
- **Etapa 2** es un **Nginx** que sirve esos estáticos. La imagen final **no lleva Node**.

- `npm ci` (no `npm install`): instala exactamente las versiones del `package-lock.json`, sin
  actualizar nada. Builds reproducibles.
- `daemon off;`: un contenedor vive mientras viva su proceso principal en primer plano. Nginx por
  defecto se va a segundo plano (mataría el contenedor); esto lo fuerza a quedarse en primer plano.

### 3.3 `frontend/nginx.conf` — la lógica de red del frontend

```nginx
server {
    listen 80;
    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;      # (A) SPA fallback
    }

    location /api/ {
        proxy_pass http://backend:8080/api/;   # (B) proxy inverso al backend
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

Hace dos trabajos:

**(A) SPA fallback** — React Router maneja las rutas en el navegador (`/expedientes`, `/login`...),
pero esas rutas no existen como ficheros en el servidor. Al recargar en `/expedientes`, Nginx buscaría
un fichero inexistente y daría 404. `try_files ... /index.html` dice: "si no encuentras el fichero,
sirve `index.html`" y deja que React Router resuelva la ruta en el cliente.

**(B) Proxy inverso `/api/`** — la pieza clave de comunicación. El JavaScript del navegador llama a la
API con rutas **relativas** (`/api/v1/auth/login`). Cuando la petición llega a Nginx:
- Todo lo que empiece por `/api/` no se sirve como estático, sino que se **reenvía** (`proxy_pass`) a
  `http://backend:8080/api/`.
- `backend` no es una IP ni `localhost`: es el **nombre del servicio** del `docker-compose.yml`, que
  Docker resuelve por DNS interno (ver §4). Es la base del DNS de Kubernetes.

**Por qué el proxy en vez de llamar directo al `:8080`:** para el navegador, la web y la API vienen
del mismo origen `localhost:3000`. No hay peticiones cruzadas entre orígenes, no hay que configurar
CORS, y el frontend no necesita saber dónde vive el backend: pide `/api` "a sí mismo" y Nginx reenvía.

### 3.4 `docker-compose.yml` — el director de orquesta

Los Dockerfiles construyen imágenes sueltas; el compose las orquesta juntas. Define tres servicios.

**`postgres` (base de datos):**
```yaml
postgres:
  image: postgres:15-alpine          # imagen oficial ya hecha (no la construimos)
  environment:
    POSTGRES_DB: ayudas_gv
    POSTGRES_USER: ayudasgv
    POSTGRES_PASSWORD: ayudasgv
  ports:
    - "5433:5432"                    # host 5433 → contenedor 5432
  volumes:
    - postgres_data:/var/lib/postgresql/data    # persistencia
  healthcheck:
    test: ["CMD-SHELL", "pg_isready -U ayudasgv -d ayudas_gv"]
    interval: 10s
```
- Usa la **imagen oficial** de Postgres: no hay Dockerfile para la BD, se descarga lista.
- `ports: "5433:5432"` — mapeo **host:contenedor**. Dentro escucha en `5432`; hacia Windows se expone
  en `5433`. Es deliberado ([spec.md §5](../spec.md)): el 5432 se reserva por si hay un Postgres local.
- `volumes: postgres_data:/var/lib/postgresql/data` — al mapear el directorio de datos a un **volumen
  con nombre**, los datos sobreviven aunque borres el contenedor. Por eso `docker compose down`
  conserva datos y `docker compose down -v` los borra.
- `healthcheck` — `pg_isready` periódico para saber si Postgres acepta conexiones de verdad. Habilita
  el `depends_on ... service_healthy` del backend.

**`backend` (Spring Boot):**
```yaml
backend:
  build:
    context: ./backend               # construir con backend/Dockerfile
  ports:
    - "8080:8080"
  environment:
    DB_URL: jdbc:postgresql://postgres:5432/ayudas_gv   # ← nombre de servicio + puerto interno
    DB_USER: ayudasgv
    DB_PASSWORD: ayudasgv
    JWT_SECRET: cambia-esto-en-produccion-clave-secreta-larga
    JWT_EXPIRATION: 86400000
    SERVER_PORT: 8080
  depends_on:
    postgres:
      condition: service_healthy
```
- `build: context: ./backend` — esta imagen **se construye** con el Dockerfile del backend.
- `DB_URL: ...//postgres:5432/...` — el backend se conecta usando el host **`postgres`** (nombre de
  servicio) y el puerto **interno `5432`** (no el 5433; el 5433 solo existe hacia Windows). Entre
  contenedores se usan los puertos internos.
- Estas variables son las "críticas" de [spec.md §5](../spec.md), leídas por `application.yml`.
  `JWT_SECRET` es de ejemplo a propósito (deuda formativa; en prod → Secret de Kubernetes).
- `depends_on ... service_healthy` — el backend no arranca hasta que el healthcheck de Postgres pasa.
  Importante: al arrancar, Spring ejecuta las **migraciones Flyway** (V1…V4) que crean las tablas.

**`frontend` (React + Nginx):**
```yaml
frontend:
  build:
    context: ./frontend
  ports:
    - "3000:80"                      # host 3000 → contenedor 80
  depends_on:
    - backend
```
- Se construye con el Dockerfile del frontend.
- `ports: "3000:80"` — Nginx escucha en el `80` dentro; se expone como `3000` en Windows. Por eso
  abres `http://localhost:3000`.
- `depends_on: backend` — arranca después del backend (aquí solo orden, sin healthcheck).

**Volumen con nombre:**
```yaml
volumes:
  postgres_data:
```
Es lo que hace que los datos de Postgres persistan entre reinicios.

---

## 4. Cómo se comunican las tres piezas (la red)

Al ejecutar `docker compose up`, Docker crea automáticamente una **red privada** (bridge) y mete los
tres contenedores dentro. Dentro de esa red ocurren dos cosas:

1. **DNS por nombre de servicio.** Docker levanta un DNS interno donde cada servicio es resoluble por
   su nombre: `postgres`, `backend` y `frontend` funcionan como hostnames. Por eso el backend usa
   `postgres:5432` y Nginx reenvía a `backend:8080`, sin conocer IPs (que además cambian). **Es el
   mismo modelo que los Services de Kubernetes.**

2. **Puertos internos vs. externos.** Dentro de la red se usan los puertos reales (5432, 8080, 80).
   Los mapeos `ports:` (5433, 8080, 3000) son **solo para acceder desde Windows**, no para la
   comunicación entre contenedores.

### El recorrido de una petición real (login `admin` / `admin123`)

```
   [ Navegador ]
        |  1. abres  http://localhost:3000
        v
   host:3000 ----> contenedor frontend (Nginx, escucha en :80)
        |             2. Nginx sirve index.html + JS/CSS (el SPA de React)
        |
        |  3. el JS hace POST /api/v1/auth/login  (ruta RELATIVA, mismo origen :3000)
        v
   Nginx ve que empieza por /api/  --> proxy_pass  http://backend:8080/api/
        |                                  (resuelve "backend" por DNS interno de Docker)
        v
   contenedor backend (Spring Boot, escucha en :8080)
        |  4. valida credenciales; consulta la BD con
        |     jdbc:postgresql://postgres:5432/ayudas_gv
        v
   contenedor postgres (escucha en :5432 dentro de la red)
        |  5. devuelve el usuario; el backend valida BCrypt y emite un JWT
        ^
        |  6. la respuesta vuelve por el mismo camino:
        |     backend --> Nginx --> navegador
```

Ideas a retener:

- **El navegador solo habla con el `:3000`** (Nginx). Nunca contacta al backend ni a Postgres
  directamente; Nginx hace de puerta de entrada y de proxy. (El `:8080` está expuesto y puedes usarlo
  a mano, p. ej. Swagger en `http://localhost:8080/api/swagger-ui.html`, pero el flujo normal no lo usa.)
- **Postgres no se alcanza por el 5433 desde la app.** El backend lo usa por `postgres:5432` dentro de
  la red. El 5433 es solo para conectarte tú con un cliente SQL desde Windows.
- **`depends_on` solo garantiza "está listo" con healthcheck.** El backend espera al *healthcheck* de
  Postgres; el frontend solo espera a que el backend *arranque* su proceso. En K8s esto se resuelve con
  *readiness probes*.

---

## 5. Comandos de uso habitual

| Comando | Qué hace |
|---|---|
| `docker compose up --build` | Construye las imágenes y levanta los 3 contenedores (en primer plano). |
| `docker compose up --build -d` | Igual, en segundo plano (*detached*). |
| `docker compose ps` | Estado de los 3 contenedores. |
| `docker compose logs -f backend` | Sigue los logs del backend en vivo. |
| `docker compose down` | Para y elimina los contenedores (conserva el volumen/datos). |
| `docker compose down -v` | Para, elimina contenedores **y borra el volumen** (BD desde cero). |

---

## 6. Mejora pendiente: `.dockerignore`

No hay `.dockerignore` en `backend/` ni en `frontend/`. Al construir, Docker envía toda la carpeta
("context") al motor. Sin `.dockerignore`, el `COPY . .` del frontend arrastra `node_modules/` local,
`.git`, etc.: builds más lentos y a veces incorrectos (un `node_modules` de Windows pisando el
`npm ci` del contenedor). Añadir dos `.dockerignore` es una mejora pequeña y didáctica; se abordará
como punto propio en [plan.md](../plan.md) (regla SDD: nada se implementa sin estar en el plan).
