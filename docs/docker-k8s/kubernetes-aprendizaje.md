# Docker + Kubernetes paso a paso — Guía de aprendizaje sobre Ayudas GV

> Guía didáctica para pasar de "sé usar `docker compose`" a "sé desplegar y operar esta app en un
> clúster de Kubernetes". Está construida **sobre este proyecto real** (postgres + backend Spring Boot +
> frontend React/Nginx), no sobre ejemplos genéricos, para que cada concepto tenga un correlato tangible.
>
> Cómo usarla: sigue los bloques en orden. Cada bloque tiene **(1) teoría mínima**, **(2) manos a la obra**
> y **(3) ejercicios** con su verificación. No copies y pegues sin leer: el objetivo es interiorizar, no
> tener YAML que funcione. El checklist de progreso vive en [plan.md](../../plan.md) (fase "K8s").

**Cómo leer las analogías.** Cada concepto técnico lleva debajo un recuadro `> **Imagínalo así.**` con una
comparación de andar por casa. No son adornos: están pensadas para que puedas **explicar el concepto en voz
alta** sin recitar la definición. Si en una entrevista te bloqueas con el término, tira de la imagen y
reconstruye la idea desde ahí. La palabra técnica (Pod, Service, Namespace) se mantiene siempre — lo que
cambia es cómo se explica.

Si vienes de cero y quieres el recorrido contado como una historia antes de entrar aquí, empieza por
[kubernetes-desde-0.md](kubernetes-desde-0.md).

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
(salvo `restart: unless-stopped`, que es un parche local). Kubernetes cambia la forma de pedir las cosas:
en vez de dar **órdenes** ("arranca esto, para aquello"), tú describes **cómo quieres que estén las cosas**
("quiero 3 copias del backend, siempre") y el clúster se pasa el día comparando la realidad con esa
descripción y corrigiéndola. A eso se le llama **estado deseado** (*desired state*), y de ahí salen las
otras dos palabras de moda: *self-healing* (se recupera solo) y *rolling updates* (cambia de versión sin
cortar el servicio).

> **Imagínalo así.** Un **mando de la tele** frente a un **termostato**. Con el mando das órdenes sueltas:
> sube, baja, apaga. Si la tele se apaga sola, ahí se queda hasta que tú hagas algo — eso es Docker Compose.
> El termostato funciona al revés: le dices "21 grados" y ya está. No le mandas encender ni apagar; él mide
> la temperatura una y otra vez y hace lo que haga falta para que sean 21, aunque abras una ventana. Eso es
> Kubernetes: tú fijas el número, él se encarga del resto **para siempre**.

> **Y la versión más doméstica todavía:** la **cisterna del váter**. Tiras de la cadena y el depósito se
> vacía; nadie corre a rellenarlo, el flotador lo hace solo hasta la marca. Ese flotador — mirar el nivel,
> compararlo con la marca, abrir el agua si falta — es literalmente lo que Kubernetes hace con tus copias
> de la aplicación, y tiene nombre técnico: **bucle de reconciliación**.

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

- **Imagen** — la **receta** de tu aplicación, escrita en un `Dockerfile`: el sistema base, las librerías y
  tu código, todo junto. Se construye una vez y ya **no cambia nunca** (por eso se dice *inmutable*). En este
  proyecto hay dos: `backend/Dockerfile` y `frontend/Dockerfile`.

  > **Imagínalo así.** La imagen es la **receta del bizcocho**, no el bizcocho. Está escrita en un papel y
  > sirve para hacer uno o cien, todos idénticos. Y como cualquier receta: si quieres cambiar algo, no tachas
  > el papel a medias — escribes una versión nueva.

- **Contenedor** — la receta ya **ejecutándose**: el bizcocho hecho. Es **efímero**: si lo borras, se pierde
  todo lo que no hayas guardado aparte. Por eso la base de datos necesita un volumen y el backend no.

  > **Imagínalo así.** De una misma receta salen cinco bizcochos y **cada uno es independiente**: puedes
  > tirar uno a la basura y los otros cuatro siguen en la mesa. Lo que le hagas a uno no afecta a los demás.

- **Volumen** — un sitio para guardar datos **fuera** del contenedor, que sobrevive aunque el contenedor
  desaparezca. Imprescindible para PostgreSQL (en Compose: `postgres_data:/var/lib/postgresql/data`).

  > **Imagínalo así.** El **táper de la nevera**. Puedes tirar la sartén, cambiarla por otra o quemarla; lo
  > que metiste en el táper sigue ahí. Sin táper, cada vez que cambias de sartén te quedas sin cena.

