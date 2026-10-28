// =============================================================================
// Mapa (exige sesión)
//  - puntos agrupados por cercanía (el backend agrupa a 80 m), tamaño = nº de reportes
//  - anillo de cada punto = proporción de situaciones (con abogado / abierto / cerrado)
//  - pulso = en el punto hubo un incidente con daño
//  - al tocar un punto: lista de sus reportes; al tocar un reporte: detalle y fotos
//  - las fotos son PRIVADAS: solo las ve el autor, el personal de revisión y el abogado elegido
// El mapa de Leaflet se crea UNA vez y se conserva al cambiar de sección.
// =============================================================================
import { api, sesion, ErrorApi } from "../api.js";
import { $, escapar, fecha, fechaHora, plural, chipSituacion, SITUACIONES, ORDEN_SITUACIONES, avisar } from "../ui.js";

const FRECUENCIA = { UNICA: "Reporte único", MEDIA: "Frecuencia media", ALTA: "Alta frecuencia", CRITICA: "Frecuencia crítica" };
const VALLEDUPAR = [10.4735, -73.2505];

const estado = {
  puntos: null,
  misReportes: new Set(),
  filtros: { situaciones: new Set(ORDEN_SITUACIONES), categoria: "", soloAlta: false, calor: true },
  puntoActivo: null,
  urlsFotos: [],
  yaDibujado: false,
};
let mapa, capaMarcadores, capaCalor;

// ------------------------------------------------------------------ ciclo de vida de la vista
export async function mostrar(_contenedor, [tipo, id] = []) {
  if (!mapa) crearMapa();
  setTimeout(() => mapa.invalidateSize(), 0); // el contenedor pudo estar oculto
  if (!estado.puntos) {
    try {
      await cargarPuntos();
      encuadrar();
    } catch (e) {
      avisar(`No se pudo cargar el mapa: ${e.message}`, "error");
      return;
    }
  }
  if (tipo === "punto") abrirPunto(Number(id));
  if (tipo === "reporte") abrirReporteDesdeEnlace(Number(id));
}

/** Al ir a otra sección se cierra el panel (el mapa se conserva). */
export function ocultar() {
  if ($("#panel")?.classList.contains("abierto")) cerrarPanel(false);
}

/** Al cerrar sesión se olvidan los datos del usuario anterior. */
export function reiniciar() {
  estado.puntos = null;
  estado.misReportes.clear();
  estado.yaDibujado = false;
  liberarFotos();
}

/** Otras vistas (p. ej. "Mis reportes") avisan que hay datos nuevos. */
export function invalidar() {
  estado.puntos = null;
}

function crearMapa() {
  mapa = L.map("mapa", { zoomControl: false, attributionControl: true, minZoom: 11, maxZoom: 19, zoomSnap: 0.25 })
    .setView(VALLEDUPAR, 12);
  L.control.zoom({ position: "bottomright" }).addTo(mapa);
  // Teselas de OpenStreetMap (gratis, sin clave), oscurecidas con un filtro CSS (ver .teselas-oscuras).
  // Su política de uso admite tráfico bajo como esta demo; en producción se usa un servidor de teselas propio.
  L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19, className: "teselas-oscuras",
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
  }).addTo(mapa);
  capaMarcadores = L.layerGroup().addTo(mapa);
  // Calor: un solo tono (azul), de oscuro (poca concentración) a claro (mucha)
  capaCalor = L.heatLayer([], {
    radius: 32, blur: 26, maxZoom: 16, minOpacity: 0.25,
    gradient: { 0.25: "#104281", 0.45: "#1c5cab", 0.65: "#3987e5", 0.85: "#86b6ef", 1: "#cde2fb" },
  });
  conectarFiltros();
  $("#panel-cerrar").addEventListener("click", () => cerrarPanel(true));
  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape" && $("#panel").classList.contains("abierto") && !$("#visor").open) cerrarPanel(true);
  });
  api.categorias().then((cs) => $("#filtro-categoria").insertAdjacentHTML("beforeend",
    cs.map((c) => `<option>${escapar(c.nombre)}</option>`).join(""))).catch(() => {});
}

async function cargarPuntos() {
  const [puntos, mios] = await Promise.all([
    api.puntos(),
    sesion.tieneRol("CIUDADANO") ? api.misReportes().catch(() => []) : Promise.resolve([]),
  ]);
  estado.puntos = puntos;
  estado.misReportes = new Set(mios.map((r) => r.id));
  dibujarChips();
  dibujar();
}

