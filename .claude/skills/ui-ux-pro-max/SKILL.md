---
name: ui-ux-pro-max
description: UI/UX design intelligence para web y mobile. Incluye 67 estilos UI, 161 paletas de color, 57 combinaciones tipográficas, 99 guías UX, 25 tipos de gráficos y 161 reglas de razonamiento para 16 stacks tecnológicos (React, Next.js, Vue, Svelte, SwiftUI, React Native, Flutter, Tailwind, shadcn/ui, HTML/CSS). Úsala cuando el usuario quiera diseñar páginas nuevas (landing, dashboard, admin, SaaS, mobile app), crear o refactorizar componentes UI (botones, modales, formularios, tablas, gráficos), elegir colores, tipografías, espaciado o sistema de layout, revisar código UI por accesibilidad o consistencia visual, o cuando diga "mejora el frontend", "qué estilo le pongo", "el diseño no queda profesional", "necesito una paleta", "elige fuentes para mi app", "revisa la UX", "implementa dark mode", "optimiza mobile", "hazlo accesible". Fuente oficial: https://github.com/nextlevelbuilder/ui-ux-pro-max-skill (72.9k ⭐)
---

# UI/UX Pro Max — Design Intelligence

Guía de diseño completa para web y mobile. 67 estilos, 161 paletas, 57 font pairings, 99 guías UX, 25 tipos de gráfico, 10 stacks tecnológicos.

**Fuente oficial:** `https://github.com/nextlevelbuilder/ui-ux-pro-max-skill`

---

## Cuándo aplicar esta skill

### Obligatorio
- Diseñar páginas nuevas (Landing, Dashboard, Admin, SaaS, Mobile App)
- Crear/refactorizar componentes UI (botones, modales, formularios, tablas, gráficos)
- Elegir paletas de color, sistemas tipográficos, escala de espaciado o layout
- Revisar código UI por UX, accesibilidad o consistencia visual
- Implementar navegación, animaciones o comportamiento responsive
- Decisiones de diseño a nivel producto (estilo, jerarquía de información, marca)

### Recomendado
- UI que "no parece suficientemente profesional" sin causa clara
- Feedback sobre usabilidad o experiencia
- Optimización pre-lanzamiento
- Alineación cross-platform (Web / iOS / Android)
- Construcción de design systems o librerías de componentes

### No aplica
- Lógica backend pura
- Diseño de API o base de datos
- Optimización de performance sin relación con la interfaz
- Infraestructura o DevOps

---

## Reglas por categoría (prioridad 1→10)

| Prioridad | Categoría | Impacto | Checks clave |
|-----------|-----------|---------|--------------|
| 1 | Accesibilidad | CRÍTICO | Contraste 4.5:1, alt text, nav teclado, aria-labels |
| 2 | Touch & Interacción | CRÍTICO | Mín 44×44px, 8px+ spacing, feedback de carga |
| 3 | Performance | ALTO | WebP/AVIF, lazy loading, reservar espacio (CLS < 0.1) |
| 4 | Selección de Estilo | ALTO | Coherencia, SVG icons (no emojis) |
| 5 | Layout & Responsive | ALTO | Mobile-first, viewport meta, sin scroll horizontal |
| 6 | Tipografía & Color | MEDIO | Base 16px, line-height 1.5, tokens semánticos |
| 7 | Animación | MEDIO | 150–300ms, transform/opacity solo, motion meaningful |
| 8 | Formularios & Feedback | MEDIO | Labels visibles, error inline, estado submit |
| 9 | Patrones de Navegación | ALTO | Back predecible, bottom nav ≤5, deep linking |
| 10 | Charts & Datos | BAJO | Leyendas, tooltips, colores accesibles |

---

## Quick Reference completa

### 1. Accesibilidad (CRÍTICO)

- `color-contrast` — Mínimo 4.5:1 para texto normal, 3:1 para texto grande
- `focus-states` — Focus rings visibles en elementos interactivos (2–4px)
- `alt-text` — Alt descriptivo en imágenes con significado
- `aria-labels` — aria-label en botones solo con icono
- `keyboard-nav` — Orden tab coincide con orden visual; soporte teclado completo
- `form-labels` — Usar `<label>` con atributo `for`
- `skip-links` — Skip to main content para usuarios de teclado
- `heading-hierarchy` — h1→h6 secuencial, sin saltar niveles
- `color-not-only` — No transmitir info solo por color (añadir icono/texto)
- `dynamic-type` — Soportar escalado de texto del sistema
- `reduced-motion` — Respetar `prefers-reduced-motion`
- `escape-routes` — Cancelar/volver en modales y flujos multi-paso

### 2. Touch & Interacción (CRÍTICO)