- **Red por nombre** — los contenedores de un mismo Compose se localizan **por el nombre del servicio**, no
  por `localhost` ni por una IP. Mira el `docker-compose.yml`: el backend conecta a
  `jdbc:postgresql://postgres:5432/...` — usa `postgres`, el nombre del servicio. **En Kubernetes funciona
  exactamente igual** (te encuentran por el nombre del Service). Entenderlo aquí te ahorra el error más
  común al dar el salto.

  > **Imagínalo así.** En la agenda del móvil no te aprendes el número de tu madre: pulsas **"Mamá"**. Si
  > mañana cambia de compañía y de número, tú sigues pulsando "Mamá" y la llamada llega igual. Eso es un
  > nombre de servicio: el número (la IP) puede cambiar cuando quiera; el nombre, no.

- **Docker Compose** — un único fichero YAML que describe varios servicios a la vez, para no arrancarlos uno
  por uno a mano. Los entornos se separan con `docker-compose.override.yml` o `--env-file`.

  > **Imagínalo así.** La **lista de la compra**. En vez de acordarte de cada producto según paseas por el
  > súper, lo llevas todo escrito y en orden.

- **Multi-stage build** — construyes en una imagen grande, con todas las herramientas (Maven, Node), y luego
  copias **solo el resultado** a una imagen final pequeña. Los dos Dockerfiles del proyecto ya lo hacen
  (Maven→JRE, Node→Nginx).

  > **Imagínalo así.** Montas el mueble en el **garaje**, con el taladro, el destornillador, las cajas y el
  > serrín por el suelo. Al salón subes **solo el mueble montado**: ni las herramientas, ni el embalaje. La
  > imagen final pesa mucho menos, arranca antes y — detalle que gusta en las entrevistas — **si no llevas
  > herramientas dentro, un atacante tampoco las encuentra**.

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

Kubernetes funciona por descripciones: escribes en un fichero YAML **cómo quieres que sea cada pieza** y con
`kubectl apply` se la entregas al clúster, que se encarga de construirla y mantenerla así. Estas son las
piezas que necesitas para esta app, de la más pequeña a la más grande.

### Pod
La pieza **más pequeña** que Kubernetes sabe manejar. Es una envoltura alrededor de **uno (o unos pocos)
contenedores** que van siempre juntos: comparten la misma dirección de red y el mismo almacenamiento.
Kubernetes nunca mueve contenedores sueltos; mueve Pods.

Regla de oro: **nunca crees Pods a mano** para algo serio. Nacen y mueren constantemente, y un Pod que muere
**no resucita** — es alguien de más arriba (el Deployment) quien crea uno nuevo en su lugar.

> **Imagínalo así.** Un Pod es un **camarote de tren compartido**. Dentro pueden viajar dos pasajeros (dos
> contenedores), y mientras dure el viaje comparten **el mismo número de camarote** (la dirección de red) y
> **el mismo armario** (el almacenamiento). Si uno guarda algo en ese armario, el otro lo alcanza sin
> pedírselo a nadie — igual que si llenas un vaso y le das un sorbo tu compañero de mesa: es el mismo líquido.
> Lo que no puede pasar es que los pasajeros de camarotes distintos rebusquen en el armario ajeno.

> **Y lo de "no les cojas cariño":** las **bombillas de casa**. Cuando se funde una no le pones nombre ni la
> intentas revivir: la tiras y pones otra igual. En la jerga se dice que los Pods son *ganado, no mascotas*.
> Las cosas a las que sí les pones nombre y cuidas son otras (los datos, por ejemplo).

### Deployment
El que dice *"quiero **N copias** de este Pod, con esta imagen, siempre"*. Es el termostato de la Parte 0, y
hace tres cosas por ti:

- **Se recupera solo** (*self-healing*): si un Pod muere, crea otro para volver a N.
- **Actualiza sin cortar** (*rolling update*): al cambiar la imagen, va sustituyendo Pods poco a poco.
- **Da marcha atrás** (*rollback*): puede volver a la versión anterior en segundos.

Es lo que usarás para el backend y el frontend. Para la base de datos lo normal es un **StatefulSet**
(identidad fija y disco propio por copia), pero con **una sola** réplica de postgres, un Deployment + PVC es
suficiente y mucho más simple de entender.

> **Imagínalo así.** El **dispensador de servilletas** del bar: siempre hay servilletas asomando. Nadie
> vigila el dispensador; el propio mecanismo empuja una nueva cuando coges la anterior. Tú no pides
> "pon una servilleta", pides "que nunca falten".

> **La pieza oculta: el ReplicaSet.** Por debajo, cada Deployment tiene un **cortapastas de galletas**, que
> es el molde exacto con el que salen todos los Pods. Si cambias la imagen, cambias de molde: se estrena un
> cortapastas nuevo y el viejo **no se tira, se guarda en el cajón**. Por eso puedes volver atrás cuando
> quieras — el molde de antes sigue ahí, listo para usarse.

### Service
Un **nombre y una dirección fijos** delante de un grupo de Pods que van cambiando. Hace falta porque los
Pods estrenan dirección cada vez que nacen, y nadie puede perseguir esa dirección. El Service sabe a quién
tiene detrás gracias a las **etiquetas** (*labels*) de los Pods.

