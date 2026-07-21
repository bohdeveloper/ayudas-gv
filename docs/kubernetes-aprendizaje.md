# Docker + Kubernetes paso a paso — Guía de aprendizaje sobre Ayudas GV

> Guía didáctica para pasar de "sé usar `docker compose`" a "sé desplegar y operar esta app en un
> clúster de Kubernetes". Está construida **sobre este proyecto real** (postgres + backend Spring Boot +
> frontend React/Nginx), no sobre ejemplos genéricos, para que cada concepto tenga un correlato tangible.
>
> Cómo usarla: sigue los bloques en orden. Cada bloque tiene **(1) teoría mínima**, **(2) manos a la obra**
> y **(3) ejercicios** con su verificación. No copies y pegues sin leer: el objetivo es interiorizar, no
> tener YAML que funcione. El checklist de progreso vive en [../plan.md](../plan.md) (fase "K8s").

**Marcadores:** `[ ]` pendiente · `[x]` hecho. Sin emojis (convención del proyecto).

---

## Índice

- [Parte 0 — Modelo mental: por qué existe Kubernetes](#parte-0)
- [Parte 1 — Docker, lo esencial ordenado](#parte-1)
- [Parte 2 — Los objetos de Kubernetes, uno a uno](#parte-2)
- [Parte 3 — Montar el clúster local (minikube)](#parte-3)
- [Parte 4 — Desplegar Ayudas GV en Kubernetes, servicio a servicio](#parte-4)
- [Parte 5 — Operar: escalar, autocurar, actualizar sin caída](#parte-5)
- [Parte 6 — Ejercicios integradores (los que te blindan para la entrevista)](#parte-6)
- [Parte 7 — Puente al futuro: microservicios del proyecto en K8s](#parte-7)
- [Apéndice A — Chuleta de comandos](#apendice-a)
- [Apéndice B — Manifiestos completos de referencia](#apendice-b)

---

<a name="parte-0"></a>
## Parte 0 — Modelo mental: por qué existe Kubernetes

Una frase para no perderte nunca:

> **Docker construye y ejecuta contenedores. Kubernetes los orquesta en un clúster: los reparte por
> varias máquinas, los reinicia si mueren, los escala, y actualiza versiones sin cortar el servicio.**

Con `docker compose up` tú levantas los contenedores en *tu* máquina y, si uno se cae, se queda caído
(salvo `restart: unless-stopped`, que es un parche local). Kubernetes convierte eso en un sistema
declarativo: tú describes **el estado deseado** ("quiero 3 copias del backend, siempre") y el clúster
trabaja continuamente para que la realidad coincida con esa descripción. Eso es lo que en las entrevistas
llaman *self-healing*, *rolling updates* y *desired state*.

**Analogía con lo que ya sabes (Compose → Kubernetes):**

| En `docker-compose.yml` tienes... | En Kubernetes se convierte en... |
|-----------------------------------|----------------------------------|
| un `service:` | un **Deployment** + un **Service** |
| `image:` / `build:` | el mismo, pero la imagen vive en un registro y el Pod la referencia |
| `ports:` publicados al host | un **Service** (interno) + **Ingress** (entrada externa) |
| `environment:` | un **ConfigMap** (config) + **Secret** (credenciales) |
| `volumes:` | un **PersistentVolumeClaim** (PVC) |
| la red por defecto (servicios se ven por nombre) | el DNS interno del clúster (te ves por el nombre del Service) |
| un `docker-compose.override.yml` por entorno | un **Namespace** por entorno + overlays (kustomize) |

Si retienes esta tabla, el resto es vocabulario.

---

<a name="parte-1"></a>
## Parte 1 — Docker, lo esencial ordenado

Antes de orquestar hay que dominar la unidad que se orquesta. Repaso ordenado (esto ya lo tocaste en
`ayudas_gv`, aquí queda consolidado):

- **Imagen** — plantilla **inmutable** definida en un `Dockerfile` (SO base + dependencias + tu app).
  Se construye una vez y no cambia. En este proyecto hay dos: `backend/Dockerfile` y `frontend/Dockerfile`.
- **Contenedor** — instancia en ejecución de una imagen. **Efímero**: si lo borras, se pierde todo lo que
  no esté en un volumen. Por eso la BD necesita volumen y el backend no.
- **Volumen** — persistencia de datos **fuera** del contenedor. Imprescindible para PostgreSQL
  (en Compose: `postgres_data:/var/lib/postgresql/data`).
- **Red** — los contenedores de un mismo Compose se ven **por el nombre del servicio**, no por `localhost`.
  Fíjate en `docker-compose.yml`: el backend conecta a `jdbc:postgresql://postgres:5432/...`, usa `postgres`
  (nombre del servicio), no `localhost`. **Este concepto es idéntico en Kubernetes** (te ves por el nombre
  del Service). Interiorizarlo ahora te ahorra el error más común de K8s.
- **Docker Compose** — describe varios servicios en un YAML. Los entornos se separan con
  `docker-compose.override.yml` o `--env-file`.
- **Multi-stage build** — compilas en una imagen pesada (Maven/Node) y copias **solo el artefacto** a una
  imagen ligera final. Los dos Dockerfiles del proyecto ya lo hacen (Maven→JRE, Node→Nginx). Pregunta
  típica de entrevista: *"¿por qué multi-stage?"* → imagen final pequeña, sin herramientas de build,
  menor superficie de ataque.

### Manos a la obra (repaso Docker)

```bash
# Construir y levantar todo el stack del proyecto
docker compose up --build -d

# Ver contenedores en marcha
docker compose ps

# Seguir logs del backend en vivo
docker compose logs -f backend

# Entrar a un contenedor a inspeccionar (shell)
docker compose exec backend sh
docker compose exec postgres psql -U ayudasgv -d ayudas_gv -c "\dt"

# Parar
docker compose down            # conserva el volumen (datos)
docker compose down -v         # borra también el volumen (BD desde cero)
```

### Ejercicios Parte 1

- [ ] **1.1** Levanta el stack y comprueba con `docker compose ps` que los 3 contenedores están `Up`.
- [ ] **1.2** Entra al contenedor de postgres y lista las tablas (`\dt`). Deberías ver `expediente`,
  `usuario`, `auditoria`, etc. (migraciones Flyway aplicadas).
- [ ] **1.3** Mira el `backend/Dockerfile` e identifica las dos etapas (`FROM ... AS build` y el `FROM`
  final). Responde por escrito: ¿qué se copia de la primera a la segunda y por qué la final no lleva Maven?
- [ ] **1.4** Borra el contenedor del backend (`docker compose rm -sf backend`) y vuelve a levantarlo.
  ¿Se perdieron los datos de la BD? ¿Por qué no? (respuesta: el volumen es de postgres, no del backend).

---

<a name="parte-2"></a>
## Parte 2 — Los objetos de Kubernetes, uno a uno

Kubernetes es declarativo: describes objetos en YAML y `kubectl apply` los crea/actualiza. Estos son los
que necesitas para esta app (de menor a mayor).

### Pod
Unidad mínima. Envuelve **1 (o pocos) contenedores** que comparten red y almacenamiento. **Nunca
despliegas Pods sueltos** en producción: son ganado, no mascotas — nacen y mueren. Un Pod que muere no
"revive"; alguien de nivel superior (el Deployment) crea uno nuevo.

### Deployment
Dice *"quiero **N réplicas** de este Pod, con esta imagen"* y se encarga de:
- **self-healing**: si un Pod muere, crea otro para mantener N.
- **rolling updates**: al cambiar la imagen, sustituye los Pods poco a poco, sin downtime.
- **rollback**: puede volver a la versión anterior.

Es el objeto que usarás para backend y frontend. Para la BD se suele usar un **StatefulSet** (identidad
estable y almacenamiento por réplica), pero para aprender con **una** réplica de postgres, un Deployment +
PVC es suficiente y más simple.

### Service
IP y **nombre DNS estables** delante de un conjunto de Pods (que cambian de IP al morir/nacer). Los Pods se
seleccionan por *labels*. Tipos:
- **ClusterIP** (por defecto): solo accesible **dentro** del clúster. Es lo que usarás para postgres y
  para la comunicación frontend→backend interna.
- **NodePort**: abre un puerto en cada nodo. Útil para pruebas locales rápidas.
- **LoadBalancer**: pide un balanceador externo (en la nube). En minikube se simula.

El nombre del Service **es** el DNS: el backend hablará a la BD por `postgres:5432`, igual que en Compose.

### Ingress
Entrada **HTTP/HTTPS** al clúster con **rutas y dominios** (el "Nginx del clúster"). En vez de exponer cada
Service, defines reglas: `/` → frontend, `/api` → backend. Necesita un *Ingress Controller* instalado
(en minikube: `minikube addons enable ingress`).

### ConfigMap / Secret
El equivalente K8s a tu bloque `environment:` de Compose, separando:
- **ConfigMap**: configuración **no sensible** (URL de la BD, puerto, tiempo de expiración del token).
- **Secret**: credenciales (usuario/contraseña de BD, `JWT_SECRET`). Se guardan en base64 (no es cifrado;
  en producción se protege con RBAC y/o un gestor externo — se menciona pero no es alcance del ejercicio).

### Namespace
Separación **lógica** dentro de un mismo clúster. Perfecto para tus tres entornos:
`ayudas-dev`, `ayudas-staging`, `ayudas-prod`. Los objetos de un namespace no colisionan con los de otro,
y el DNS se resuelve dentro del namespace (`postgres` desde `ayudas-dev` es el postgres de `ayudas-dev`).
Conecta directo con lo que ya hiciste con `--env-file`/overrides en Compose.

### PersistentVolume (PV) / PersistentVolumeClaim (PVC)
El volumen de Docker, en versión K8s. El **PVC** es la *petición* ("quiero 1Gi de disco"); el clúster la
satisface con un **PV** real. Postgres montará un PVC para no perder los datos cuando su Pod se recree.

### Probes (liveness / readiness) — clave para el self-healing
Kubernetes necesita saber si un contenedor está **vivo** y **listo**:
- **livenessProbe**: si falla repetidamente, K8s **reinicia** el contenedor.
- **readinessProbe**: si falla, el Service **deja de enviarle tráfico** (pero no lo reinicia).

El backend Spring Boot tarda en arrancar (JVM + Flyway), así que las probes evitan mandarle tráfico antes
de tiempo. Como el proyecto aún no expone `/actuator/health`, empezaremos con probes TCP al puerto 8080 y
dejaremos como ejercicio añadir actuator para probes HTTP "de verdad".

---

<a name="parte-3"></a>
## Parte 3 — Montar el clúster local (minikube)

`minikube` levanta un clúster de Kubernetes de **un nodo** dentro de una VM/contenedor en tu máquina.
Es el entorno ideal para aprender sin coste.

### Instalación (Windows)

```powershell
# Con winget (recomendado en Windows 11)
winget install Kubernetes.minikube
winget install Kubernetes.kubectl

# Verifica
minikube version
kubectl version --client
```

`minikube` necesita un *driver*: Docker Desktop es el más cómodo en Windows (usa `--driver=docker`).

### Arrancar y comprobar

```powershell
minikube start --driver=docker
kubectl get nodes            # debe salir 1 nodo "Ready"
kubectl cluster-info
minikube dashboard           # (opcional) panel web del clúster
```

### Cómo hará tu clúster para ver tus imágenes locales

Este es **el punto que más confunde** al empezar. Tu imagen `ayudas-gv-backend` la construye tu Docker,
pero el clúster corre en otra "máquina" (la VM de minikube) y no la ve. Tres opciones (usaremos la 1):

1. **Construir dentro del Docker de minikube** — apuntas tu CLI de Docker al demonio de minikube y
   construyes ahí. Entonces el clúster ya tiene la imagen:
   ```powershell
   # PowerShell: conecta tu shell al Docker de minikube
   & minikube -p minikube docker-env --shell powershell | Invoke-Expression
   docker build -t ayudas-gv-backend:1.0 ./backend
   docker build -t ayudas-gv-frontend:1.0 ./frontend
   ```
   ```bash
   # Bash equivalente
   eval $(minikube docker-env)
   docker build -t ayudas-gv-backend:1.0 ./backend
   docker build -t ayudas-gv-frontend:1.0 ./frontend
   ```
   Con esto, en los manifiestos usarás `imagePullPolicy: IfNotPresent` (o `Never`) para que **no** intente
   descargar la imagen de internet.

2. `minikube image load ayudas-gv-backend:1.0` — construyes con tu Docker normal y "subes" la imagen al
   clúster.

3. Publicar la imagen en un registro real (Docker Hub, GHCR) — es lo de producción, pero innecesario para
   aprender en local.

### Ejercicios Parte 3

- [ ] **3.1** Instala minikube y kubectl. `minikube start` y confirma `kubectl get nodes` → 1 nodo Ready.
- [ ] **3.2** Conecta tu shell al Docker de minikube y construye las dos imágenes del proyecto con tag
  `:1.0`. Verifica con `docker images | grep ayudas` que existen **dentro** de minikube.
- [ ] **3.3** Explica con tus palabras por qué, sin el paso 3.2, un `kubectl apply` de tu Deployment se
  quedaría en estado `ImagePullBackOff`.

---

<a name="parte-4"></a>
## Parte 4 — Desplegar Ayudas GV en Kubernetes, servicio a servicio

Crea una carpeta `k8s/` en la raíz del repo. Iremos añadiendo manifiestos y aplicándolos **en orden de
dependencia**: namespace → config/secretos → postgres → backend → frontend → ingress. Los manifiestos
completos están en el [Apéndice B](#apendice-b); aquí va el hilo conductor y el porqué de cada uno.

### Paso 4.1 — Namespace

Aísla todo el entorno de desarrollo:

```yaml
# k8s/00-namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: ayudas-dev
```

```bash
kubectl apply -f k8s/00-namespace.yaml
kubectl config set-context --current --namespace=ayudas-dev   # trabajar por defecto aquí
```

### Paso 4.2 — ConfigMap y Secret (la config del backend)

Traducimos el bloque `environment:` del `docker-compose.yml`. Lo **no sensible** al ConfigMap, las
**credenciales** al Secret. Fíjate en `DB_URL`: el host es `postgres` (el nombre del Service que crearemos),
exactamente como en Compose.

```yaml
# k8s/10-backend-config.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: backend-config
data:
  DB_URL: jdbc:postgresql://postgres:5432/ayudas_gv
  SERVER_PORT: "8080"
  JWT_EXPIRATION: "86400000"
---
apiVersion: v1
kind: Secret
metadata:
  name: backend-secret
type: Opaque
stringData:                 # stringData: lo escribes en claro y K8s lo codifica por ti
  DB_USER: ayudasgv
  DB_PASSWORD: ayudasgv
  JWT_SECRET: cambia-esto-en-produccion-clave-secreta-larga
```

### Paso 4.3 — PostgreSQL (Deployment + PVC + Service)

La BD necesita **persistencia** (PVC) y un **Service ClusterIP** llamado `postgres` para que el backend la
encuentre por DNS. Una sola réplica (aprender; en prod sería StatefulSet).

Puntos didácticos del manifiesto (ver Apéndice B):
- El **PVC** pide 1Gi; el Pod lo monta en `/var/lib/postgresql/data`. Si el Pod muere y se recrea, los
  datos siguen ahí.
- El **Service** se llama `postgres` → ese es el hostname que resuelve el backend.
- Las credenciales salen del mismo Secret que usa el backend (coherencia).

```bash
kubectl apply -f k8s/20-postgres.yaml
kubectl get pods -w            # espera a que postgres pase a Running
```

### Paso 4.4 — Backend (Deployment + Service)

El Deployment referencia la imagen `ayudas-gv-backend:1.0` que construiste en minikube, inyecta la config
con `envFrom` (ConfigMap + Secret) y declara **probes** al puerto 8080. El Service `backend` (ClusterIP)
lo expone dentro del clúster en el puerto 8080.

Detalles clave:
- `imagePullPolicy: IfNotPresent` → usa la imagen local, no la busca en internet.
- `readinessProbe` TCP a 8080 con `initialDelaySeconds` generoso: Spring Boot + Flyway tardan en arrancar.
- `envFrom` combina `configMapRef: backend-config` y `secretRef: backend-secret` en una sola inyección.

```bash
kubectl apply -f k8s/30-backend.yaml
kubectl rollout status deployment/backend
kubectl logs -l app=backend --tail=50      # ver el arranque de Spring Boot
```

### Paso 4.5 — Frontend (Deployment + Service)

La imagen `ayudas-gv-frontend:1.0` sirve la SPA con Nginx en el puerto 80. Su `nginx.conf` proxea `/api` al
backend; dentro del clúster el host `backend` resuelve al Service del paso anterior (si en `nginx.conf` el
proxy apunta a `backend:8080`, funciona sin tocar nada — compruébalo).

```bash
kubectl apply -f k8s/40-frontend.yaml
kubectl get pods
```

### Paso 4.6 — Ingress (entrada HTTP única)

En vez de exponer cada Service, un Ingress enruta por path. Habilita el controller y aplica la regla:

```bash
minikube addons enable ingress
kubectl apply -f k8s/50-ingress.yaml
```

Regla: `/` → Service `frontend:80`, `/api` → Service `backend:8080`. Para probarlo en local:

```bash
minikube tunnel                      # (en otra terminal; enруta la IP del Ingress)
kubectl get ingress                  # anota la ADDRESS/host
# o, sin ingress, acceso rápido por túnel de servicio:
minikube service frontend -n ayudas-dev --url
```

### Ejercicios Parte 4

- [ ] **4.1** Aplica los manifiestos en orden y llega a tener los 3 Pods `Running`.
  `kubectl get pods -o wide` — observa la IP de cada Pod.
- [ ] **4.2** Demuestra el DNS interno: entra a un Pod y resuelve la BD por nombre.
  ```bash
  kubectl exec -it deploy/backend -- sh -c "getent hosts postgres || nslookup postgres"
  ```
- [ ] **4.3** Abre la app por el túnel (`minikube service frontend --url`) y haz login con `admin/admin123`.
  Confirma que el frontend habla con el backend y el backend con la BD (cadena completa dentro del clúster).
- [ ] **4.4** Provoca y diagnostica un fallo: pon mal la `DB_PASSWORD` en el Secret, `kubectl apply`,
  reinicia el backend (`kubectl rollout restart deploy/backend`) y usa `kubectl describe pod` +
  `kubectl logs` para encontrar el error. Luego arréglalo. (Aprender a **leer** el clúster vale más que el
  happy path).

---

<a name="parte-5"></a>
## Parte 5 — Operar: escalar, autocurar, actualizar sin caída

Aquí está la esencia de "orquestar". Todo sobre el backend, que es *stateless* (se puede replicar sin más).

### Escalar

```bash
kubectl scale deployment/backend --replicas=3
kubectl get pods -l app=backend        # ahora hay 3 Pods; el Service reparte entre ellos
```

El Service `backend` balancea automáticamente entre las 3 réplicas. No has tocado el frontend ni el DNS:
sigue hablando a `backend:8080` y K8s reparte.

### Self-healing (el ejercicio estrella)

```bash
# Mata un Pod a mano
kubectl delete pod <nombre-de-un-pod-backend>
# Observa cómo el Deployment crea otro para volver a 3, en segundos
kubectl get pods -l app=backend -w
```

Nunca bajas de las réplicas deseadas: eso es *desired state* en acción. Compáralo mentalmente con Compose,
donde matar el contenedor lo deja caído.

### Rolling update (actualizar sin downtime)

```bash
# 1) Cambia algo del backend y reconstruye con nuevo tag
docker build -t ayudas-gv-backend:1.1 ./backend      # (con el docker-env de minikube activo)
# 2) Apunta el Deployment a la nueva imagen
kubectl set image deployment/backend backend=ayudas-gv-backend:1.1
# 3) Observa la sustitución progresiva de Pods
kubectl rollout status deployment/backend
```

Kubernetes levanta Pods `:1.1`, espera a que pasen la readinessProbe, y solo entonces retira los `:1.0`.
Si algo va mal:

```bash
kubectl rollout undo deployment/backend      # rollback a la versión anterior
kubectl rollout history deployment/backend
```

### Ejercicios Parte 5

- [ ] **5.1** Escala el backend a 3 réplicas. Con `kubectl get endpoints backend` comprueba que el Service
  lista 3 IPs.
- [ ] **5.2** Mata un Pod del backend y cronometra cuánto tarda el Deployment en recrearlo. Explica por qué
  el frontend no se enteró.
- [ ] **5.3** Haz un rolling update a `:1.1` (cambia, por ejemplo, un literal de log) y verifica con
  `kubectl rollout status` que no hubo caída. Luego haz `rollout undo` y confirma que vuelve a `:1.0`.
- [ ] **5.4** (Reto) Escala postgres a 2 réplicas. ¿Qué pasa? Razona por qué una BD con un PVC
  `ReadWriteOnce` **no** se replica como el backend, y por eso las BBDD usan StatefulSet, no Deployment.

---

<a name="parte-6"></a>
## Parte 6 — Ejercicios integradores (los que te blindan para la entrevista)

Cada uno combina varios conceptos. Escribe una respuesta corta además de ejecutarlo.

- [ ] **6.1 — Entornos con Namespaces.** Crea un segundo namespace `ayudas-staging` y despliega el mismo
  stack ahí, cambiando solo el ConfigMap (por ejemplo, `JWT_EXPIRATION` distinto). Demuestra que
  `postgres` en `ayudas-dev` y `postgres` en `ayudas-staging` son bases de datos independientes.
- [ ] **6.2 — Probes de verdad.** Añade `spring-boot-starter-actuator` al backend y cambia las probes TCP
  por `httpGet` a `/api/actuator/health/liveness` y `/readiness`. Explica la diferencia entre liveness y
  readiness con lo que observas al arrancar.
- [ ] **6.3 — De Compose a K8s tú solo.** Sin mirar el Apéndice B, coge el `docker-compose.yml` y escribe
  de memoria el Deployment+Service del backend. Luego compáralo. Anota qué se te olvidó (suele ser el
  `selector`/`labels` o el `imagePullPolicy`).
- [ ] **6.4 — Resiliencia.** Con el backend a 3 réplicas y la app abierta, borra un Pod mientras navegas.
  ¿Se cae la sesión? ¿Por qué el JWT lo hace *stateless* y ayuda aquí? (conecta con la decisión de
  arquitectura del proyecto: no hay estado de sesión en el servidor).
- [ ] **6.5 — Límites de recursos.** Añade `resources.requests`/`limits` (CPU/memoria) al backend y explica
  qué pasa si un Pod supera su `limit` de memoria (OOMKilled) frente a superar el de CPU (throttling).
- [ ] **6.6 — Preguntas orales.** Prepara respuesta de 30s a: *Pod vs Deployment vs Service*; *¿por qué
  no despliego Pods sueltos?*; *¿ClusterIP vs NodePort vs LoadBalancer?*; *¿qué es un rolling update y cómo
  hago rollback?*; *¿ConfigMap vs Secret?*; *¿por qué una BD no se escala como un backend stateless?*.

---

<a name="parte-7"></a>
## Parte 7 — Puente al futuro: microservicios del proyecto en K8s

Esto conecta el aprendizaje de K8s con el rumbo real del proyecto (spec.md §2, invariante 6). El enunciado
prevé extraer del monolito: `gateway`, `ms-autenticacion`, `ms-expedientes`, `ms-tramitacion`. En
Kubernetes, **cada microservicio es un Deployment + un Service**, y el `gateway` es quien recibe el tráfico
del Ingress y lo reparte:

```
Ingress ──▶ Service gateway ──▶ (enruta por path) ──▶ Service ms-autenticacion
                                                  ├─▶ Service ms-expedientes
                                                  └─▶ Service ms-tramitacion
                                                          │
                                                   Service postgres (o una BD por servicio)
```

Cuando llegues a la Fase 1 y empieces a extraer `ms-tramitacion`, ya sabrás darle su Deployment/Service y
enchufarlo al gateway sin tocar los demás. **Ese es el pago del aprendizaje de esta guía**: la app está
diseñada con fronteras limpias justamente para esto.

No hay que implementarlo ahora (es Fase 1+), pero deja el modelo mental hecho:
- [ ] **7.1** Dibuja (en papel o en un `.md`) el diagrama de los 4 servicios como Deployments+Services en
  el namespace `ayudas-dev`, indicando qué Service expone el Ingress.

---

<a name="apendice-a"></a>
## Apéndice A — Chuleta de comandos

```bash
# --- Docker ---
docker build -t img:tag .            docker compose up -d / down [-v]
docker ps                            docker compose logs -f <svc>
docker exec -it <c> sh               docker images

# --- minikube ---
minikube start --driver=docker       minikube dashboard
minikube addons enable ingress       minikube service <svc> -n <ns> --url
minikube tunnel                      minikube image load img:tag
eval $(minikube docker-env)          # (bash) construir dentro del clúster

# --- kubectl: ver ---
kubectl get pods|svc|deploy|ingress|endpoints|namespaces [-n <ns>] [-o wide]
kubectl describe pod <pod>           kubectl logs [-f] <pod> | -l app=backend
kubectl get pods -w                  kubectl get all -n <ns>

# --- kubectl: aplicar / operar ---
kubectl apply -f <archivo|carpeta>   kubectl delete -f <archivo>
kubectl scale deploy/<n> --replicas=3
kubectl rollout status|undo|history|restart deploy/<n>
kubectl set image deploy/<n> <cont>=img:tag
kubectl exec -it deploy/<n> -- sh    kubectl config set-context --current --namespace=<ns>
```

---

<a name="apendice-b"></a>
## Apéndice B — Manifiestos completos de referencia

> Créalos en `k8s/`. Están pensados para minikube con imágenes construidas dentro del clúster
> (`imagePullPolicy: IfNotPresent`). No los apliques a ciegas: entiende cada bloque (para eso está la guía).

### `k8s/00-namespace.yaml`
```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: ayudas-dev
```

### `k8s/10-backend-config.yaml`
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: backend-config
  namespace: ayudas-dev
data:
  DB_URL: jdbc:postgresql://postgres:5432/ayudas_gv
  SERVER_PORT: "8080"
  JWT_EXPIRATION: "86400000"
---
apiVersion: v1
kind: Secret
metadata:
  name: backend-secret
  namespace: ayudas-dev
type: Opaque
stringData:
  DB_USER: ayudasgv
  DB_PASSWORD: ayudasgv
  JWT_SECRET: cambia-esto-en-produccion-clave-secreta-larga
```

### `k8s/20-postgres.yaml`
```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: postgres-pvc
  namespace: ayudas-dev
spec:
  accessModes: ["ReadWriteOnce"]
  resources:
    requests:
      storage: 1Gi
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: postgres
  namespace: ayudas-dev
spec:
  replicas: 1
  selector:
    matchLabels:
      app: postgres
  template:
    metadata:
      labels:
        app: postgres
    spec:
      containers:
        - name: postgres
          image: postgres:15-alpine
          ports:
            - containerPort: 5432
          env:
            - name: POSTGRES_DB
              value: ayudas_gv
            - name: POSTGRES_USER
              valueFrom:
                secretKeyRef: { name: backend-secret, key: DB_USER }
            - name: POSTGRES_PASSWORD
              valueFrom:
                secretKeyRef: { name: backend-secret, key: DB_PASSWORD }
          volumeMounts:
            - name: data
              mountPath: /var/lib/postgresql/data
          readinessProbe:
            exec:
              command: ["pg_isready", "-U", "ayudasgv", "-d", "ayudas_gv"]
            initialDelaySeconds: 5
            periodSeconds: 10
      volumes:
        - name: data
          persistentVolumeClaim:
            claimName: postgres-pvc
---
apiVersion: v1
kind: Service
metadata:
  name: postgres
  namespace: ayudas-dev
spec:
  selector:
    app: postgres
  ports:
    - port: 5432
      targetPort: 5432
  # sin 'type' → ClusterIP (solo interno). El nombre 'postgres' es el hostname DNS.
```

### `k8s/30-backend.yaml`
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: backend
  namespace: ayudas-dev
spec:
  replicas: 1
  selector:
    matchLabels:
      app: backend
  template:
    metadata:
      labels:
        app: backend
    spec:
      containers:
        - name: backend
          image: ayudas-gv-backend:1.0
          imagePullPolicy: IfNotPresent      # usa la imagen local de minikube
          ports:
            - containerPort: 8080
          envFrom:
            - configMapRef: { name: backend-config }
            - secretRef: { name: backend-secret }
          readinessProbe:
            tcpSocket: { port: 8080 }
            initialDelaySeconds: 30           # Spring Boot + Flyway tardan en arrancar
            periodSeconds: 10
          livenessProbe:
            tcpSocket: { port: 8080 }
            initialDelaySeconds: 60
            periodSeconds: 15
          resources:
            requests: { cpu: "250m", memory: "384Mi" }
            limits:   { cpu: "1",    memory: "768Mi" }
---
apiVersion: v1
kind: Service
metadata:
  name: backend
  namespace: ayudas-dev
spec:
  selector:
    app: backend
  ports:
    - port: 8080
      targetPort: 8080
  # ClusterIP: el frontend/gateway le habla por 'backend:8080'
```

### `k8s/40-frontend.yaml`
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: frontend
  namespace: ayudas-dev
spec:
  replicas: 1
  selector:
    matchLabels:
      app: frontend
  template:
    metadata:
      labels:
        app: frontend
    spec:
      containers:
        - name: frontend
          image: ayudas-gv-frontend:1.0
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 80
          readinessProbe:
            httpGet: { path: /, port: 80 }
            initialDelaySeconds: 5
            periodSeconds: 10
---
apiVersion: v1
kind: Service
metadata:
  name: frontend
  namespace: ayudas-dev
spec:
  selector:
    app: frontend
  ports:
    - port: 80
      targetPort: 80
```

### `k8s/50-ingress.yaml`
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: ayudas-ingress
  namespace: ayudas-dev
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /$1
spec:
  rules:
    - http:
        paths:
          - path: /api(/|$)(.*)
            pathType: ImplementationSpecific
            backend:
              service:
                name: backend
                port: { number: 8080 }
          - path: /()(.*)
            pathType: ImplementationSpecific
            backend:
              service:
                name: frontend
                port: { number: 80 }
```

> Nota sobre el Ingress: el `rewrite-target` es sensible; si el frontend ya proxea `/api` a `backend:8080`
> vía su `nginx.conf`, para empezar puede bastar con exponer **solo** el frontend por Ingress y dejar que
> él reenvíe a `/api`. Prueba las dos variantes: entender por qué una funciona y la otra no es parte del
> aprendizaje.

---

### Cierre

Cuando tengas marcados los ejercicios de las Partes 3–6, sabrás: construir imágenes para un clúster,
declarar el estado deseado, exponer servicios por DNS interno, dar entrada por Ingress, separar config de
secretos, y las tres operaciones que definen "orquestar": **escalar, autocurar y actualizar sin caída**.
Eso es exactamente lo que te van a preguntar y lo que vas a hacer en el nuevo proyecto.
