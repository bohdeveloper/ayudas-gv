#!/usr/bin/env node
// Genera el HTML de reporte semanal a partir de los markdowns diarios.
// Ejecutar: node scripts/generar-reporte-semanal.js

const fs   = require('fs');
const path = require('path');

// ─── Helpers de fecha ─────────────────────────────────────────────────────────

function getMondayOf(date) {
  const d   = new Date(date);
  const day = d.getDay();
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
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

function toES(date) {
  return date.toLocaleDateString('es-ES', { day: '2-digit', month: 'long', year: 'numeric' });
}

const DAY_SLUG = ['domingo', 'lunes', 'martes', 'miercoles', 'jueves', 'viernes', 'sabado'];
const DAY_ES   = ['Domingo', 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado'];

// ─── Parseo de Markdown → HTML ────────────────────────────────────────────────

function esc(s) {
  return s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}

function inline(text) {
  return esc(text)
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    .replace(/\*([^*]+)\*/g, '<em>$1</em>');
}

function mdToHtml(md) {
  // Elimina el bloque de evidencias antes de renderizar
  const clean  = md.replace(/<!--\s*evidencias[\s\S]*?-->/g, '').trim();
  const lines  = clean.split('\n');
  let   html   = '';
  let   inList = false;

  for (const line of lines) {
    if (/^### /.test(line)) {
      if (inList) { html += '</ul>'; inList = false; }
      html += `<h3>${inline(line.slice(4))}</h3>`;
    } else if (/^## /.test(line)) {
      if (inList) { html += '</ul>'; inList = false; }
      html += `<h2>${inline(line.slice(3))}</h2>`;
    } else if (/^# /.test(line)) {
      if (inList) { html += '</ul>'; inList = false; }
      // título del día — lo omitimos, se muestra en el header de la tarjeta
    } else if (/^---+$/.test(line.trim())) {
      if (inList) { html += '</ul>'; inList = false; }
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
      const t = line.trim();
      if (/^\*.*\*$/.test(t)) {
        html += `<p class="hito">${inline(t)}</p>`;
      } else {
        html += `<p>${inline(t)}</p>`;
      }
    }
  }
  if (inList) html += '</ul>';
  return html;
}

function parseEvidencias(md) {
  const match = md.match(/<!--\s*evidencias\s*([\s\S]*?)-->/);
  if (!match) return [];
  return match[1]
    .trim()
    .split('\n')
    .map(l => l.replace(/^\d+\.\s*/, '').trim())
    .filter(Boolean);
}

// ─── Lectura de reportes ──────────────────────────────────────────────────────

const ROOT        = path.join(__dirname, '..');
const reportesDir = path.join(ROOT, 'docs', 'reportes');
const semanasDir  = path.join(reportesDir, 'semanas');

if (!fs.existsSync(semanasDir)) fs.mkdirSync(semanasDir, { recursive: true });

const monday = getMondayOf(new Date());
const days   = [];

for (let i = 0; i < 5; i++) {
  const d    = addDays(monday, i);
  const slug = DAY_SLUG[d.getDay()];
  const iso  = toISO(d);
  const file = path.join(reportesDir, `${iso}-${slug}.md`);

  if (fs.existsSync(file)) {
    const raw = fs.readFileSync(file, 'utf8');
    days.push({
      iso,
      nombre:     DAY_ES[d.getDay()],
      html:       mdToHtml(raw),
      evidencias: parseEvidencias(raw)
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

function escAttr(s) {
  return s.replace(/&/g,'&amp;').replace(/"/g,'&quot;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}

function buildHtml(weekLabel, days) {
  const dayCards = days.map((d, di) => {
    const slots = d.evidencias.map((label, si) =>
      buildSlot(di, si, label)
    ).join('\n');

    return `
    <section class="day-card" id="day-${di}">
      <div class="day-header">
        <span class="day-name">${d.nombre}</span>
        <span class="day-date">${d.iso}</span>
      </div>
      <div class="day-content">${d.html}</div>
      <div class="ev-section">
        <div class="ev-title">📸 Evidencias</div>
        <div class="slots-list" id="slots-${di}">${slots}</div>
        <button class="btn-add-slot" onclick="addSlot(${di})">➕ Añadir evidencia</button>
      </div>
    </section>`;
  }).join('\n');

  return `<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1.0">
<title>Reporte semanal — ${weekLabel}</title>
<style>
  :root {
    --bg:#0f172a; --surface:#1e293b; --border:#334155;
    --accent:#38bdf8; --accent2:#818cf8;
    --text:#e2e8f0; --muted:#94a3b8;
    --ok:#4ade80; --warn:#fbbf24; --danger:#f87171;
    --r:12px;
  }
  *,*::before,*::after{box-sizing:border-box;margin:0;padding:0}
  body{background:var(--bg);color:var(--text);font-family:'Segoe UI',system-ui,sans-serif;
       font-size:15px;line-height:1.65;padding:2rem 1rem}
  .container{max-width:920px;margin:0 auto}

  /* ── Header ── */
  header{display:flex;align-items:center;justify-content:space-between;
         margin-bottom:2.5rem;flex-wrap:wrap;gap:1rem}
  .header-left h1{font-size:1.65rem;color:var(--accent);font-weight:700}
  .header-left p{color:var(--muted);font-size:.9rem;margin-top:.2rem}
  .btn-save{background:var(--accent);color:#0f172a;border:none;border-radius:8px;
            padding:.55rem 1.3rem;font-weight:700;font-size:.9rem;cursor:pointer;transition:opacity .2s}
  .btn-save:hover{opacity:.85}

  /* ── Day card ── */
  .day-card{background:var(--surface);border:1px solid var(--border);
            border-radius:var(--r);margin-bottom:2rem;overflow:hidden}
  .day-header{display:flex;align-items:center;justify-content:space-between;
              background:var(--border);padding:.75rem 1.25rem}
  .day-name{font-weight:700;font-size:1.05rem;color:var(--accent2)}
  .day-date{font-size:.82rem;color:var(--muted)}
  .day-content{padding:1.25rem 1.5rem}

  /* ── Markdown ── */
  .day-content h2{font-size:.9rem;text-transform:uppercase;letter-spacing:.06em;
                  color:var(--ok);margin:1.1rem 0 .4rem;border-bottom:1px solid var(--border);padding-bottom:.3rem}
  .day-content h3{color:var(--warn);font-size:.9rem;margin:.9rem 0 .3rem}
  .day-content ul{padding-left:1.5rem;margin:.3rem 0 .6rem}
  .day-content li{margin:.2rem 0}
  .day-content li.sub{list-style:circle;color:var(--muted);font-size:.88rem}
  .day-content code{background:#0f2240;color:#7dd3fc;padding:.1em .4em;border-radius:4px;
                    font-size:.85em;font-family:'Cascadia Code','Fira Code',monospace}
  .day-content p{margin:.3rem 0}
  .day-content p.hito{color:var(--accent);font-style:italic;
                       border-left:3px solid var(--accent);padding-left:.75rem;margin-top:.8rem}

  /* ── Evidence section ── */
  .ev-section{border-top:1px solid var(--border);padding:1rem 1.5rem 1.25rem}
  .ev-title{font-size:.82rem;font-weight:700;color:var(--muted);text-transform:uppercase;
            letter-spacing:.06em;margin-bottom:.9rem}

  /* ── Slot ── */
  .ev-slot{border:1px solid var(--border);border-radius:10px;
           padding:1rem;margin-bottom:.85rem;background:rgba(255,255,255,.02)}
  .slot-header{display:flex;align-items:center;gap:.5rem;margin-bottom:.65rem}
  .slot-label{flex:1;background:rgba(255,255,255,.05);border:1px solid var(--border);
              border-radius:6px;padding:.4rem .7rem;color:var(--text);font-size:.88rem;font-weight:600}
  .slot-label:focus{outline:none;border-color:var(--accent)}
  .btn-rm-slot{background:none;border:1px solid var(--border);border-radius:6px;
               color:var(--muted);width:28px;height:28px;cursor:pointer;font-size:15px;
               display:flex;align-items:center;justify-content:center;flex-shrink:0}
  .btn-rm-slot:hover{background:var(--danger);color:#fff;border-color:var(--danger)}

  /* ── Drop zone ── */
  .drop-zone{border:2px dashed var(--border);border-radius:8px;padding:1rem;
             text-align:center;cursor:pointer;transition:border-color .2s,background .2s}
  .drop-zone.drag-over{border-color:var(--accent);background:rgba(56,189,248,.07)}
  .drop-zone.has-image{display:none}
  .drop-hint{color:var(--muted);font-size:.82rem;pointer-events:none}

  /* ── Image preview ── */
  .slot-image{display:none;position:relative;border-radius:8px;overflow:hidden;
              border:1px solid var(--border)}
  .slot-image.visible{display:block}
  .slot-image img{width:100%;display:block;border-radius:8px 8px 0 0}
  .img-desc{width:100%;background:rgba(0,0,0,.35);border:none;border-top:1px solid var(--border);
            color:var(--muted);font-size:.82rem;padding:.4rem .7rem;font-family:inherit;resize:none}
  .img-desc:focus{outline:none;color:var(--text)}
  .btn-rm-img{position:absolute;top:6px;right:6px;background:rgba(0,0,0,.65);color:#fff;
              border:none;border-radius:50%;width:26px;height:26px;cursor:pointer;font-size:14px;
              display:flex;align-items:center;justify-content:center}
  .btn-rm-img:hover{background:var(--danger)}

  /* ── Add slot button ── */
  .btn-add-slot{background:none;border:1px dashed var(--border);border-radius:8px;
                color:var(--muted);padding:.45rem 1rem;font-size:.83rem;cursor:pointer;
                transition:border-color .2s,color .2s;margin-top:.15rem}
  .btn-add-slot:hover{border-color:var(--accent2);color:var(--accent2)}

  /* ── Toast ── */
  #toast{position:fixed;bottom:1.5rem;right:1.5rem;background:var(--ok);color:#0f172a;
         padding:.6rem 1.2rem;border-radius:8px;font-weight:700;font-size:.9rem;
         opacity:0;transition:opacity .3s;pointer-events:none}
  #toast.show{opacity:1}
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
// ── Contadores de slots por día (para IDs únicos al añadir nuevos) ─────────
const slotCounters = {};
document.querySelectorAll('.ev-slot').forEach(slot => {
  const [, di, si] = slot.id.match(/slot-(\\d+)-(\\d+)/);
  slotCounters[di] = Math.max(slotCounters[di] || 0, +si + 1);
});

// ── Drag & drop en drop zones ───────────────────────────────────────────────
document.addEventListener('dragover', e => {
  const zone = e.target.closest('.drop-zone');
  if (zone) { e.preventDefault(); zone.classList.add('drag-over'); }
});
document.addEventListener('dragleave', e => {
  const zone = e.target.closest('.drop-zone');
  if (zone) zone.classList.remove('drag-over');
});
document.addEventListener('drop', e => {
  const zone = e.target.closest('.drop-zone');
  if (!zone) return;
  e.preventDefault();
  zone.classList.remove('drag-over');
  const [di, si] = zone.dataset.slot.split('-');
  if (e.dataTransfer.files[0]) loadImage(e.dataTransfer.files[0], di, si);
});

// ── Click en drop zone → abrir picker ──────────────────────────────────────
document.addEventListener('click', e => {
  const zone = e.target.closest('.drop-zone');
  if (zone) {
    const [di, si] = zone.dataset.slot.split('-');
    document.getElementById('picker-' + di + '-' + si).click();
  }
});

function handlePicker(input, di, si) {
  if (input.files[0]) loadImage(input.files[0], di, si);
  input.value = '';
}

function loadImage(file, di, si) {
  if (!file.type.startsWith('image/')) return;
  const reader = new FileReader();
  reader.onload = e => setSlotImage(di, si, e.target.result);
  reader.readAsDataURL(file);
}

function setSlotImage(di, si, src) {
  const zone  = document.querySelector('.drop-zone[data-slot="' + di + '-' + si + '"]');
  const imgEl = document.getElementById('slotimg-' + di + '-' + si);
  const img   = imgEl.querySelector('img');
  img.src     = src;
  zone.classList.add('has-image');
  imgEl.classList.add('visible');
}

function removeImage(di, si) {
  const zone  = document.querySelector('.drop-zone[data-slot="' + di + '-' + si + '"]');
  const imgEl = document.getElementById('slotimg-' + di + '-' + si);
  imgEl.querySelector('img').src = '';
  imgEl.querySelector('.img-desc').value = '';
  zone.classList.remove('has-image');
  imgEl.classList.remove('visible');
}

// ── Añadir nuevo slot ───────────────────────────────────────────────────────
function addSlot(di) {
  const si   = slotCounters[di] = (slotCounters[di] || 0) + 1;
  const list = document.getElementById('slots-' + di);
  const div  = document.createElement('div');
  div.innerHTML = buildSlotHTML(di, si, '');
  list.appendChild(div.firstElementChild);
}

function buildSlotHTML(di, si, label) {
  return '<div class="ev-slot" id="slot-' + di + '-' + si + '">' +
    '<div class="slot-header">' +
      '<input type="text" class="slot-label" value="' + label.replace(/"/g,'&quot;') + '" placeholder="Descripción de la evidencia...">' +
      '<button class="btn-rm-slot" title="Eliminar slot" onclick="removeSlot(' + di + ',' + si + ')">×</button>' +
    '</div>' +
    '<div class="drop-zone" data-slot="' + di + '-' + si + '">' +
      '<div class="drop-hint">Arrastra el pantallazo aquí o haz clic para seleccionar</div>' +
    '</div>' +
    '<input type="file" id="picker-' + di + '-' + si + '" accept="image/*" style="display:none"' +
           ' onchange="handlePicker(this,' + di + ',' + si + ')">' +
    '<div class="slot-image" id="slotimg-' + di + '-' + si + '">' +
      '<img src="" alt="evidencia">' +
      '<button class="btn-rm-img" title="Eliminar imagen" onclick="removeImage(' + di + ',' + si + ')">×</button>' +
      '<textarea class="img-desc" rows="2" placeholder="Breve descripción de lo que muestra la imagen..."></textarea>' +
    '</div>' +
  '</div>';
}

function removeSlot(di, si) {
  const slot = document.getElementById('slot-' + di + '-' + si);
  if (slot) slot.remove();
}

// ── Guardar HTML ────────────────────────────────────────────────────────────
function guardarHtml() {
  // Sincronizar valores de inputs/textareas al atributo HTML antes de serializar
  document.querySelectorAll('input[type=text], .slot-label').forEach(el => {
    el.setAttribute('value', el.value);
  });
  document.querySelectorAll('textarea.img-desc').forEach(el => {
    el.textContent = el.value;
  });
  // Sincronizar src de imágenes al atributo
  document.querySelectorAll('.slot-image img').forEach(img => {
    img.setAttribute('src', img.src);
  });

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

// Función auxiliar que genera el HTML de un slot (usada tanto en Node como en el <script> del HTML)
function buildSlot(di, si, label) {
  return `<div class="ev-slot" id="slot-${di}-${si}">
      <div class="slot-header">
        <input type="text" class="slot-label" value="${escAttr(label)}" placeholder="Descripción de la evidencia...">
        <button class="btn-rm-slot" title="Eliminar slot" onclick="removeSlot(${di},${si})">×</button>
      </div>
      <div class="drop-zone" data-slot="${di}-${si}">
        <div class="drop-hint">Arrastra el pantallazo aquí o haz clic para seleccionar</div>
      </div>
      <input type="file" id="picker-${di}-${si}" accept="image/*" style="display:none"
             onchange="handlePicker(this,${di},${si})">
      <div class="slot-image" id="slotimg-${di}-${si}">
        <img src="" alt="evidencia">
        <button class="btn-rm-img" title="Eliminar imagen" onclick="removeImage(${di},${si})">×</button>
        <textarea class="img-desc" rows="2" placeholder="Breve descripción de lo que muestra la imagen..."></textarea>
      </div>
    </div>`;
}