function encuadrar() {
  if (!estado.puntos?.length) return;
  const limites = L.latLngBounds(estado.puntos.map((p) => [p.latitud, p.longitud]));
  mapa.flyToBounds(limites, { duration: 1.6, paddingTopLeft: [20, 150], paddingBottomRight: [20, 30] });
}

// ------------------------------------------------------------------ marcadores
function reportesVisibles(punto) {
  const f = estado.filtros;
  return punto.reportes.filter((r) => f.situaciones.has(r.situacion) && (!f.categoria || r.categoria === f.categoria));
}

/** Ícono SVG: anillo segmentado por situación (2 px de separación) y número de reportes al centro. */
function icono(reportes) {
  const n = reportes.length;
  const tam = Math.round(Math.min(66, 16 + 9 * Math.sqrt(n - 1) + (n > 1 ? 8 : 0)));
  const grosor = n > 1 ? Math.max(4, tam * 0.15) : 4;
  const r = tam / 2 - grosor / 2 - 1;
  const circ = 2 * Math.PI * r;
  const conteo = ORDEN_SITUACIONES.map((k) => [k, reportes.filter((x) => x.situacion === k).length]).filter(([, c]) => c > 0);
  const hueco = conteo.length > 1 ? 2 : 0;
  let desplazamiento = 0;
  const segmentos = conteo.map(([k, c]) => {
    const largo = Math.max(0.5, (c / n) * circ - hueco);
    const seg = `<circle cx="${tam / 2}" cy="${tam / 2}" r="${r}" fill="none" stroke="${SITUACIONES[k].hex}" stroke-width="${grosor}"
      stroke-dasharray="${largo} ${circ - largo}" stroke-dashoffset="${-desplazamiento}" transform="rotate(-90 ${tam / 2} ${tam / 2})"/>`;
    desplazamiento += (c / n) * circ;
    return seg;
  }).join("");
  const centro = `<circle cx="${tam / 2}" cy="${tam / 2}" r="${Math.max(2, r - grosor / 2 - 1)}" fill="#1a1a19"/>`;
  const numero = n > 1 ? `<text class="numero" x="50%" y="50%" dy=".35em" text-anchor="middle" style="--tam-numero:${Math.round(tam * 0.34)}px">${n}</text>` : "";
  return { html: `<svg width="${tam}" height="${tam}" viewBox="0 0 ${tam} ${tam}">${centro}${segmentos}${numero}</svg>`, tam };
}

function dibujar() {
  capaMarcadores.clearLayers();
  const calor = [];
  let indice = 0;
  const animar = !estado.yaDibujado; // la animación de entrada solo la primera vez
  estado.yaDibujado = true;
  // los puntos grandes al final, para que queden encima
  const visibles = estado.puntos
    .map((p) => ({ punto: p, reportes: reportesVisibles(p) }))
    .filter(({ reportes }) => reportes.length > 0 && (!estado.filtros.soloAlta || reportes.length >= 3))
    .sort((a, b) => a.reportes.length - b.reportes.length);

  for (const { punto, reportes } of visibles) {
    const { html, tam } = icono(reportes);
    const incidentes = reportes.filter((r) => r.tipo === "INCIDENTE").length;
    const clases = ["marcador", incidentes ? "con-incidente" : "", estado.puntoActivo === punto.id ? "activo" : "",
                    animar ? "" : "sin-entrada"].join(" ");
    const marcador = L.marker([punto.latitud, punto.longitud], {
      icon: L.divIcon({ html: `<div class="${clases}" style="animation-delay:${Math.min(indice++ * 14, 900)}ms;width:${tam}px;height:${tam}px">${html}</div>`,
                        className: "", iconSize: [tam, tam], iconAnchor: [tam / 2, tam / 2] }),
      riseOnHover: true, keyboard: true, title: `${punto.zona}: ${reportes.length} reportes`,
    });
    marcador.bindTooltip(
      `<strong>${escapar(punto.zona)}</strong><span>${plural(reportes.length, "reporte", "reportes")}` +
      `${incidentes ? " · " + plural(incidentes, "incidente", "incidentes") : ""}</span>`,
      { className: "tip", direction: "top", offset: [0, -tam / 2 - 4], opacity: 1 });
    marcador.on("click", () => abrirPunto(punto.id));
    marcador.addTo(capaMarcadores);
    reportes.forEach(() => calor.push([punto.latitud, punto.longitud, 0.55]));
  }
  capaCalor.setLatLngs(calor);
  if (estado.filtros.calor) capaCalor.addTo(mapa); else mapa.removeLayer(capaCalor);
  const todos = visibles.flatMap((v) => v.reportes);
  $("#k-reportes").textContent = todos.length;
  $("#k-puntos").textContent = visibles.filter((v) => v.reportes.length >= 3).length;
  $("#k-incidentes").textContent = todos.filter((r) => r.tipo === "INCIDENTE").length;
}

