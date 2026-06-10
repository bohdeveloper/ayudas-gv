#!/usr/bin/env node
// Genera el HTML de reporte semanal a partir de los markdowns diarios.
// Ejecutar: node scripts/generar-reporte-semanal.js

const fs   = require('fs');
const path = require('path');

// ─── Helpers de fecha ─────────────────────────────────────────────────────────

function getMondayOf(date) {
  const d   = new Date(date);
  const day = d.getDay(); // 0=Dom
  d.setDate(d.getDate() - (day === 0 ? 6 : day - 1));
  d.setHours(0, 0, 0, 0);
  return d;
}

function addDays(date, n) {
  const d = new Date(date);
  d.setDate(d.getDate() + n);
  return d;
}

function toISO(date) {
  const y  = date.getFullYear();
  const m  = String(date.getMonth() + 1).padStart(2, '0');
  const d  = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

function toES(date) {
  return date.toLocaleDateString('es-ES', { day: '2-digit', month: 'long', year: 'numeric' });
}

const DAY_SLUG = ['domingo', 'lunes', 'martes', 'miercoles', 'jueves', 'viernes', 'sabado'];
const DAY_ES   = ['Domingo', 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado'];

// ─── Markdown → HTML (sin dependencias externas) ──────────────────────────────

function inline(text) {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    .replace(/\*([^*]+)\*/g, '<em>$1</em>');
}

function mdToHtml(md) {
  const lines  = md.split('\n');
  let   html   = '';
  let   inList = false;

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];

    if (/^### /.test(line)) {
      if (inList) { html += '</ul>'; inList = false; }
      html += `<h3>${inline(line.slice(4))}</h3>`;
    } else if (/^## /.test(line)) {
      if (inList) { html += '</ul>'; inList = false; }
      html += `<h2>${inline(line.slice(3))}</h2>`;
    } else if (/^# /.test(line)) {
      if (inList) { html += '</ul>'; inList = false; }
      // Omitimos el H1 del markdown (usamos el título del día como cabecera)
    } else if (/^---+$/.test(line.trim())) {
      if (inList) { html += '</ul>'; inList = false; }
      // Omitimos el HR
    } else if (/^  - /.test(line)) {
      if (!inList) { html += '<ul>'; inList = true; }
      html += `<li class="sub">${inline(line.slice(4))}</li>`;
    } else if (/^- /.test(line)) {
      if (!inList) { html += '<ul>'; inList = true; }
      html += `<li>${inline(line.slice(2))}</li>`;
    } else if (line.trim() === '') {
      if (inList) { html += '</ul>'; inList = false; }
    } else {
      if (inList) { html += '</ul>'; inList = false; }
      const trimmed = line.trim();
      if (/^\*.*\*$/.test(trimmed)) {
        html += `<p class="hito">${inline(trimmed)}</p>`;
      } else {
        html += `<p>${inline(trimmed)}</p>`;
      }
    }
  }
  if (inList) html += '</ul>';
  return html;
}

// ─── Lectura de reportes ──────────────────────────────────────────────────────

const ROOT       = path.join(__dirname, '..');
const reportesDir = path.join(ROOT, 'docs', 'reportes');
const semanasDir  = path.join(reportesDir, 'semanas');

if (!fs.existsSync(semanasDir)) fs.mkdirSync(semanasDir, { recursive: true });

const monday = getMondayOf(new Date());
const days   = [];

for (let i = 0; i < 5; i++) { // Lun–Vie
  const d    = addDays(monday, i);
  const slug = DAY_SLUG[d.getDay()];
  const iso  = toISO(d);
  const file = path.join(reportesDir, `${iso}-${slug}.md`);

  if (fs.existsSync(file)) {
    days.push({
      iso,
      nombre: DAY_ES[d.getDay()],
      html:   mdToHtml(fs.readFileSync(file, 'utf8'))
    });
  }
}

if (days.length === 0) {
  console.log('ℹ️  No se encontraron reportes para esta semana en docs/reportes/');
  process.exit(0);
}

const friday     = addDays(monday, 4);
const weekLabel  = `Semana ${toES(monday)} – ${toES(friday)}`;
const outputPath = path.join(semanasDir, `semana-${toISO(monday)}.html`);

fs.writeFileSync(outputPath, buildHtml(weekLabel, days), 'utf8');
console.log(`✅  Reporte generado: ${outputPath}`);