- `touch-target-size` — Mínimo 44×44pt (Apple) / 48×48dp (Material)
- `touch-spacing` — Mínimo 8px/8dp entre targets táctiles
- `hover-vs-tap` — Usar click/tap para interacciones primarias; no depender solo de hover
- `loading-buttons` — Deshabilitar botón durante async; mostrar spinner
- `error-feedback` — Mensajes de error claros cerca del problema
- `cursor-pointer` — cursor-pointer en elementos clickables (Web)
- `tap-delay` — Usar `touch-action: manipulation` para eliminar delay 300ms
- `standard-gestures` — Gestos estándar de plataforma consistentes
- `safe-area-awareness` — Targets primarios lejos de notch y gesture bar
- `swipe-clarity` — Acciones swipe con affordance visible (chevron, label)

### 3. Performance (ALTO)

- `image-optimization` — WebP/AVIF, responsive images (srcset), lazy load
- `image-dimension` — `width` y `height` explícitos para prevenir CLS
- `font-loading` — `font-display: swap/optional`; reservar espacio
- `font-preload` — Preload solo fuentes críticas
- `critical-css` — Priorizar CSS above-the-fold
- `lazy-loading` — Lazy load componentes non-hero con dynamic import
- `bundle-splitting` — Split por ruta/feature (React Suspense / Next.js dynamic)
- `content-jumping` — Reservar espacio para contenido async (CLS)
- `virtualize-lists` — Virtualizar listas >50 items (`virtua`, `content-visibility: auto`)
- `progressive-loading` — Skeletons/shimmer en operaciones >1s
- `debounce-throttle` — Debounce/throttle en eventos de alta frecuencia

### 4. Selección de Estilo (ALTO)

- `style-match` — Estilo coherente con tipo de producto
- `consistency` — Mismo estilo en todas las páginas
- `no-emoji-icons` — SVG icons (Heroicons, Lucide), no emojis
- `color-palette-from-product` — Paleta desde el tipo de producto/industria
- `effects-match-style` — Sombras, blur, radius alineados con el estilo elegido
- `platform-adaptive` — Respetar idioms de plataforma (iOS HIG vs Material)
- `elevation-consistent` — Escala de elevación/sombra consistente
- `dark-mode-pairing` — Diseñar variantes light/dark juntas
- `icon-style-consistent` — Un solo set de iconos (stroke width, corner radius)
- `primary-action` — Solo un CTA primario por pantalla

**Estilos disponibles:** glassmorphism, claymorphism, minimalism, brutalism, neumorphism, bento grid, dark mode, responsive, skeuomorphism, flat design, + 57 más

### 5. Layout & Responsive (ALTO)

- `viewport-meta` — `width=device-width initial-scale=1` (nunca deshabilitar zoom)
- `mobile-first` — Diseño mobile-first, escalar hacia tablet y desktop
- `breakpoint-consistency` — Breakpoints sistemáticos: 375 / 768 / 1024 / 1440
- `readable-font-size` — Mínimo 16px cuerpo en mobile (evita auto-zoom iOS)
- `line-length-control` — Mobile 35–60 chars/línea; desktop 60–75 chars
- `horizontal-scroll` — Sin scroll horizontal en mobile
- `spacing-scale` — Sistema de espaciado incremental de 4pt/8dp
- `container-width` — max-width consistente en desktop (max-w-6xl / 7xl)
- `z-index-management` — Escala de z-index definida (0 / 10 / 20 / 40 / 100 / 1000)
- `viewport-units` — Preferir `min-h-dvh` sobre `100vh` en mobile
- `content-priority` — Contenido core primero en mobile
- `visual-hierarchy` — Jerarquía por tamaño, espaciado, contraste — no solo color

### 6. Tipografía & Color (MEDIO)

- `line-height` — 1.5–1.75 para body text
- `font-pairing` — Personalidades heading/body complementarias
- `font-scale` — Escala tipográfica consistente: 12 / 14 / 16 / 18 / 24 / 32
- `contrast-readability` — Texto oscuro sobre fondo claro (slate-900 sobre blanco)
- `weight-hierarchy` — Headings bold (600–700), body regular (400), labels medium (500)
- `color-semantic` — Tokens semánticos (primary, secondary, error, surface) — no hex raw en componentes
- `color-dark-mode` — Dark mode con variantes desaturadas/más claras, no colores invertidos
- `color-accessible-pairs` — Pares foreground/background ≥4.5:1 (AA) o 7:1 (AAA)
- `color-not-decorative-only` — Color funcional (error rojo, success verde) + icono/texto
- `truncation-strategy` — Preferir wrap sobre truncar; tooltip con texto completo si trunca
- `number-tabular` — `font-variant-numeric: tabular-nums` para columnas de datos, precios
- `whitespace-balance` — Whitespace intencional para agrupar/separar; evitar clutter