Hay tres tipos, y la diferencia es simplemente **quién puede llamar**:

- **ClusterIP** (el de por defecto): solo se puede llamar **desde dentro** del clúster. Es el que usas para
  postgres y para que el frontend hable con el backend.
- **NodePort**: abre además un puerto hacia fuera en la máquina. Práctico para pruebas rápidas.
- **LoadBalancer**: pide un balanceador de verdad al proveedor de nube. En minikube se simula.

El nombre del Service **es** la dirección: el backend llama a la base de datos escribiendo `postgres:5432`,
igual que hacía en Compose.

> **Imagínalo así.** El **teléfono de la pizzería**. Marcas siempre el mismo número y te atiende quien esté
> libre en ese momento. La pizzería puede contratar gente, despedirla o cambiar todo el turno: **el número no
> cambia nunca**, y a ti te da igual quién descuelgue. Eso es un Service: un número fijo y una lista, por
> detrás, de quién está disponible para atender.

> **Y los tres tipos, en el mismo edificio:** el **telefonillo interno** solo funciona entre vecinos
> (ClusterIP). Abrir una **ventana a la calle** deja que te griten desde fuera (NodePort). Contratar a un
> **recepcionista en la puerta** es lo caro y lo serio, y solo existe si el edificio lo ofrece (LoadBalancer).

### Ingress
La **puerta de entrada HTTP única** al clúster, que reparte según la dirección pedida. En vez de abrir un
acceso por cada Service, escribes reglas: `/` → frontend, `/api` → backend. Necesita que haya un *Ingress
Controller* instalado, que es quien hace el trabajo de verdad (en minikube: `minikube addons enable ingress`).

> **Imagínalo así.** El **conserje del edificio**. Hay una sola puerta a la calle y él pregunta a dónde vas:
> "¿A la peluquería? Tercero. ¿Al dentista? Quinto". Sin conserje tendrías que abrir una puerta a la calle
> por cada negocio del edificio, que es justo lo que quieres evitar. Y ojo al matiz: el Ingress es **la lista
> de instrucciones** que le das al conserje; el conserje (el *controller*) hay que contratarlo aparte.

### ConfigMap / Secret
Los dos sitios donde vive la configuración, separada del código. Es el equivalente al bloque `environment:`
de Compose, partido en dos según el peligro:

- **ConfigMap**: ajustes **que no pasa nada si alguien lee** — la dirección de la base de datos, el puerto,
  cuánto dura un token.
- **Secret**: **credenciales** — usuario y contraseña de la BD, `JWT_SECRET`. Se guardan en base64, que
  **no es cifrado**: es solo texto disfrazado. En producción lo que de verdad los protege es limitar quién
  puede leerlos (RBAC) o usar un gestor externo.

> **Imagínalo así.** El ConfigMap es la **nota pegada en la nevera** con el horario del gimnasio: está a la
> vista y no importa. El Secret es el **PIN de la tarjeta** — lo guardas en un cajón, no en la nevera.
> Y el detalle de base64 merece que lo digas tal cual en una entrevista: es como escribir el PIN **del
> revés**. Cualquiera que abra el cajón lo lee igual. Lo que protege el PIN no es el disfraz, es **quién
> tiene llave del cajón**.

### Namespace
Una división **lógica** dentro del mismo clúster, para que convivan varios entornos sin pisarse:
`ayudas-dev`, `ayudas-staging`, `ayudas-prod`. Los objetos de uno no chocan con los del otro, y los nombres
se resuelven **dentro de tu propio namespace**: si desde `ayudas-dev` pides `postgres`, te dan el postgres de
`ayudas-dev`, no el de staging.

> **Imagínalo así.** Los **pisos de un mismo edificio**. En el 2.ºA vive un Juan y en el 3.ºA vive otro Juan,
> y nadie se confunde. Cuando en el 2.ºA dicen "avisa a Juan", se refieren al suyo, sin necesidad de apellido.
> Solo si quieres llamar al Juan del tercero tienes que decirlo entero — y en Kubernetes también existe ese
> "nombre completo": `postgres.ayudas-staging`.

### PersistentVolume (PV) / PersistentVolumeClaim (PVC)
El volumen de Docker, en versión Kubernetes, y viene en dos piezas que confunden hasta que ves la diferencia:
el **PVC** es la **petición** ("necesito 1 Gi de disco") y el **PV** es **el disco concreto** que el clúster
te acaba asignando. Postgres monta un PVC para que sus datos sobrevivan cuando su Pod se recree.

> **Imagínalo así.** Alquilar un **trastero**. El PVC es el formulario que rellenas: "quiero 5 metros
> cuadrados". El PV es el trastero real que te asignan, con su número y su llave. Tú nunca eliges cuál te
> toca — pides las medidas y te lo dan. Y lo importante: **puedes cambiarte de casa (el Pod muere y nace
> otro) y el trastero sigue siendo tuyo, con todo dentro**.