// ─── Plantilla HTML ───────────────────────────────────────────────────────────

function buildHtml(weekLabel, days) {
  const dayCards = days.map((d, idx) => `
    <section class="day-card" id="day-${idx}">
      <div class="day-header">
        <span class="day-name">${d.nombre}</span>
        <span class="day-date">${d.iso}</span>
      </div>
      <div class="day-content">${d.html}</div>
      <div class="evidence-section">
        <p class="evidence-label">📸 Evidencias</p>
        <div class="drop-zone" data-day="${idx}" onclick="triggerPicker(${idx})">
          <div class="drop-hint">Arrastra pantallazos aquí<br>o haz clic para seleccionar</div>
        </div>
        <input type="file" id="picker-${idx}" accept="image/*" multiple style="display:none"
               onchange="handleFiles(this.files, ${idx})">
        <div class="images-grid" id="grid-${idx}"></div>
      </div>
    </section>`).join('\n');

  return `<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Reporte semanal — ${weekLabel}</title>
<style>
  :root {
    --bg: #0f172a;
    --surface: #1e293b;
    --border: #334155;
    --accent: #38bdf8;
    --accent2: #818cf8;
    --text: #e2e8f0;
    --muted: #94a3b8;
    --success: #4ade80;
    --warn: #fbbf24;
    --radius: 12px;
  }
  *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
  body {
    background: var(--bg);
    color: var(--text);
    font-family: 'Segoe UI', system-ui, sans-serif;
    font-size: 15px;
    line-height: 1.6;
    padding: 2rem 1rem;
  }
  .container { max-width: 900px; margin: 0 auto; }

  /* Header */
  header {
    display: flex; align-items: center; justify-content: space-between;
    margin-bottom: 2.5rem; flex-wrap: wrap; gap: 1rem;
  }
  .header-left h1 { font-size: 1.6rem; color: var(--accent); font-weight: 700; }
  .header-left p  { color: var(--muted); font-size: 0.9rem; margin-top: 0.25rem; }
  .btn-save {
    background: var(--accent); color: #0f172a;
    border: none; border-radius: 8px; padding: 0.55rem 1.2rem;
    font-weight: 700; font-size: 0.9rem; cursor: pointer;
    transition: opacity .2s;
  }
  .btn-save:hover { opacity: 0.85; }

  /* Day cards */
  .day-card {
    background: var(--surface);
    border: 1px solid var(--border);
    border-radius: var(--radius);
    margin-bottom: 1.75rem;
    overflow: hidden;
  }
  .day-header {
    display: flex; align-items: center; justify-content: space-between;
    background: var(--border); padding: 0.75rem 1.25rem;
  }
  .day-name  { font-weight: 700; font-size: 1.05rem; color: var(--accent2); }
  .day-date  { font-size: 0.82rem; color: var(--muted); }
  .day-content { padding: 1.25rem 1.5rem; }

  /* Markdown rendering */
  .day-content h2 {
    font-size: 0.95rem; text-transform: uppercase; letter-spacing: .05em;
    color: var(--success); margin: 1.1rem 0 0.4rem;
    border-bottom: 1px solid var(--border); padding-bottom: 0.3rem;
  }
  .day-content h3 { color: var(--warn); font-size: 0.9rem; margin: 0.9rem 0 0.3rem; }
  .day-content ul { padding-left: 1.5rem; margin: 0.3rem 0 0.6rem; }
  .day-content li { margin: 0.2rem 0; }
  .day-content li.sub { list-style: circle; color: var(--muted); font-size: 0.9rem; }
  .day-content code {
    background: #0f2240; color: #7dd3fc;
    padding: 0.1em 0.4em; border-radius: 4px; font-size: 0.88em;
    font-family: 'Cascadia Code', 'Fira Code', monospace;
  }
  .day-content p { margin: 0.3rem 0; color: var(--text); }
  .day-content p.hito {
    color: var(--accent); font-style: italic;
    border-left: 3px solid var(--accent); padding-left: 0.75rem;
    margin-top: 0.8rem;
  }

  /* Evidence section */
  .evidence-section { padding: 1rem 1.5rem 1.25rem; border-top: 1px solid var(--border); }
  .evidence-label { font-size: 0.85rem; color: var(--muted); font-weight: 600; margin-bottom: 0.6rem; }
  .drop-zone {
    border: 2px dashed var(--border);
    border-radius: 8px; padding: 1.2rem;
    text-align: center; cursor: pointer;
    transition: border-color .2s, background .2s;
  }
  .drop-zone.drag-over { border-color: var(--accent); background: rgba(56,189,248,.06); }
  .drop-hint { color: var(--muted); font-size: 0.85rem; pointer-events: none; }
  .images-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 0.75rem; margin-top: 0.75rem;
  }
  .img-wrapper {
    position: relative; border-radius: 8px; overflow: hidden;
    border: 1px solid var(--border);
  }
  .img-wrapper img { width: 100%; display: block; }
  .img-caption {
    font-size: 0.78rem; color: var(--muted);
    padding: 0.3rem 0.5rem; background: rgba(0,0,0,.4);
  }
  .img-wrapper .btn-remove {
    position: absolute; top: 6px; right: 6px;
    background: rgba(0,0,0,.65); color: #fff;
    border: none; border-radius: 50%; width: 24px; height: 24px;
    cursor: pointer; font-size: 14px; line-height: 1;
    display: flex; align-items: center; justify-content: center;
  }
  .img-wrapper .btn-remove:hover { background: #ef4444; }

  /* Toast */
  #toast {
    position: fixed; bottom: 1.5rem; right: 1.5rem;
    background: var(--success); color: #0f172a;
    padding: 0.6rem 1.1rem; border-radius: 8px;
    font-weight: 700; font-size: 0.9rem;
    opacity: 0; transition: opacity .3s; pointer-events: none;
  }
  #toast.show { opacity: 1; }
</style>
</head>
<body>
<div class="container">
  <header>
    <div class="header-left">
      <h1>Reporte semanal</h1>
      <p>${weekLabel}</p>
    </div>
    <button class="btn-save" onclick="guardarHtml()">💾 Guardar HTML</button>
  </header>

  ${dayCards}
</div>

<div id="toast">✅ HTML guardado</div>

<script>
  // ── Drag & Drop ────────────────────────────────────────────────────────────
  document.querySelectorAll('.drop-zone').forEach(zone => {
    zone.addEventListener('dragover', e => { e.preventDefault(); zone.classList.add('drag-over'); });
    zone.addEventListener('dragleave', () => zone.classList.remove('drag-over'));
    zone.addEventListener('drop', e => {
      e.preventDefault();
      zone.classList.remove('drag-over');
      const dayIdx = zone.dataset.day;
      handleFiles(e.dataTransfer.files, dayIdx);
    });
  });

  function triggerPicker(dayIdx) {
    document.getElementById('picker-' + dayIdx).click();
  }

  function handleFiles(files, dayIdx) {
    const grid = document.getElementById('grid-' + dayIdx);
    Array.from(files).forEach(file => {
      if (!file.type.startsWith('image/')) return;
      const reader = new FileReader();
      reader.onload = e => {
        const wrapper = document.createElement('div');
        wrapper.className = 'img-wrapper';
        wrapper.innerHTML =
          '<img src="' + e.target.result + '" alt="' + escHtml(file.name) + '">' +
          '<div class="img-caption">' + escHtml(file.name) + '</div>' +
          '<button class="btn-remove" title="Eliminar" onclick="this.closest(\\'.img-wrapper\\').remove()">×</button>';
        grid.appendChild(wrapper);
      };
      reader.readAsDataURL(file);
    });
  }

  function escHtml(s) {
    return s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
  }

  // ── Guardar HTML ───────────────────────────────────────────────────────────
  function guardarHtml() {
    // Ocultamos los file-pickers antes de serializar
    document.querySelectorAll('input[type=file]').forEach(el => el.value = '');

    const html = '<!DOCTYPE html>\\n' + document.documentElement.outerHTML;
    const blob = new Blob([html], { type: 'text/html;charset=utf-8' });
    const a    = document.createElement('a');
    a.href     = URL.createObjectURL(blob);
    a.download = location.pathname.split('/').pop() || 'reporte-semanal.html';
    a.click();
    URL.revokeObjectURL(a.href);

    const toast = document.getElementById('toast');
    toast.classList.add('show');
    setTimeout(() => toast.classList.remove('show'), 2500);
  }
</script>
</body>
</html>`;
}