// ------------------------------------------------------------------ filtros
function dibujarChips() {
  const todos = estado.puntos.flatMap((p) => p.reportes);
  $("#chips-situacion").innerHTML = ORDEN_SITUACIONES.map((k) => {
    const s = SITUACIONES[k];
    const n = todos.filter((r) => r.situacion === k).length;
    return `<button type="button" class="chip" data-situacion="${k}" aria-pressed="${estado.filtros.situaciones.has(k)}"
              title="${escapar(s.explicacion)}"><span class="muestra" style="background:${s.color}"></span>${s.plural} <b>${n}</b></button>`;
  }).join("");
}

function conectarFiltros() {
  $("#chips-situacion").addEventListener("click", (e) => {
    const chip = e.target.closest(".chip");
    if (!chip) return;
    const k = chip.dataset.situacion;
    const f = estado.filtros.situaciones;
    if (f.has(k) && f.size === 1) return; // siempre queda al menos una situación visible
    f.has(k) ? f.delete(k) : f.add(k);
    chip.setAttribute("aria-pressed", f.has(k));
    dibujar();
  });
  $("#filtro-categoria").addEventListener("change", (e) => { estado.filtros.categoria = e.target.value; dibujar(); });
  $("#filtro-alta").addEventListener("change", (e) => { estado.filtros.soloAlta = e.target.checked; dibujar(); });
  $("#filtro-calor").addEventListener("change", (e) => { estado.filtros.calor = e.target.checked; dibujar(); });
}

// ------------------------------------------------------------------ panel
const panel = () => $("#panel");
const contenido = () => $("#panel-contenido");

function abrirPanel() {
  panel().classList.add("abierto");
  panel().setAttribute("aria-hidden", "false");
}

function cerrarPanel(actualizarEnlace) {
  panel().classList.remove("abierto");
  panel().setAttribute("aria-hidden", "true");
  estado.puntoActivo = null;
  liberarFotos();
  if (actualizarEnlace) enlace("");
  if (estado.puntos) dibujar();
}

/** Enlace compartible (#/mapa/punto/ID o #/mapa/reporte/ID) sin disparar el enrutador. */
function enlace(fragmento) {
  history.replaceState(null, "", `#/mapa${fragmento ? "/" + fragmento : ""}`);
}