### Probes (liveness / readiness) — la clave del self-healing
Kubernetes necesita saber dos cosas distintas sobre cada contenedor, y las pregunta por separado:

- **livenessProbe** — *"¿sigues vivo?"*. Si falla varias veces seguidas, K8s **reinicia** el contenedor.
- **readinessProbe** — *"¿puedes atender ya?"*. Si falla, el Service **deja de mandarle trabajo**, pero
  **no** lo reinicia: le da tiempo.

Distinguirlas importa mucho aquí, porque el backend Spring Boot tarda en arrancar (la JVM más las migraciones
de Flyway). Sin readiness, el Service le mandaría peticiones nada más nacer y los usuarios verían errores.
Como el proyecto todavía no expone `/actuator/health`, empezamos con probes TCP al puerto 8080 y queda como
ejercicio pasarlas a HTTP "de verdad".

> **Imagínalo así.** Un compañero acaba de llegar a la oficina: aún con el abrigo puesto y el ordenador
> arrancando. **¿Está vivo?** Sí, evidentemente — no llames a la ambulancia. **¿Está listo para que le pases
> llamadas?** No, dale cinco minutos. Son dos preguntas distintas y confundirlas se paga: si tratas
> "todavía no está listo" como "está muerto", **reinicias a alguien que solo necesitaba tiempo** — y como
> tarda lo mismo en arrancar la siguiente vez, entra en un bucle de reinicios infinito. Ese error tiene
> nombre propio en producción: `CrashLoopBackOff` por una liveness demasiado impaciente.

---

<a name="parte-3"></a>
## Parte 3 — Montar el clúster local (minikube)

`minikube` levanta un clúster de Kubernetes **de una sola máquina** dentro de tu ordenador (en un contenedor
o una VM). Es el entorno ideal para aprender sin gastar un euro.

> **Imagínalo así.** Un **piso piloto**. Tiene cocina, baño y salón de verdad, funciona todo, y sirve
> perfectamente para aprender a moverte por la casa — pero está montado dentro de una nave. Casi todo lo que
> practiques aquí vale igual en un clúster real; lo que no se puede ensayar en el piso piloto es **lo que
> pasa entre varias casas**, porque solo hay una. Guárdate ese matiz: varias cosas que en minikube "funcionan"
> es simplemente porque **todo cae en la misma máquina**.

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

Este es **el punto que más confunde** al empezar. Tu imagen `ayudas-gv-backend` la ha construido el Docker de
tu Windows, pero el clúster vive en otra máquina distinta (la de minikube), **con su propio Docker**. Y no
comparten nada.

> **Imagínalo así.** Has hecho una tarta y la has metido en **tu nevera**. Tu vecino no la tiene en la suya,
> por mucho que viváis en el mismo edificio. Solo hay dos salidas: **llevársela** (`minikube image load`) o
> **ir a cocinarla a su casa** (construir dentro del Docker de minikube). Si no haces ninguna de las dos y le
> pides al clúster que sirva la tarta, se queda mirando la nevera vacía una y otra vez — y eso, en pantalla,
> se llama `ImagePullBackOff`.

Tres opciones (usaremos la 1):

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

