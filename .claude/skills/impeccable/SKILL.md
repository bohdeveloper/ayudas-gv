---
name: impeccable
description: Vocabulario de diseño frontend para eliminar "AI slop" y construir interfaces con identidad visual real. Úsala siempre que el usuario quiera mejorar el frontend de una web, cuando el diseño se vea generado por IA, genérico o de plantilla, o cuando pida "mejora esta página", "hazlo más profesional", "elimina el slop de IA", "mejora la tipografía", "el diseño parece vibe-coded", "hazlo más bold/quieter", "refina el layout", "añade animaciones con sentido", "audita el diseño", "revisa la accesibilidad", "hazlo production-ready". Incluye 23 comandos, 46 anti-patrones detectables y los modos brand vs product. Fuente oficial: https://impeccable.style (pbakaus/impeccable — 38k ⭐ en GitHub)
---

# Impeccable — Design Skills

El vocabulario de diseño que los modelos de IA no tienen por defecto. 23 comandos, 46 anti-patrones catalogados, dos modos (brand/product). Fuente: `pbakaus/impeccable` — `https://impeccable.style`

---

## Dos registros de diseño

Impeccable opera en dos modos porque una landing page y un dashboard tienen reglas contradictorias:

| | **Brand** (landing, marketing) | **Product** (app, dashboard, tool) |
|---|---|---|
| Objetivo | Narrar. Forma la historia. | Servir la tarea. Diseño al servicio de la función. |
| Voz | Bold, expresiva, memorable | Calm, clara, predecible |
| Tipografía | Display con personalidad fuerte | Sistema tipográfico funcional |
| Color | Paleta de marca con intención | Tokens semánticos: status, feedback, jerarquía |
| Layout | Asimétrico, tensión deliberada | Grid consistente, densidad controlada |
| Movimiento | Transiciones de marca | Microinteracciones funcionales |

**Regla crítica:** Nunca mezclar ambos registros en la misma sección. Identificar el registro antes de diseñar.

---

## 23 Comandos (vocabulario compartido)

### 🏗 CREATE — Construir

#### `/impeccable craft`
Planificar, construir y revisar visualmente una feature en un solo flujo.
- Lee primero `PRODUCT.md` y `DESIGN.md` si existen
- Propone brief de diseño antes de escribir código
- Genera el componente/página con decisiones justificadas
- Auto-revisa con `/impeccable critique` internamente

#### `/impeccable shape`
Pensar antes de construir. Produce un brief de diseño por descubrimiento, no por suposición.
- Hace preguntas sobre audiencia, contexto, personalidad de marca
- Identifica el registro (brand vs product)
- Genera brief: audiencia, tono, referencias, anti-referencias

#### `/impeccable` (freeform)
Entrada principal para trabajo en lenguaje natural o para obtener recomendación del siguiente paso.
Úsalo cuando no sepas qué comando elegir.

---

### 🔍 EVALUATE — Evaluar

#### `/impeccable critique`
Design review completo con scoring, tests de persona y detección automática.
- Puntúa el diseño en 5 dimensiones
- Ejecuta persona tests (¿qué ve un usuario nuevo? ¿un usuario avanzado?)
- Detecta anti-patrones del catálogo de slop
- Lista los problemas por prioridad (P0 → P3)

#### `/impeccable audit`
Check técnico de calidad en 5 dimensiones con severidad P0–P3:
- **Accesibilidad** — contraste, aria, focus, semántica
- **Responsive** — mobile-first, viewport, overflow
- **Performance** — LCP, bundle, lazy load, CLS
- **Slop detector** — anti-patrones del catálogo de 46 reglas
- **Implementación** — edge cases, error states, i18n

---

### ✨ REFINE — Refinar

#### `/impeccable typeset`
Corregir tipografía que se siente genérica, inconsistente o accidental.
- Selecciona pareja de fuentes con contraste de registro (display + body)
- Establece escala tipográfica con ratios mínimo 1.25×
- Fija tracking, leading y peso por nivel
- Rechaza fuentes sobreusadas: Inter, Geist, Space Grotesk, Instrument Serif

