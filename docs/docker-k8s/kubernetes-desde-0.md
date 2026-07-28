# Kubernetes desde 0 — la Fase K8s de Ayudas GV contada en cristiano

> Este documento cuenta **qué hicimos, por qué y qué aprendimos** al llevar Ayudas GV a Kubernetes, sin dar
> nada por sabido. Si nunca has tocado K8s, empieza aquí: se lee de corrido y no hace falta ejecutar nada.
>
> El **manual de trabajo** (comandos, ejercicios, ficheros completos) es
> [kubernetes-aprendizaje.md](kubernetes-aprendizaje.md). El **checklist** vivo está en
> [plan.md](../../plan.md). Esto de aquí es el relato: lo que le contarías a alguien en una cafetería.
>
> Cada concepto lleva su recuadro `> **Imagínalo así.**`. Las palabras técnicas (Pod, Service, Namespace) se
> usan siempre — la gracia es que puedas soltarlas **sabiendo lo que significan**, no repetirlas de memoria.

---

## Índice

- [El problema, antes de la solución](#el-problema)
- [El vocabulario mínimo, de una tacada](#vocabulario)
- [Bloque 1 — Montar el clúster (23/07)](#bloque-1)
- [Bloque 2 — Mudar la aplicación al clúster (24/07)](#bloque-2)
- [Bloque 3 — Operarlo: escalar, romper y arreglar (27/07)](#bloque-3)
- [Bloque 4 — Lo que queda (28/07)](#bloque-4)
- [Las seis preguntas y cómo responderlas en 30 segundos](#preguntas)

---

<a name="el-problema"></a>
## El problema, antes de la solución

Antes de esta fase, Ayudas GV se levantaba con `docker compose up`: tres piezas (base de datos, backend,
frontend) arrancando juntas en un ordenador. Funciona perfectamente... mientras nadie apague nada.

El problema aparece con tres preguntas incómodas:

1. **Si el backend se cae a las tres de la mañana, ¿quién lo levanta?** Con Compose: tú, cuando te enteres.
2. **Si de repente entran diez veces más usuarios, ¿cómo pones más copias?** Con Compose: a mano, y avisando
   a todo el mundo de las direcciones nuevas.
3. **Si quieres subir una versión nueva, ¿cómo lo haces sin que nadie note un corte?** Con Compose: no puedes.
   Paras y arrancas.

Kubernetes existe para responder a esas tres. Y su idea central es un cambio de forma de pedir las cosas:

> **Imagínalo así.** Un **mando de la tele** frente a un **termostato**. Con el mando das órdenes sueltas
> (sube, baja, apaga) y si la tele se apaga sola ahí se queda: eso es Docker Compose. Al termostato le dices
> "21 grados" y se acabó: no le mandas encender ni apagar, él mide y corrige solo, aunque abras una ventana.
> Kubernetes es el termostato. Tú fijas **cómo quieres que estén las cosas** y él se pasa el día
> consiguiéndolo.

Ese "pasarse el día comprobando y corrigiendo" tiene nombre técnico —**bucle de reconciliación**— y una
imagen todavía más casera: la **cisterna del váter**. Se vacía, y el flotador la rellena hasta la marca sin
que nadie se lo pida. Toda la magia de Kubernetes es esa idea repetida a distintos niveles.

---

<a name="vocabulario"></a>
## El vocabulario mínimo, de una tacada

Seis palabras y ya puedes seguir el resto del documento:

| Palabra | Qué es | Imagínalo así |
|---|---|---|
| **Pod** | La pieza más pequeña: la envoltura de tu programa en marcha | Un **camarote de tren**: quien viaja dentro comparte número y armario |
| **Deployment** | El que se asegura de que siempre haya N copias vivas | El **dispensador de servilletas**: siempre asoma una nueva |
| **Service** | Un nombre fijo delante de copias que van cambiando | El **teléfono de la pizzería**: mismo número, atiende quien esté libre |
| **Ingress** | La puerta única desde fuera, que reparte según a dónde vayas | El **conserje**: "¿la peluquería? Tercero" |
| **Namespace** | Una división para que convivan varios entornos sin pisarse | Los **pisos** de un edificio: hay un Juan en el 2.ºA y otro en el 3.ºA |
| **PVC** | La petición de un disco que sobrevive a los Pods | Alquilar un **trastero**: cambias de casa y sigue siendo tuyo |

Dos ideas transversales que valen más que las seis palabras:

- **Los Pods son bombillas, no mascotas.** Cuando se funde una no le pones nombre ni la revives: pones otra
  igual. Por eso nunca te diriges a un Pod concreto — hablas con el Service.
- **Nada de lo que importa vive dentro del Pod.** Los datos van al trastero (PVC), la configuración a su sitio
  (ConfigMap/Secret). El Pod es desechable **a propósito**.

---

<a name="bloque-1"></a>
## Bloque 1 — Montar el clúster (23/07)

**Lo que había que conseguir:** tener un Kubernetes de verdad funcionando en el portátil, y que supiera
encontrar las imágenes de Ayudas GV.

**Lo que se hizo:** instalar `minikube` (el clúster) y `kubectl` (el mando a distancia para hablar con él),
arrancarlo con `minikube start --driver=docker`, y comprobar que había una máquina lista (`kubectl get nodes`
→ un nodo `Ready`). Antes de eso, un `.dockerignore` en backend y frontend para no arrastrar basura al
construir.

> **Imagínalo así.** minikube es un **piso piloto**: cocina, baño y salón de verdad, todo funcionando, pero
> montado dentro de una nave. Sirve para aprender a moverte por la casa. Lo que **no** puedes ensayar ahí es
> lo que pasa *entre varias casas*, porque solo hay una — y eso nos pasará factura en el Bloque 3.

### La piedra en el camino: dos neveras

El momento en que casi todo el mundo se atasca. Construyes tu imagen, se la pides al clúster, y el clúster
dice que no la encuentra.

> **Imagínalo así.** Has hecho una tarta y la has metido en **tu nevera**. Tu vecino no la tiene en la suya,
> por mucho que viváis en el mismo edificio. Tu Windows tiene su Docker; la máquina de minikube tiene **otro**.
> Solo hay dos salidas: **llevarle la tarta** o **ir a cocinarla a su casa**.

Elegimos cocinar allí: apuntar el Docker al de minikube y construir las imágenes `:1.0` **dentro** del clúster.
Si no haces ni una cosa ni la otra, el Pod se queda intentándolo en bucle y en pantalla pone
`ImagePullBackOff` — que traducido es *"llevo un rato mirando la nevera y sigue vacía"*.

**Resultado:** clúster arrancado (minikube v1.38.1, Kubernetes v1.35.1) y las dos imágenes del proyecto
dentro de él.

---

<a name="bloque-2"></a>
## Bloque 2 — Mudar la aplicación al clúster (24/07)

**Lo que había que conseguir:** que Ayudas GV entera —base de datos, backend y frontend— funcionara dentro
del clúster, y poder hacer login de punta a punta.

Aquí nació la carpeta [k8s/](../../k8s/), que es la traducción del `docker-compose.yml` al idioma de Kubernetes.
Seis ficheros, aplicados **en el orden en que se necesitan unos a otros**:

> **Imagínalo así.** Es **mudarse a un piso**. Primero la llave y el piso vacío, luego el alta de luz y agua,
> después la nevera con la comida, luego la cocina, después el comedor, y al final el timbre y el buzón para
> que te encuentren desde la calle. Al revés no funciona: no enchufas la nevera antes de tener luz.

| Fichero | Qué monta | En la mudanza |
|---|---|---|
| `00-namespace.yaml` | El namespace `ayudas-dev` | La llave del piso vacío |
| `10-backend-config.yaml` | ConfigMap + Secret | El alta de luz y agua |
| `20-postgres.yaml` | Base de datos + su disco (PVC) | La nevera, con la comida dentro |
| `30-backend.yaml` | El backend + su Service | La cocina |
| `40-frontend.yaml` | El frontend + su Service | El comedor donde se sirve |
| `50-ingress.yaml` | La entrada HTTP | El timbre y el buzón |

### Por qué la configuración va en dos sitios

Kubernetes separa los ajustes según el peligro que tenga que alguien los lea:

> **Imagínalo así.** El **ConfigMap** es la nota pegada en la nevera con el horario del gimnasio: a la vista,
> no pasa nada. El **Secret** es el PIN de la tarjeta: va en un cajón. Y un detalle que conviene decir tal
> cual: los Secret se guardan en base64, que **no es cifrado** — es escribir el PIN **del revés**. Quien abra
> el cajón lo lee igual. Lo que protege el PIN no es el disfraz, es **quién tiene llave del cajón**.

### Lo que se demostró funcionando

- Los **tres Pods en marcha** y el login `admin/admin123` respondiendo correctamente de punta a punta.
- **Que se encuentran por el nombre**: desde dentro del backend, preguntar por `postgres` devolvía la
  dirección correcta. Igual que en Compose, y por la misma razón por la que en el móvil pulsas "Mamá" en vez
  de marcar el número.
- **Un fallo provocado a propósito**: poner mal la contraseña en el Secret para ver qué cara pone el clúster.
  Salió `password authentication failed`, el Pod nunca llegó a estar listo, y se arregló restaurando el
  Secret. **Aprender a leer los errores vale más que el camino feliz** — es la diferencia entre saber
  desplegar y saber mantener.

---

<a name="bloque-3"></a>
## Bloque 3 — Operarlo: escalar, romper y arreglar (27/07)

Aquí es donde Kubernetes deja de ser "una forma complicada de arrancar contenedores" y se ve para qué sirve.
Cuatro ejercicios.

### 1. Poner tres copias del backend en vez de una

Un comando, `kubectl scale ... --replicas=3`, y de una copia pasamos a tres.

Lo interesante no es que aparezcan dos Pods más, sino **que nadie tuvo que enterarse**. No se tocó el
frontend, ni ninguna dirección, ni ninguna configuración. El Service ya tenía tres destinos detrás.

> **Imagínalo así.** Abres **dos cajas más en el supermercado**. No hay que avisar a los clientes ni cambiar
> los carteles: la cola se reparte sola.

Se comprobó de verdad, no de palabra: seis peticiones de login se repartieron **3, 1 y 2** entre las tres
copias. Y ese reparto desigual enseña algo — Kubernetes elige por cada conexión, **no va turnándose con
precisión de reloj**. Como en el súper: una caja atiende a tres clientes y otra a uno, y no pasa nada.

### 2. Matar un Pod a ver qué pasa

Se borró uno de los tres a propósito. El sustituto apareció **en 2 segundos** y estuvo listo para trabajar
**en 77**. Durante todo ese rato, la aplicación siguió respondiendo sin un solo error.

Dos detalles que son el verdadero aprendizaje:

- **El que se estaba muriendo dejó de recibir trabajo antes de morir.** No se le mandó una última petición
  para que fallara: se le sacó de la lista primero.
- **El que estaba arrancando no recibió nada hasta estar listo.** Ahí está la diferencia entre las dos
  preguntas que Kubernetes hace todo el rato:

> **Imagínalo así.** Un compañero acaba de llegar a la oficina, con el abrigo puesto y el ordenador
> arrancando. **¿Está vivo?** Sí, no llames a la ambulancia. **¿Está listo para que le pases llamadas?** No,
> dale cinco minutos. Son dos preguntas distintas (*liveness* y *readiness*) y confundirlas se paga caro: si
> tratas "todavía no está listo" como "está muerto", **reinicias a alguien que solo necesitaba tiempo**, y
> como la próxima vez tardará lo mismo, entras en un bucle de reinicios sin fin.

Y una distinción que suele preguntarse en entrevistas, vista en la misma sesión:

| Qué pasa | Quién reacciona | Cómo lo notas |
|---|---|---|
| El programa de dentro casca | El vigilante de la máquina lo **reinicia en el sitio** | Mismo Pod y mismo nombre; sube el contador `RESTARTS` |
| El Pod entero desaparece | El Deployment **crea uno nuevo** | Nombre nuevo, dirección nueva, `RESTARTS` a cero |

Uno es **rearmar el diferencial de la luz**; el otro es **poner una bombilla nueva**.

### 3. Cambiar de versión sin cortar el servicio

Se construyó una versión `:1.1`, se le dijo al Deployment que la usara, y Kubernetes fue sustituyendo las
tres copias **una a una**.

> **Imagínalo así.** El **relevo de turno en una tienda**. El cajero nuevo llega, se pone el uniforme, cuenta
> su caja y **solo cuando está listo** el anterior cierra la suya y se marcha. En ningún momento hay menos
> mostradores de los necesarios. Ese es todo el secreto de actualizar sin cortes: **entra el nuevo antes de
> irse el viejo**.

Después se deshizo el cambio con un solo comando, y aquí apareció la pieza que hace posible la marcha atrás:

> **Imagínalo así.** Cada versión tiene su **cortapastas de galletas** — el molde con el que salen todas las
> copias. Al cambiar de versión se estrena molde, pero **el viejo no se tira: se guarda en el cajón**. Volver
> atrás no reconstruye ni recompila nada: **saca el molde de antes y vuelve a usarlo**. Por eso tarda
> segundos y casi nunca falla.

Con un detalle que descoloca al principio: **deshacer no borra el error del historial, añade un paso nuevo**.
Como en la contabilidad de una tienda, donde un apunte equivocado no se tacha, se corrige con otro apunte.
Por eso siempre puedes deshacer el deshacer.

### 4. Intentar hacer lo mismo con la base de datos (y romperla)

El último ejercicio era razonar por qué la base de datos **no** se puede multiplicar como el backend. En vez
de razonarlo sobre el papel, se probó.

> **Imagínalo así.** El backend son los **taquilleros del cine**: pon los que quieras, cualquiera te vende la
> entrada igual. La base de datos es el **cuaderno de contabilidad**: si dos personas escriben en él a la vez,
> se pisan los renglones. No es que "no se pueda escalar" — es que **el trabajo no es repartible**, porque el
> cuaderno es uno solo.

Lo esperado era que el segundo Postgres se negara a arrancar. **No se negó: arrancó.** Y la cadena de
consecuencias resultó más instructiva que el resultado que se buscaba:

1. La protección del disco (`ReadWriteOnce`) **no protege de esto**. Es la **llave del trastero, y la tiene la
   casa, no una persona**: todos los Pods de la misma máquina pueden entrar. Como aquí solo hay una máquina
   (el piso piloto del Bloque 1), los dos entraron.
2. Postgres tiene su propio candado para impedir que dos copias usen el mismo disco... y **se despistó**. El
   candado se basa en anotar "me está usando el proceso número tal", pero **cada contenedor cuenta sus
   procesos por su cuenta y ambos se llamaban igual**. El segundo leyó la nota, creyó reconocer su propia
   letra, la tiró a la basura y entró.
3. Durante **76 segundos hubo dos bases de datos escribiendo en el mismo disco**. Esa es la ventana en la que
   se corrompen los datos de verdad.
4. Al quitar la copia sobrante, esta se llevó la nota del candado al salir. La original fue a mirar su propia
   nota, no la encontró, y **se apagó de golpe** por seguridad. El clúster la levantó otra vez sola.
5. **Los datos sobrevivieron**: todas las migraciones intactas, los usuarios y los registros en su sitio, y el
   login funcionando. Salvados por el cuaderno de bitácora que Postgres lleva por dentro.

**La lección que vale para cualquier sitio:** las defensas de un programa dan por hechas cosas del entorno, y
**los contenedores rompen justo esas suposiciones**. El candado de Postgres llevaba décadas funcionando; lo
tumbó un detalle de cómo se aíslan los contenedores.

Lo que sí lo impediría de verdad: pedir el disco como `ReadWriteOncePod`, que sí significa "un único Pod, y
punto".

### Entonces, ¿qué es un StatefulSet?

Es el hermano del Deployment pensado para las cosas que **sí tienen identidad y guardan datos**. La
diferencia práctica: en un Deployment todas las copias piden **el mismo disco**; en un StatefulSet **cada
copia tiene el suyo**, y además los Pods tienen nombres estables y ordenados que sobreviven a los reinicios.

> **Imagínalo así.** El Deployment es una **habitación de hotel**: te dan la que esté libre y te da igual
> cuál. El StatefulSet es un **piso con tu nombre en el buzón**: siempre el mismo, con tu trastero, y si te
> vas y vuelves sigue siendo el tuyo.

Pero aquí está el matiz que casi nadie dice, y que distingue en una entrevista: **un StatefulSet no te da una
base de datos replicada**. Te daría dos Postgres con dos discos vacíos e independientes — dos cuadernos sin
relación, no un equipo. Que dos bases de datos se coordinen de verdad (una manda, la otra copia, y si la
primera cae la segunda toma el relevo) es trabajo de un **operador** especializado o de una base de datos
gestionada por el proveedor.

**Para este proyecto, la respuesta correcta sigue siendo una sola copia.**

---

<a name="bloque-4"></a>
## Bloque 4 — Lo que queda (28/07)

El último bloque cierra la Fase K8s. Son ejercicios que combinan lo anterior:

- **Un segundo entorno** (`ayudas-staging`) con el mismo montaje pero configuración distinta, para ver que no
  se pisan. Es el edificio con dos pisos: un Juan en el 2.ºA y otro en el 3.ºA.
- **Preguntas de salud "de verdad"**: hoy Kubernetes comprueba si el backend está listo llamando a su puerta y
  viendo si alguien abre. Pasaremos a **preguntarle cómo se encuentra** — la diferencia entre ver luz en la
  ventana y que el vecino te diga que está bien.
- **Escribir de memoria** el Deployment y el Service del backend, y luego comparar. Duele, y por eso funciona.
- **Resiliencia con sesión**: borrar un Pod mientras navegas y comprobar que **no te echa de la sesión**.
  Funciona porque el proyecto no guarda la sesión en el servidor: el propio usuario lleva su credencial encima
  (JWT). Es la diferencia entre **una pulsera de festival** (te la miran y entras, da igual quién esté en la
  puerta) y **una lista de invitados** que solo tiene un portero concreto.
- **Poner límites de consumo** a cada Pod y entender qué pasa al pasarse: si te pasas de **memoria**, te matan
  en el acto; si te pasas de **CPU**, solo te frenan. Como pasarse de peso en el ascensor (no arranca) frente a
  ir en hora punta por la autopista (llegas, pero despacio).
- **Dos arreglos detectados el 27/07**: proteger la base de datos para que un cambio de versión no repita el
  accidente del doble arranque, y dejar anotado **por qué** se hizo cada despliegue, que ahora el historial es
  una lista de números sin explicación.

---

<a name="preguntas"></a>
## Las seis preguntas y cómo responderlas en 30 segundos

Las típicas de entrevista, con la respuesta corta y la imagen por si te quedas en blanco. Aquí están las seis
nucleares con analogía; la batería completa (30 preguntas de Docker y K8s, más los diagramas de apoyo y la
chuleta de comandos) está en [entrevista-docker-k8s.md](entrevista-docker-k8s.md).

**1. ¿Pod, Deployment y Service?**
El **Pod** es tu programa en marcha, y es desechable. El **Deployment** se encarga de que siempre haya N Pods
vivos y de cambiarlos de versión sin cortes. El **Service** es el nombre fijo por el que se les llama, porque
los Pods cambian de dirección constantemente.
*Bombilla, dispensador de servilletas, teléfono de la pizzería.*

**2. ¿Por qué no despliego Pods sueltos?**
Porque un Pod que muere no vuelve. Nadie lo está vigilando. El Deployment es precisamente el que vigila.
*Una bombilla fundida no se enciende sola; hace falta alguien que reponga.*

**3. ¿ClusterIP, NodePort o LoadBalancer?**
Cambia **quién puede llamar**. ClusterIP solo desde dentro; NodePort abre un puerto hacia fuera; LoadBalancer
pide un balanceador de verdad al proveedor de nube.
*Telefonillo del edificio, ventana a la calle, recepcionista en la puerta.*

**4. ¿Qué es un rolling update y cómo se deshace?**
Sustituir las copias de una en una, levantando la nueva **antes** de retirar la vieja, para que no haya corte.
Deshacerlo es un comando, y es rápido porque la definición anterior nunca se borró.
*Relevo de turno en la tienda; y el molde viejo, guardado en el cajón.*

**5. ¿ConfigMap o Secret?**
ConfigMap para lo que no importa que se lea; Secret para credenciales. Y aclara siempre que **base64 no es
cifrado**: lo que de verdad protege un Secret es controlar quién puede leerlo.
*Nota en la nevera contra PIN en el cajón; y el PIN está escrito del revés, no en clave.*

**6. ¿Por qué una base de datos no se escala como un backend?**
Porque el backend no guarda nada y cualquier copia sirve, mientras que la base de datos **es** el estado:
varias copias sobre el mismo disco se pisan. Se resuelve con StatefulSet (identidad y disco propio por copia)
y, para replicación real, con un operador o una base de datos gestionada.
*Taquilleros del cine contra el cuaderno de contabilidad.*
*Y si quieres rematar: "lo probamos, y lo interesante es que no falla como uno espera — arranca igual, porque
`ReadWriteOnce` limita por máquina, no por Pod".* Esa frase demuestra que lo has hecho, no leído.

---

*Documento divulgativo de la Fase K8s de Ayudas GV. Detalle técnico y comandos en
[kubernetes-aprendizaje.md](kubernetes-aprendizaje.md); estado del trabajo en [plan.md](../../plan.md);
diario de cada sesión en [../reportes/](../reportes/).*