function abrirPunto(idPunto, filtroLista = "TODOS") {
  const punto = estado.puntos?.find((p) => p.id === idPunto);
  if (!punto) return;
  estado.puntoActivo = idPunto;
  liberarFotos();
  enlace(`punto/${idPunto}`);
  const reportes = reportesVisibles(punto).length ? reportesVisibles(punto) : punto.reportes;
  const conteo = Object.fromEntries(ORDEN_SITUACIONES.map((k) => [k, reportes.filter((r) => r.situacion === k).length]));
  const lista = filtroLista === "TODOS" ? reportes : reportes.filter((r) => r.situacion === filtroLista);
  const fechas = reportes.map((r) => r.fecha).sort();
  const frecuencia = reportes.length >= 5 ? "CRITICA" : reportes.length >= 3 ? "ALTA" : reportes.length === 2 ? "MEDIA" : "UNICA";

  contenido().innerHTML = `
    <div class="vista">
      <div class="etiqueta-superior">Punto del mapa</div>
      <h2 id="panel-titulo">${escapar(punto.zona)}</h2>
      <p class="sub">${plural(reportes.length, "reporte", "reportes")} desde el ${fecha(fechas[0])}</p>
      <span class="insignia ${frecuencia === "CRITICA" ? "critica" : ""}">${FRECUENCIA[frecuencia]}</span>
      <div class="barra-situacion" role="img" aria-label="${ORDEN_SITUACIONES.map((k) => `${SITUACIONES[k].plural}: ${conteo[k]}`).join(", ")}">
        ${ORDEN_SITUACIONES.filter((k) => conteo[k]).map((k) => `<span style="flex:${conteo[k]};background:${SITUACIONES[k].color}"></span>`).join("")}
      </div>
      <div class="conteos">${ORDEN_SITUACIONES.map((k) => `<span><span class="muestra" style="background:${SITUACIONES[k].color}"></span>${SITUACIONES[k].plural} <b>${conteo[k]}</b></span>`).join("")}</div>
      <div class="etiquetas">${[...new Set(reportes.map((r) => r.categoria))].map((c) => `<span class="etiqueta">${escapar(c)}</span>`).join("")}</div>
      <div class="segmentos" role="group" aria-label="Filtrar la lista">
        ${["TODOS", ...ORDEN_SITUACIONES].map((k) => {
          const n = k === "TODOS" ? reportes.length : conteo[k];
          const nombre = k === "TODOS" ? "Todos" : SITUACIONES[k].plural;
          return `<button type="button" data-filtro="${k}" aria-pressed="${filtroLista === k}">${nombre} (${n})</button>`;
        }).join("")}
      </div>
      ${lista.length ? `<ul class="lista">${lista.map(tarjeta).join("")}</ul>` : `<p class="vacio">No hay reportes en esta situación.</p>`}
    </div>`;

  contenido().querySelectorAll("[data-filtro]").forEach((b) => b.addEventListener("click", () => abrirPunto(idPunto, b.dataset.filtro)));
  contenido().querySelectorAll("[data-reporte]").forEach((b) => b.addEventListener("click", () => abrirReporte(Number(b.dataset.reporte), idPunto)));
  abrirPanel();
  dibujar();
  // el punto queda visible junto al panel: a la izquierda en PC, arriba de la hoja inferior en celular
  const desplazamiento = window.innerWidth > 760 ? [-(panel().offsetWidth / 2), 0] : [0, -window.innerHeight * 0.3];
  mapa.flyTo(mapa.unproject(mapa.project([punto.latitud, punto.longitud], 17).subtract(desplazamiento), 17), 17, { duration: 0.9 });
}

function tarjeta(r) {
  const incidente = r.tipo === "INCIDENTE";
  const mio = estado.misReportes.has(r.id);
  return `
    <li>
      <button type="button" class="tarjeta" data-reporte="${r.id}" style="--color-situacion:${SITUACIONES[r.situacion].color}">
        <div class="tarjeta-cabeza">
          <span class="tarjeta-titulo">${incidente ? '<span class="tipo-incidente">⚠ Incidente</span> · ' : ""}${escapar(r.categoria)}</span>
          <time class="tarjeta-fecha" datetime="${r.fecha}">${fecha(r.fecha)}</time>
        </div>
        <p class="tarjeta-desc">${escapar(r.descripcion)}</p>
        <div class="tarjeta-pie">
          ${chipSituacion(r.situacion)}
          <span class="estado">${escapar(r.estado)}</span>
          ${mio ? '<span class="etiqueta etiqueta-mia">Tu reporte</span>' : ""}
          ${r.numFotos ? `<span class="candado" title="Las fotos son evidencia privada">🔒 ${plural(r.numFotos, "foto", "fotos")}</span>` : ""}
        </div>
      </button>
    </li>`;
}

function abrirReporteDesdeEnlace(idReporte) {
  const punto = estado.puntos?.find((p) => p.reportes.some((r) => r.id === idReporte));
  if (punto) abrirPunto(punto.id);
  abrirReporte(idReporte, punto?.id);
}