#### `/impeccable colorize`
Añadir color estratégico a interfaces monocromas sin volverse llamativo.
- Parte del tipo de producto y audiencia, no de preferencia estética
- Propone paleta con tokens semánticos (base, surface, text, accent, feedback)
- Usa OKLCH para valores de color precisos y perceptualmente uniformes
- Anti-patrón explícito: paleta AI por defecto (púrpura→azul gradiente + cyan)

#### `/impeccable bolder`
Empujar diseños seguros hacia impacto sin caer en el caos.
- Aumenta contraste tipográfico
- Introduce tensión visual deliberada
- Refuerza el elemento de firma del diseño

#### `/impeccable quieter`
Bajar el volumen a diseños que gritan sin perder su intención.
- Reduce ruido visual (sombras, bordes, colores competidores)
- Aumenta el peso del whitespace
- Mantiene la jerarquía pero con menos elementos

#### `/impeccable layout`
Corregir layout, spacing y ritmo visual.
- Establece sistema de espaciado 4pt/8pt
- Elimina monotonía de espaciado (mismo valor en todo)
- Crea ritmo: agrupaciones apretadas para relacionados, separaciones generosas entre secciones

#### `/impeccable animate`
Movimiento con propósito que transmite estado, no decoración.
- 150–300ms para micro-interacciones; ≤400ms para transiciones complejas
- Solo `transform`/`opacity` (compositor-friendly)
- Easing: ease-out al entrar, ease-in al salir
- Anti-patrón: bounce/elastic en UI (reservar para elementos físicos)
- Obligatorio: `prefers-reduced-motion`

#### `/impeccable delight`
Pequeños momentos de personalidad que convierten funcional en memorable.
- Microinteracciones en hover/focus/active estados
- Easter eggs sutiles que no interrumpen el flujo
- Momento de firma: el detalle que hace única la interfaz

#### `/impeccable overdrive`
Llevar una interfaz más allá de los límites convencionales.
- Shaders, físicas, 60fps, transiciones cinematográficas
- Solo cuando el brief lo justifica explícitamente
- Siempre con `prefers-reduced-motion` como salida de escape

---

### 🧹 SIMPLIFY — Simplificar

#### `/impeccable distill`
Substracción implacable. Reducir diseños a su esencia.
- Elimina todo elemento que no sirva la función principal
- Aplica el principio Chanel: quita un accesorio antes de salir
- Prioridad: legibilidad y claridad sobre expresión visual

#### `/impeccable clarify`
Reescribir copy de UX confuso para que las interfaces se expliquen solas.
- Voz activa: "Instala el CLI" no "El CLI será instalado"
- Etiquetas de botones específicas: "Guardar API Key" no "Continuar"
- Errores con causa + siguiente paso, no solo el problema
- Anti-patrón: buzzwords (streamline, empower, supercharge, world-class)

#### `/impeccable adapt`
Hacer que los diseños funcionen en pantallas, dispositivos y contextos sin amputar features.
- Mobile-first con breakpoints 375/768/1024/1440
- Verificar landscape y tablet
- `min-h-dvh` sobre `100vh`; `env(safe-area-inset-*)` para notches

---

### 🔧 HARDEN — Endurecer

#### `/impeccable polish`
El pase final y meticuloso entre bueno y excelente.
- Spacing, tipografía, estados interactivos, copy y consistencia
- Último comando antes de hacer deploy
- Revisa detalles que nadie nombra pero todos perciben

#### `/impeccable harden`
Hacer interfaces production-ready: edge cases, i18n, error states, overflow.
- Long strings que rompen el layout
- Números grandes / negativos / cero
- Texto RTL y LTR
- Imágenes rotas, datos ausentes, estados de error
- Overflow en contenedores flexibles