Crea una carpeta `k8s/` en la raíz del repo. Iremos añadiendo ficheros y aplicándolos **en el orden en que
se necesitan unos a otros**: namespace → config y secretos → postgres → backend → frontend → ingress. Los
ficheros completos están en el [Apéndice B](#apendice-b); aquí va el hilo y el porqué de cada uno.

> **Imagínalo así.** Es **mudarse a un piso**. Primero la llave y el piso vacío (namespace), luego das de alta
> la luz y el agua (config y secretos), después metes la nevera con la comida (la base de datos, que es lo que
> guarda cosas), luego la cocina (el backend), después el comedor donde se sirve (el frontend) y al final
> pones el timbre y el buzón para que te encuentren desde la calle (el ingress). Hacerlo al revés no funciona:
> no puedes enchufar la nevera antes de dar de alta la luz.

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
- Probes `httpGet` contra Actuator (Bloque 4, ejercicio 6.2): `startupProbe` da margen al arranque de
  Spring Boot + Flyway, `readinessProbe` incluye la BD y `livenessProbe` solo el proceso. Antes eran TCP,
  que solo comprobaban "el puerto está abierto" — y Tomcat abre el puerto antes de estar listo.
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

El Service `backend` reparte solo entre las 3 copias. No has tocado el frontend ni la configuración de red:
sigue llamando a `backend:8080` y Kubernetes decide a quién le toca cada petición.

> **Imagínalo así.** Abres **dos cajas más** en el supermercado. Nadie avisa a los clientes ni cambia la
> cartelería: la cola simplemente empieza a repartirse. Y como en el súper de verdad, **el reparto no sale
> exacto** — puede que una caja atienda a tres clientes y otra a uno. Kubernetes elige por cada conexión, no
> va turnándose con precisión de reloj.

### Self-healing (el ejercicio estrella)

```bash
# Mata un Pod a mano
kubectl delete pod <nombre-de-un-pod-backend>
# Observa cómo el Deployment crea otro para volver a 3, en segundos
kubectl get pods -l app=backend -w
```

Nunca te quedas por debajo de las copias que pediste: eso es el **estado deseado** funcionando delante de tus
ojos. Compáralo con Compose, donde matar el contenedor lo deja muerto y ahí se queda.

> **Imagínalo así.** Vuelve la **cisterna del váter** de la Parte 0. Tiras de la cadena (borras el Pod) y el
> depósito se vacía, pero el flotador ya está abriendo el agua antes de que te dé tiempo a mirar. Tú no has
> pedido que se rellene; **es que la marca del depósito no ha cambiado**.

> **Un matiz que distingue a quien lo ha practicado.** Hay **dos** formas de que un Pod "se recupere" y no
> son la misma: si el programa de dentro casca, el vigilante de la máquina lo **reinicia en el sitio** (mismo
> Pod, mismo nombre, y sube el contador `RESTARTS`) — como rearmar el diferencial de la luz. Si el Pod entero
> desaparece, el Deployment **crea uno nuevo** con otro nombre y otra dirección — como poner una bombilla
> nueva. Míralo en la columna `RESTARTS` de `kubectl get pods` y sabrás cuál de las dos ocurrió.

### Rolling update (actualizar sin downtime)

```bash
# 1) Cambia algo del backend y reconstruye con nuevo tag
docker build -t ayudas-gv-backend:1.1 ./backend      # (con el docker-env de minikube activo)
# 2) Apunta el Deployment a la nueva imagen
kubectl set image deployment/backend backend=ayudas-gv-backend:1.1
# 3) Observa la sustitución progresiva de Pods
kubectl rollout status deployment/backend
```

Kubernetes levanta Pods `:1.1`, **espera a que digan que están listos** (la readinessProbe) y solo entonces
retira los `:1.0`. Uno por uno, sin que ningún usuario se entere.

> **Imagínalo así.** El **relevo de turno en una tienda**. El cajero nuevo llega, se pone el uniforme, cuenta
> su caja y **solo cuando está listo** el anterior cierra la suya y se va a casa. En ningún momento hay menos
> mostradores abiertos de los necesarios. Lo contrario — que el primero se marche y luego llegue el
> siguiente — dejaría a la gente esperando, y es exactamente lo que estos ajustes evitan: `maxSurge` es
> "cuántos de más puedo tener a la vez" y `maxUnavailable` "cuántos de menos me puedo permitir".

Si algo va mal:

```bash
kubectl rollout undo deployment/backend      # vuelve a la versión anterior
kubectl rollout history deployment/backend
```

> **Imagínalo así.** Aquí se entiende el **cortapastas guardado en el cajón**: volver atrás no reconstruye
> nada ni recompila la versión vieja — **saca el molde de antes y vuelve a usarlo**. Por eso un rollback
> tarda segundos y es de lo más fiable que tiene Kubernetes.

> **Detalle contraintuitivo:** deshacer **no borra** el paso en falso del historial, **añade uno nuevo**.
> Como en la contabilidad de una tienda: un error no se tacha, se corrige con un apunte nuevo que deja
> constancia. Por eso siempre puedes deshacer el deshacer.

### Ejercicios Parte 5

- [ ] **5.1** Escala el backend a 3 réplicas. Con `kubectl get endpoints backend` comprueba que el Service
  lista 3 IPs.
- [ ] **5.2** Mata un Pod del backend y cronometra cuánto tarda el Deployment en recrearlo. Explica por qué
  el frontend no se enteró.
- [ ] **5.3** Haz un rolling update a `:1.1` (cambia, por ejemplo, un literal de log) y verifica con
  `kubectl rollout status` que no hubo caída. Luego haz `rollout undo` y confirma que vuelve a `:1.0`.
- [ ] **5.4** (Reto) Escala postgres a 2 réplicas. ¿Qué pasa? Razona por qué una BD con un PVC
  `ReadWriteOnce` **no** se replica como el backend, y por eso las BBDD usan StatefulSet, no Deployment.

  > **Aviso, porque este ejercicio engaña** (comprobado en vivo el 27/07/2026): lo intuitivo es esperar que
  > el segundo Pod falle limpiamente. **No falla: arranca.** En un clúster de una sola máquina el disco se
  > deja montar dos veces, y el propio candado de Postgres se despista porque cada contenedor cuenta sus
  > procesos por separado. Resultado: **dos bases de datos escribiendo en el mismo disco a la vez**. Aquí no
  > hubo daños (los datos aguantaron), pero es la receta exacta de una corrupción. Vuelve a 1 réplica en
  > cuanto lo veas. El relato completo, en [kubernetes-desde-0.md](kubernetes-desde-0.md) § Bloque 3.

  > **Imagínalo así.** El backend son los **taquilleros del cine**: pon los que quieras, cualquiera te vende
  > la entrada igual. La base de datos es el **cuaderno de contabilidad**: si dos personas escriben en él a
  > la vez, se pisan los renglones y acabas sin saber qué se apuntó. No es que "no se pueda escalar" — es que
  > **el trabajo no es repartible**, porque el cuaderno es uno solo.

  > **Y `ReadWriteOnce` no significa lo que parece:** es la **llave del trastero**, y la llave la tiene *la
  > casa*, no *una persona*. Todos los que viven en esa casa (todos los Pods de la misma máquina) pueden
  > entrar. Si de verdad quieres que entre uno y solo uno, lo que pides es `ReadWriteOncePod`.

---

<a name="parte-6"></a>
## Parte 6 — Ejercicios integradores (los que te blindan para la entrevista)

> **Hechos el 2026-07-29.** Debajo de cada ejercicio queda **lo que realmente pasó**, no la teoría de lo que
> debería pasar. Cuando algo salió distinto de lo esperado, está anotado: eso es lo que se recuerda.

### 6.1 — Entornos con Namespaces — HECHO

Crea un segundo namespace `ayudas-staging` con el mismo stack, cambiando solo la configuración.
Manifiesto: [`k8s/staging/ayudas-staging.yaml`](../../k8s/staging/ayudas-staging.yaml) ·
`kubectl apply -f k8s/staging/`

**Lo observado.** Los dos entornos tienen objetos con el **mismo nombre** (`postgres`, `backend`,
`frontend`) y conviven sin chocar:

```
kubectl -n ayudas-dev     exec deploy/backend -- getent hosts postgres
  10.111.20.147   postgres.ayudas-dev.svc.cluster.local
kubectl -n ayudas-staging exec deploy/backend -- getent hosts postgres
  10.109.85.152   postgres.ayudas-staging.svc.cluster.local
```

El mismo nombre `postgres` resuelve a **dos IPs distintas**, porque el DNS interno es
`<servicio>.<namespace>.svc.cluster.local` y dentro de un namespace el sufijo se sobreentiende. Y son dos
bases de datos de verdad separadas: tras las pruebas de la Fase 1, `dev` tenía 19 trámites y `staging`
seguía con los 11 de la semilla. El `JWT_EXPIRATION` también difiere (24 h en dev, 1 h en staging) sin
tocar una línea de código: solo cambia el ConfigMap.

> **Imagínalo así.** Un namespace es una **planta de un edificio de oficinas**. En la 3.ª y en la 5.ª puede
> haber un despacho llamado "Recepción"; si estás en la 3.ª y dices "voy a recepción", vas a la tuya. Para
> ir a la otra tienes que decir el nombre completo: "recepción de la quinta".

**Nota de oficio para la entrevista:** ese fichero está copiado y pegado *a propósito*, para verlo entero.
En un proyecto real se usa **Kustomize** (base + overlays) o **Helm** (un chart con `values-dev.yaml` /
`values-staging.yaml`) para que solo exista una definición y los entornos sean parches encima.

### 6.2 — Probes de verdad — HECHO

Se añadió `spring-boot-starter-actuator` y las probes TCP pasaron a `httpGet`. Hay **tres**, y cada una
responde a una pregunta distinta:

| Probe | Pregunta | Si falla |
|-------|----------|----------|
| `startupProbe` | ¿Ha terminado de arrancar? | Nada todavía; bloquea a las otras dos hasta que pase |
| `readinessProbe` | ¿Puede atender peticiones? | El Pod **sale del Service** (deja de recibir tráfico), pero **no** se reinicia |
| `livenessProbe` | ¿Sigue vivo el proceso? | K8s **mata y recrea** el contenedor |

La decisión que más se pregunta: **`readiness` incluye la base de datos, `liveness` no.**

```yaml
readiness: { include: readinessState,db }   # sin BD no puedo responder → sácame del Service
liveness:  { include: livenessState }       # sin BD reiniciarme no arregla nada → no me mates
```

Si metieras la BD en la liveness, una caída de PostgreSQL provocaría que K8s reiniciara todos los backends
en bucle — más carga, ningún arreglo, y un incidente pequeño convertido en grande.

La `startupProbe` sustituye al `initialDelaySeconds: 60` que había antes: en vez de adivinar un margen fijo,
da hasta 30 intentos × 5 s **solo durante el arranque** y deja la liveness agresiva en régimen normal.

> **Imagínalo así.** *Readiness* es el cartel de **ABIERTO/CERRADO** de una tienda: si está cerrado no entra
> nadie, pero la tienda sigue en pie. *Liveness* es el **detector de humos**: si salta, se desaloja y se
> vuelve a montar. Y *startup* es el **rato antes de abrir**: mientras estás colocando el género, ni el
> cartel ni el detector cuentan.

### 6.3 — De Compose a K8s tú solo — HECHO

Coge `docker-compose.yml` y escribe de memoria el Deployment+Service del backend. Luego compara con
[`k8s/30-backend.yaml`](../../k8s/30-backend.yaml). **Lo que más se olvida**, por orden:

1. **`selector.matchLabels` ↔ `template.metadata.labels`.** Tienen que coincidir. Es el error nº 1: el
   Deployment crea Pods que él mismo no reconoce como suyos.
2. **El `selector` del Service.** Un Service sin `selector` correcto no tiene endpoints y devuelve
   "connection refused" sin decirte por qué. `kubectl get endpointslice` es el diagnóstico.
3. **`imagePullPolicy: IfNotPresent`.** Sin esto, con una imagen local de minikube el Pod se queda en
   `ImagePullBackOff` intentando bajarla de Docker Hub.
4. **`containerPort` ≠ `port` ≠ `targetPort`.** `port` es por dónde escucha el Service; `targetPort`, por
   dónde escucha el contenedor.

### 6.4 — Resiliencia con JWT stateless — HECHO

Backend a 3 réplicas, sesión iniciada **una sola vez**, y se borra un Pod mientras se navega:

```
Pod borrado: backend-7cdc7b4c49-chr2f
40 peticiones con el mismo token durante el borrado
  → OK: 40   FALLOS: 0
```

**Por qué no se cae la sesión.** Porque no hay sesión *en el servidor*. El JWT viaja en cada petición y
cualquiera de los tres Pods lo valida con la misma clave: no necesita recordar nada del usuario. Si hubiera
`HttpSession`, la sesión viviría en la memoria de **un** Pod concreto y matarlo tiraría al usuario a la
pantalla de login (o exigiría "sticky sessions" o un Redis compartido — complejidad que aquí no existe).

Es la decisión de arquitectura de `SecurityConfig` (`SessionCreationPolicy.STATELESS`) pagando dividendos
en infraestructura. Ese es el enlace que hay que saber contar: **una decisión de código habilita una
propiedad de despliegue.**

Detalle fino que se ve en la salida: el Pod nuevo aparece con `READY: false` y **no recibe ni una petición**
hasta que su readinessProbe pasa. Ese es exactamente el trabajo de la readiness.

### 6.5 — Límites de recursos — HECHO

`resources.requests`/`limits` están ahora en los tres Deployments.

- **`requests`** = lo que el planificador **reserva** para decidir en qué nodo cabe el Pod. Si pides más de
  lo que hay libre, el Pod se queda `Pending` — y `kubectl describe` te dice "Insufficient cpu/memory".
- **`limits`** = el techo duro. Y aquí está la pregunta de entrevista:

| Recurso | Al superar el `limit` | Por qué |
|---------|----------------------|---------|
| **Memoria** | **OOMKilled**: el kernel mata el proceso y el Pod se reinicia | La memoria no es "compartible en el tiempo": o la tienes o no la tienes |
| **CPU** | **Throttling**: el contenedor va lento, pero **nadie lo mata** | La CPU sí se reparte en el tiempo: se le dan menos ciclos y ya |

Diagnóstico: `kubectl describe pod` muestra `Last State: Terminated, Reason: OOMKilled` y el contador de
`RESTARTS` sube. Si un Pod se reinicia solo cada pocos minutos, ese es el primer sitio donde mirar.

> **Imagínalo así.** El límite de CPU es un **carril lento**: llegas más tarde, pero llegas. El límite de
> memoria es el **aforo de un ascensor**: pasado el peso, no arranca — te bajas.

### 6.6 — Preguntas orales (respuesta de 30 segundos) — HECHO

**Pod vs Deployment vs Service.** El *Pod* es la unidad que se ejecuta: uno o más contenedores que comparten
red y disco. Es efímero y su IP cambia. El *Deployment* es quien se encarga de que haya siempre N Pods vivos
de una versión concreta: si uno muere, lo repone; si cambias la imagen, los sustituye poco a poco. El
*Service* es un nombre y una IP fijos que reparten tráfico entre los Pods que estén listos, así nadie
depende de una IP que va a cambiar.

**¿Por qué no despliego Pods sueltos?** Porque un Pod suelto no se recupera: si muere, muere. Nadie lo
repone, no puedes escalarlo ni actualizarlo sin caída. El Deployment es lo que convierte "un proceso
corriendo" en "un servicio que sigue estando".

**ClusterIP vs NodePort vs LoadBalancer.** *ClusterIP* es solo interno: los demás Pods lo ven, el mundo no
(es lo que usan aquí postgres y backend). *NodePort* abre un puerto alto en cada nodo — sirve para pruebas,
es incómodo en producción. *LoadBalancer* pide un balanceador real al proveedor cloud. En este proyecto no
hace falta ninguno de los dos últimos: la entrada es un **Ingress**, que enruta por host/path y deja todos
los Services en ClusterIP.

**¿Qué es un rolling update y cómo hago rollback?** Es actualizar sin cortar el servicio: el Deployment crea
un ReplicaSet nuevo y va cambiando Pods poco a poco, respetando `maxSurge` y `maxUnavailable`, así que
siempre hay capacidad atendiendo. Se lanza con `kubectl set image` y se sigue con `rollout status`. Si sale
mal, `kubectl rollout undo` vuelve a inflar el ReplicaSet anterior — que sigue ahí, a 0 réplicas. Con
`kubernetes.io/change-cause` anotado, `rollout history` te dice qué era cada revisión.

**ConfigMap vs Secret.** Los dos inyectan configuración sin tocar la imagen. El *ConfigMap* es para lo no
sensible (URL de la BD, puerto, timeouts). El *Secret* es para credenciales: se guarda en base64 —que **no
es cifrado**— y su valor está en poder tratarlo distinto: RBAC más estricto, cifrado en reposo en etcd, no
aparece en un `describe`. En la práctica, con un gestor externo (Vault, Sealed Secrets) en producción.

**¿Por qué una BD no se escala como un backend stateless?** Porque el backend no guarda nada: da igual qué
réplica te atienda. La BD **es** el estado. Dos Pods de PostgreSQL sobre el mismo disco no dan el doble de
capacidad: corrompen los datos — lo comprobamos el 27/07, dos postmasters escribieron a la vez sobre el
mismo directorio. Escalar una BD es replicación (primario/réplicas, con su propio protocolo), y en K8s eso
es un **StatefulSet** —que da identidad y disco propio a cada Pod— más un operador (CloudNativePG) o
directamente una BD gestionada.

### Aprendizaje extra no previsto: el `spec` de un PVC es inmutable

Al endurecer `20-postgres.yaml` cambiando `ReadWriteOnce` → `ReadWriteOncePod`, `kubectl apply` falló:

```
The PersistentVolumeClaim "postgres-pvc" is invalid: spec: Forbidden:
spec is immutable after creation except resources.requests and volumeAttributesClassName
```

Un PVC ya creado solo admite **crecer** (`resources.requests`). Cambiar el modo de acceso exige **borrarlo
y recrearlo**, y con él se va el disco. En dev da igual (Flyway reconstruye la BD desde las migraciones);
en producción esto es exactamente el tipo de decisión que hay que tomar **antes** de crear el PVC.

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

Cuando llegues a la Fase 1 y empieces a separar `ms-tramitacion`, ya sabrás darle su Deployment y su Service
y enchufarlo al gateway **sin tocar los demás**. Ese es el pago de esta guía: la app está diseñada con
fronteras limpias justamente para esto.

> **Imagínalo así.** Pasas de un **restaurante con una única cocina enorme** a un **patio de comidas**: un
> puesto de pizzas, otro de sushi, otro de postres. Cada puesto abre, cierra o se amplía sin molestar a los
> vecinos, y a la entrada hay un **panel indicador** que manda a cada cliente al puesto que busca — ese panel
> es el gateway. Lo que ganas es poder reformar un puesto sin cerrar el patio; lo que pagas es que ahora hay
> que coordinar varios puestos en vez de mandar en una sola cocina.

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
  accessModes: ["ReadWriteOncePod"]   # Bloque 4: UN Pod, no un nodo (ver 6.1 y el incidente del 27/07)
  resources:
    requests:
      storage: 1Gi
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: postgres
  namespace: ayudas-dev
  annotations:
    kubernetes.io/change-cause: "Bloque 4: strategy Recreate + PVC ReadWriteOncePod"
spec:
  replicas: 1
  strategy:
    type: Recreate      # nunca dos postmasters sobre el mismo disco a la vez
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
          image: ayudas-gv-backend:2.2
          imagePullPolicy: IfNotPresent      # usa la imagen local de minikube
          ports:
            - containerPort: 8080
          envFrom:
            - configMapRef: { name: backend-config }
            - secretRef: { name: backend-secret }
          startupProbe:                      # hasta 30 x 5 s = 150 s para arrancar
            httpGet: { path: /api/actuator/health/liveness, port: 8080 }
            periodSeconds: 5
            failureThreshold: 30
          readinessProbe:
            httpGet: { path: /api/actuator/health/readiness, port: 8080 }
            periodSeconds: 10
            failureThreshold: 3
          livenessProbe:
            httpGet: { path: /api/actuator/health/liveness, port: 8080 }
            periodSeconds: 15
            failureThreshold: 3
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