async function abrirReporte(idReporte, idPunto) {
  liberarFotos();
  enlace(`reporte/${idReporte}`);
  abrirPanel();
  contenido().innerHTML = `<div class="vista"><p class="vacio">Cargando…</p></div>`;
  let d;
  try {
    d = await api.detallePublico(idReporte);
  } catch (e) {
    contenido().innerHTML = `<div class="vista"><p class="vacio">${escapar(e.message)}</p></div>`;
    return;
  }
  const s = SITUACIONES[d.situacion];
  const hitos = [{ estado: "Reportado", fecha: d.fecha }, ...d.lineaDeTiempo];
  contenido().innerHTML = `
    <div class="vista">
      ${idPunto ? `<button type="button" class="volver" id="volver">← Volver a ${escapar(d.zona)}</button>` : ""}
      <div class="etiqueta-superior">${d.tipo === "INCIDENTE" ? "⚠ Incidente con daño" : "Condición reportada"} · Reporte #${d.id}</div>
      <h2 id="panel-titulo">${escapar(d.categoria)}</h2>
      <div class="situacion-grande"><span class="muestra" style="background:${s.color}"></span>
        <div><b>${s.nombre}</b><small>${escapar(s.explicacion)}</small></div></div>
      <p class="descripcion">${escapar(d.descripcion)}</p>
      <dl class="datos">
        <div><dt>Zona</dt><dd>${escapar(d.zona)}</dd></div>
        <div><dt>Fecha del reporte</dt><dd>${fecha(d.fecha)}</dd></div>
        <div><dt>Estado de revisión</dt><dd>${escapar(d.estado)}</dd></div>
        <div><dt>Gravedad percibida</dt><dd class="gravedad" aria-label="${d.nivelGravedad ?? "sin dato"} de 5">${d.nivelGravedad ? "●".repeat(d.nivelGravedad) + "○".repeat(5 - d.nivelGravedad) : "—"}</dd></div>
      </dl>
      <div class="seccion">Línea de tiempo</div>
      <ol class="hitos">${hitos.map((h) => `<li><span>${escapar(h.estado)}</span><time>${h.estado === "Reportado" ? fecha(h.fecha) : fechaHora(h.fecha)}</time></li>`).join("")}</ol>
      <div class="seccion">Fotos de evidencia ${d.numFotos ? `(${d.numFotos})` : ""}</div>
      <div id="zona-fotos"></div>
    </div>`;
  $("#volver")?.addEventListener("click", () => abrirPunto(idPunto));
  await cargarFotos($("#zona-fotos"), d.id, d.numFotos, estado.misReportes.has(d.id), () => abrirReporte(d.id, idPunto));
}

// ------------------------------------------------------------------ fotos privadas
/**
 * Galería de fotos de un reporte. Las fotos son privadas: se piden con el token y se muestran como
 * blobs locales. La usan el mapa y el expediente del abogado.
 */
export async function cargarFotos(zona, idReporte, numFotos, esAutor, alSubir) {
  try {
    const lista = await api.evidencias(idReporte);
    zona.innerHTML = lista.length
      ? `<div class="fotos">${lista.map((f, i) => `<button type="button" class="foto" data-foto="${i}" aria-label="Ver foto ${i + 1}"><img alt="" loading="lazy"></button>`).join("")}</div>`
      : `<p class="mensaje">Este reporte aún no tiene fotos.</p>`;
    lista.forEach(async (f, i) => {
      const url = URL.createObjectURL(await api.archivo(f.urlArchivo));
      estado.urlsFotos.push(url);
      const boton = zona.querySelector(`[data-foto="${i}"]`);
      if (!boton) return;
      boton.querySelector("img").src = url;
      boton.addEventListener("click", () => verFoto(url, `Cargada el ${fechaHora(f.fechaCarga)} · huella SHA-256 ${f.hashSha256.slice(0, 12)}…`));
    });
    if (esAutor) {
      zona.insertAdjacentHTML("beforeend", `
        <label class="subir boton"><input type="file" accept="image/jpeg,image/png,image/webp">＋ Agregar foto</label>
        <p class="mensaje">JPG, PNG o WEBP, máximo 10 MB.</p>`);
      zona.querySelector(".subir input").addEventListener("change", async (e) => {
        const archivo = e.target.files[0];
        if (!archivo) return;
        try {
          await api.subirFoto(idReporte, archivo);
          avisar("Foto agregada.", "exito");
          invalidar();
          alSubir?.();
        } catch (err) {
          avisar(err.message, "error");
        }
      });
    }
  } catch (e) {
    const motivo = e instanceof ErrorApi && e.estado === 403
      ? "Son evidencia privada: solo las ven el autor, el personal de revisión y el abogado que el ciudadano elija."
      : e.message;
    zona.innerHTML = `<div class="bloqueo"><strong>🔒 ${numFotos ? plural(numFotos, "foto privada", "fotos privadas") : "Fotos privadas"}</strong>
      <span>${escapar(motivo)}</span></div>`;
  }
}

export function liberarFotos() {
  estado.urlsFotos.forEach((u) => URL.revokeObjectURL(u));
  estado.urlsFotos = [];
}

function verFoto(url, pie) {
  $("#visor-img").src = url;
  $("#visor-pie").textContent = pie;
  $("#visor").showModal();
}