#### `/impeccable onboard`
Diseñar experiencias de primer uso, empty states y caminos hacia el valor.
- Empty state ≠ pantalla en blanco: mensaje útil + acción
- First run: reducir fricción hasta la primera acción de valor
- Loading states con skeleton, no spinner infinito

#### `/impeccable optimize`
Diagnosticar y corregir performance de UI: LCP a bundle size.
- WebP/AVIF, srcset, lazy loading
- Bundle splitting por ruta
- Virtualización de listas >50 items
- Eliminar layout thrash (getBoundingClientRect en render)

---

### ⚙️ SYSTEM — Sistema

#### `/impeccable init`
Configurar el proyecto: crea `PRODUCT.md` y `DESIGN.md`.
- Entrevista corta: audiencia, casos de uso, personalidad de marca
- Anti-referencias explícitas (lo que nunca debe parecer)
- Genera `DESIGN.md` en formato Google Stitch spec

#### `/impeccable extract`
Extraer componentes, tokens y patrones reutilizables al design system.
- Detecta patrones que se repiten en el código
- Los formaliza como tokens CSS o componentes

#### `/impeccable document`
Generar un `DESIGN.md` que capture el sistema visual para que todos los agentes se mantengan on-brand.
- Formato Google Stitch spec (portable entre herramientas)
- Incluye: paleta, tipografía, spacing, componentes, voz

#### `/impeccable live` *(Claude Code / entorno con servidor)*
Iterar en la UI en el navegador en tiempo real.
- Pick element → comment/stroke → 3 variantes → aceptar a source

---

## 46 Anti-patrones de AI Slop

Catálogo completo de Impeccable. Se dividen en: **AI slop** (tells de UI generada por IA) y **Quality** (errores de diseño generales).

### Visual Details (7)
| Anti-patrón | Problema | Fix |
|---|---|---|
| **Border accent on rounded element** | Borde grueso coloreado en card redondeada — el borde choca con el radio | Eliminar el borde o el border-radius |
| **Glassmorphism everywhere** | Blur/glass como decoración, no para resolver un problema real de capas | Solo blur cuando indica contenido descartable detrás (modales, sheets) |
| **Side-tab accent border** | Borde grueso en un solo lado de una card — el tell más reconocible de UI generada por IA | Acento más sutil o eliminar completamente |
| **Hairline border with wide shadow** | Borde de 1px + sombra difusa amplia — firma de UI generada | Elegir uno: borde definido o elevación suave, nunca los dos |
| **Repeating-gradient stripes** | Rayas de repeating-gradient como decoración de superficie | Textura deliberada o superficie limpia |
| **Extreme border-radius on cards** | Cards con radius 24px+ convirtiéndolo todo en blob suave | Cards: máximo 12–16px; pill solo para tags y botones |
| **Amateurish hand-drawn SVG** | Ilustraciones SVG hand-coded que parecen garabatos, no whimsy | Sin ilustración si no hay assets reales |

### Typography (10)
| Anti-patrón | Problema | Fix |
|---|---|---|
| **Flat type hierarchy** | Tamaños de fuente demasiado cercanos, sin jerarquía visual | Mínimo ratio 1.25× entre steps |
| **Icon tile stacked above heading** | Icono en cuadrado redondeado encima del heading — template universal de feature cards | Side-by-side o icono en flow sin contenedor |
| **Italic serif display headline** | Serif itálica oversize como hero principal — reflex universal de landing AI startup | Roman, o fuente display no-serif |
| **Hero eyebrow / pill chip** | Label pequeño uppercase encima del hero — scaffold de SaaS generado | Eliminar el eyebrow o integrarlo en el headline |
| **Repeated section kicker labels** | Labels uppercase tracked repetidos sobre cada sección | Estructura más fuerte: artefactos, imágenes, sistema de marca |
| **Oversized hero headline** | Frase completa a tamaño display ocupa todo el fold | Headlines cortos a ese tamaño; frases largas a tamaño menor |
| **Crushed letter spacing** | Tracking más apretado que donde los caracteres mantienen su forma | Apretar tipografía display ópticamente, no destructivamente |
| **Overused font** | Inter, Geist, Space Grotesk, Instrument Serif — sin personalidad propia | Fuente con personalidad para el proyecto específico |
| **Single font for everything** | Solo una familia en toda la página | Pareja display + body con contraste de registro |
| **All-caps body text** | Texto largo en mayúsculas perjudica la lectura | Mayúsculas solo para labels cortos y headings |