**Font pairings recomendadas por estilo:**
- SaaS/Dashboard: Inter + DM Sans, Plus Jakarta Sans + Inter
- Luxury/Premium: Playfair Display + Lato, Cormorant + Libre Baskerville
- Startup/Tech: Syne + Inter, Space Grotesk + DM Sans
- Editorial/Blog: Merriweather + Source Sans 3, Lora + Open Sans
- Minimal/Clean: DM Sans + DM Sans, Nunito Sans + Nunito Sans

### 7. Animación (MEDIO)

- `duration-timing` — 150–300ms micro-interacciones; transiciones complejas ≤400ms
- `transform-performance` — Solo `transform`/`opacity`; nunca `width`/`height`/`top`/`left`
- `loading-states` — Skeleton o progress indicator cuando carga >300ms
- `easing` — ease-out al entrar, ease-in al salir; nunca linear en UI
- `motion-meaning` — Cada animación expresa causa-efecto, no decoración
- `state-transition` — Cambios de estado animados, no instantáneos
- `exit-faster-than-enter` — Animaciones de salida ~60–70% de la duración de entrada
- `stagger-sequence` — Entrada de lista: 30–50ms de delay por item
- `interruptible` — Animaciones interrumpibles por input del usuario
- `no-blocking-animation` — Nunca bloquear input durante animación
- `layout-shift-avoid` — Animaciones sin reflow ni CLS; usar transform para posición

### 8. Formularios & Feedback (MEDIO)

- `input-labels` — Label visible por input (no solo placeholder)
- `error-placement` — Error debajo del campo relacionado
- `submit-feedback` — Estado loading → success/error en submit
- `required-indicators` — Marcar campos requeridos (asterisco)
- `empty-states` — Mensaje útil + acción cuando no hay contenido
- `toast-dismiss` — Auto-dismiss toasts en 3–5s con `aria-live="polite"`
- `confirmation-dialogs` — Confirmar antes de acciones destructivas
- `progressive-disclosure` — Opciones complejas progresivamente; no abrumar al inicio
- `inline-validation` — Validar on blur (no on keystroke); error tras terminar input
- `input-type-keyboard` — `type="email"` / `tel` / `number` para teclado móvil correcto
- `password-toggle` — Toggle show/hide en campos de contraseña
- `autofill-support` — `autocomplete` / `textContentType` para autofill del sistema
- `error-recovery` — Mensajes de error con ruta de recuperación (retry, edit, help)
- `focus-management` — Tras error en submit, auto-focus en primer campo inválido
- `error-summary` — Múltiples errores: resumen arriba con anchors a cada campo

### 9. Navegación (ALTO)

- `bottom-nav-limit` — Navegación inferior máx 5 items; con icono + label
- `drawer-usage` — Drawer/sidebar para nav secundaria, no acciones primarias
- `back-behavior` — Navegación atrás predecible y consistente; preservar scroll/estado
- `deep-linking` — Todas las pantallas clave accesibles por deep link / URL
- `nav-label-icon` — Items de nav con icono Y label de texto; icono solo perjudica
- `nav-state-active` — Ubicación actual visualmente destacada en nav
- `modal-escape` — Modales con cierre claro; swipe-down para cerrar en mobile
- `state-preservation` — Volver atrás restaura scroll, filtros y estado previo
- `nav-hierarchy` — Nav primaria (tabs/bottom) vs secundaria (drawer/settings) separadas
- `adaptive-navigation` — ≥1024px: sidebar; pequeño: bottom/top nav
- `focus-on-route-change` — Tras transición, mover focus a región de contenido principal

### 10. Charts & Datos (BAJO)

- `chart-type` — Tipo correcto: tendencia→línea, comparación→barra, proporción→pie/donut
- `color-guidance` — Paletas accesibles; evitar solo rojo/verde para daltónicos
- `data-table` — Alternativa en tabla para accesibilidad; chart solo no es screen-reader friendly
- `legend-visible` — Leyenda siempre visible cerca del gráfico
- `tooltip-on-interact` — Tooltips en hover (web) o tap (mobile) con valores exactos
- `axis-labels` — Ejes etiquetados con unidades; sin labels rotados en mobile
- `responsive-chart` — Gráficos que reflow en pantallas pequeñas
- `empty-data-state` — Empty state con mensaje útil cuando no hay datos
- `large-dataset` — >1000 puntos: agregar/samplear + drill-down para detalle
- `number-formatting` — `Intl.NumberFormat` para números, fechas, monedas en ejes
- `no-pie-overuse` — No pie/donut para >5 categorías; usar barra

---

## Paletas de color por tipo de producto