### Color & Contrast (5)
| Anti-patrón | Problema | Fix |
|---|---|---|
| **AI color palette** | Gradientes púrpura/violeta y cyan-sobre-negro — tell más reconocible de UI generada | Paleta deliberada específica del producto |
| **Dark mode with glowing accents** | Fondos oscuros con glow de box-shadow coloreado — look "cool" por defecto de IA | Iluminación sutil y con propósito |
| **Gradient text** | Texto con gradiente — decorativo, no significativo | Colores sólidos en texto |
| **Gray text on colored background** | Texto gris sobre fondo coloreado — lavado y difícil de leer | Tono oscuro del color de fondo, o blanco con contraste |
| **Cream / beige palette** | Fondo crema/beige — la superficie "con gusto" por defecto de IA | Fondo desde paleta deliberada, no warm off-white por reflejo |

### Layout & Space (8)
| Anti-patrón | Problema | Fix |
|---|---|---|
| **Hero metric layout** | Número grande + label pequeño + 3 stats + acento gradiente — usado en todas partes | Layout que comunica el valor real del producto |
| **Identical card grids** | Cards del mismo tamaño con icono+heading+texto repetidas — homepage AI por defecto | Variedad de tamaños y jerarquías en el grid |
| **Monotonous spacing** | El mismo valor de espacio en todas partes — sin ritmo ni variación | Agrupaciones apretadas para relacionados, separaciones generosas entre secciones |
| **Nested cards** | Cards dentro de cards — ruido visual y profundidad excesiva | Aplanar: usar spacing, tipografía y divisores en vez de nesting |
| **Numbered section markers (01/02/03)** | Marcadores numéricos como labels de sección — scaffold editorial de IA | Números solo si el contenido es realmente una secuencia |
| **Line length too long** | Líneas de texto >80 chars — el ojo pierde el camino de vuelta | `max-width: 65ch` a `75ch` en contenedores de texto |
| **Content overflowing container** | Contenido más ancho que su contenedor — scroll horizontal no deseado | Wrap de texto, anchos restringidos, scroll affordance deliberado |
| **Positioned child clipped by overflow** | `overflow: hidden` cortando tooltips/menus absolutely positioned | `overflow: visible` o mover la capa posicionada fuera del clip |

### Motion (3)
| Anti-patrón | Problema | Fix |
|---|---|---|
| **Bounce / elastic easing** | Bounce y elastic en elementos de UI — se siente anticuado | Reservar spring physics para elementos físicos; ease-out-quart/quint/expo para UI |
| **Layout property animation** | Animar width/height/padding/margin — layout thrash y jank | `transform` y `opacity` únicamente; `grid-template-rows` para height |
| **Image hover transform** | Escalar o rotar imagen en hover — firma de UI generada | Imágenes estáticas; interacción sutil y con propósito si es necesaria |

### Copy (4)
| Anti-patrón | Problema | Fix |
|---|---|---|
| **Em-dash overuse** | Más de un par de em-dashes en body copy — cadencia de IA | Comas, dos puntos, puntos, paréntesis |
| **Marketing buzzword** | streamline, empower, supercharge, world-class, enterprise-grade | Verbo + sustantivo específico que describe lo que el producto hace literalmente |
| **Aphoristic-cadence copy** | Secciones que terminan en rebatimiento corto o contraste manufacturado | Una vez está bien; el patrón repetido es el tell |
| **Theater framing copy** | Llamar "theater" a algo — tic de copy generado | Decir directamente lo que la cosa hace o no hace |

### Imagery (1)
| Anti-patrón | Problema | Fix |
|---|---|---|
| **Broken or placeholder image** | `<img>` con src vacío, missing o placeholder — muestra caja de imagen rota | Imágenes reales, assets generados, o eliminar el tag |

### General Quality (8)
| Anti-patrón | Problema | Fix |
|---|---|---|
| **Cramped padding** | Texto demasiado cerca del borde de su contenedor | Mínimo 8px; idealmente 12–16px de padding interno |
| **Body text touching viewport edge** | Párrafos a ras del borde del viewport | Contenedor con mínimo 16px horizontal padding, o `max-width` + `mx-auto` |
| **Justified text** | Texto justificado sin hifenación — "ríos de blanco" | `text-align: left`; `hyphens: auto` solo si es obligatorio justificar |
| **Low contrast text** | No cumple WCAG AA (4.5:1 body, 3:1 texto grande) | Aumentar contraste texto/fondo |
| **Skipped heading level** | h1 seguido de h3 sin h2 — rompe el outline del documento | Headings secuenciales h1→h6 sin saltos |
| **Tight line height** | Line-height <1.3× — texto multi-línea difícil de leer | 1.5–1.7 para body text |
| **Tiny body text** | Body text <12px — ilegible en pantallas HiDPI | Mínimo 14px body; 16px es el ideal |
| **Wide letter spacing on body text** | Tracking >0.05em en body — disrumpe agrupaciones naturales | Tracking amplio solo para labels uppercase cortos |

---

## Flujo de trabajo recomendado

### Para una página nueva
```
1. /impeccable shape     → Brief de diseño por descubrimiento
2. /impeccable craft     → Planificar + construir en un flujo
3. /impeccable critique  → Design review con scoring
4. /impeccable polish    → Pase final de refinamiento
```

### Para una página existente
```
1. /impeccable audit     → Check técnico P0–P3
2. /impeccable critique  → Design review + detección de slop
3. [comando específico]  → typeset / colorize / layout / bolder / quieter
4. /impeccable polish    → Pase final
```

### Pares de comandos útiles
- `bolder` ↔ `quieter` — dos mitades de voz; sin neutralidad
- `audit` → `harden` — detectar problemas técnicos, luego corregirlos
- `critique` → `polish` — revisar el trabajo, luego refinarlo
- `shape` → `craft` — planificar la feature, luego construir el plan

---

## Contexto de proyecto (PRODUCT.md / DESIGN.md)

Impeccable captura el contexto una vez y lo lee en cada comando.

**PRODUCT.md** — captura el brief:
```
Usuarios: [quiénes son, cómo leen, cuándo usan]
Registro: [brand / product]
Voz de marca: [cómo debe sentirse]
Anti-referencias: [lo que nunca debe parecer]
```

**DESIGN.md** — captura el sistema visual (formato Google Stitch):
```
Paleta: tokens semánticos en OKLCH
Tipografía: roles, pesos, escala
Spacing: sistema 4pt/8pt
Componentes: inventario de estados
```

Cuando existan estos archivos, leerlos antes de cualquier comando para no inventar un sistema nuevo encima del existente.

---

## Detección de slop en código

Para auditar un archivo o componente en busca de anti-patrones, aplicar el catálogo de 46 reglas contra el código y reportar en formato:

```
## src/Hero.tsx

src/Hero.tsx:12 — gradient-text [AI slop]
src/Hero.tsx:34 — side-tab-accent-border [AI slop]
src/Hero.tsx:67 — marketing-buzzword: "supercharge" [AI slop]
src/Hero.tsx:89 — low-contrast-text (ratio estimado ~2.1:1) [Quality]

## src/Card.tsx

✓ sin anti-patrones detectados
```

**3 quick wins al final** — los fixes de menor esfuerzo con mayor impacto visual.