| Producto | Paleta base | Acento |
|----------|-------------|--------|
| **SaaS/B2B** | #0F172A / #F8FAFC | #6366F1 o #3B82F6 |
| **E-commerce** | #FFFFFF / #F9FAFB | #EC4899 o #F59E0B |
| **Fintech** | #0A0E1A / #F0F4FF | #22D3EE o #818CF8 |
| **Healthcare** | #F0FDF4 / #FFFFFF | #10B981 o #0EA5E9 |
| **Dashboard/Analytics** | #0F172A / #1E293B | #8B5CF6 o #06B6D4 |
| **Portfolio** | #09090B / #FAFAFA | acento de marca personalizado |
| **Landing/Marketing** | #FFFFFF / #F8FAFC | bold: #F97316 o #8B5CF6 |
| **Blog/Editorial** | #FFFBF7 / #FFFFFF | #D97706 o #7C3AED |
| **Startup/App** | según audiencia | neon/vibrant si youth, neutral si enterprise |

---

## Flujo de trabajo recomendado

### Paso 1: Analizar requisitos
Extraer del contexto del usuario:
- **Tipo de producto**: SaaS, e-commerce, portfolio, dashboard, mobile app...
- **Audiencia**: consumidor final, empresas, desarrolladores, adolescentes...
- **Keywords de estilo**: minimal, dark, vibrant, corporate, playful...
- **Stack**: Next.js, React, Vue, SwiftUI, React Native...

### Paso 2: Generar sistema de diseño
Con la info del Paso 1, proponer:
1. **Estilo visual** — elegir de los 67 estilos disponibles
2. **Paleta de color** — tokens semánticos (base, surface, text, muted, accent)
3. **Tipografía** — pareja display + body con justificación
4. **Espaciado** — escala 4pt/8dp
5. **Elemento firma** — el detalle único que hace memorable el diseño

### Paso 3: Implementar y validar
- Aplicar Quick Reference §1–§3 (CRÍTICO + ALTO) como revisión final
- Testear en 375px (teléfono pequeño) y orientación landscape
- Verificar comportamiento con `prefers-reduced-motion` activo
- Comprobar contraste dark mode independientemente

---

## Checklist pre-entrega

### Calidad visual
- [ ] Sin emojis como iconos (usar SVG: Heroicons, Lucide, Phosphor)
- [ ] Familia de iconos consistente (stroke width, corner radius)
- [ ] Tokens de color semánticos (no hex hardcoded en componentes)
- [ ] Pressed-states sin shift de layout

### Interacción
- [ ] Todos los elementos táctiles con feedback visual
- [ ] Touch targets ≥44×44pt
- [ ] Timing de micro-interacciones: 150–300ms
- [ ] Estados disabled visualmente claros y no interactivos
- [ ] Orden de focus de screen reader coincide con orden visual

### Light/Dark Mode
- [ ] Contraste texto primario ≥4.5:1 en ambos modos
- [ ] Contraste texto secundario ≥3:1 en ambos modos
- [ ] Separadores/bordes distinguibles en ambos temas
- [ ] Ambos temas testados antes de entregar

### Layout
- [ ] Safe areas respetadas en headers, tab bars y CTA bars
- [ ] Contenido scroll no oculto por barras fijas
- [ ] Verificado en teléfono pequeño, grande y tablet (portrait + landscape)
- [ ] Ritmo de espaciado 4/8dp mantenido
- [ ] Texto largo legible en dispositivos grandes (sin párrafos edge-to-edge)

### Accesibilidad
- [ ] Imágenes/iconos con aria-label o alt
- [ ] Formularios con labels, hints y mensajes de error claros
- [ ] Color no es el único indicador
- [ ] `prefers-reduced-motion` soportado sin rotura de layout
- [ ] Roles/estados de accesibilidad correctos (selected, disabled, expanded)

---

## Anti-patrones frecuentes

| Anti-patrón | Corrección |
|-------------|------------|
| Emojis como iconos de UI | SVG del mismo set (Lucide, Heroicons) |
| `transition: all` | Listar propiedades explícitas |
| Hex hardcoded en componentes | Tokens CSS variables (`--color-primary`) |
| Placeholder como único label | Label visible + placeholder como ejemplo |
| Solo color para indicar error | Color + icono + texto descriptivo |
| `outline: none` sin reemplazo | `:focus-visible` con ring alternativo |
| Imágenes sin dimensiones | `width` + `height` explícitos siempre |
| Listas >50 sin virtualizar | `virtua` o `content-visibility: auto` |
| Gradiente morado genérico de IA | Paleta específica del tipo de producto |
| `user-scalable=no` | Nunca deshabilitar zoom |
| `onPaste` + `preventDefault` | Permitir siempre el paste |
| Fechas/números hardcoded | `Intl.DateTimeFormat` / `Intl.NumberFormat` |
