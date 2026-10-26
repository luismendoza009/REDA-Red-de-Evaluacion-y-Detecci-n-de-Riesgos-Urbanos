// =============================================================================
// REDA · mapa público
//  - puntos agrupados por cercanía (el backend agrupa a 80 m), tamaño = nº de reportes
//  - anillo de cada punto = proporción de situaciones (con abogado / abierto / cerrado)
//  - pulso = en el punto hubo un incidente con daño
//  - al tocar un punto: lista de sus reportes; al tocar un reporte: detalle y fotos
//  - las fotos son PRIVADAS: solo las ve el autor o el personal de revisión con sesión iniciada
// Todo texto escrito por usuarios se inserta escapado (prevención de XSS).
// =============================================================================
import { api, sesion, ErrorApi } from "./api.js";

const SITUACIONES = {
  CON_ABOGADO: { nombre: "Con abogado", plural: "Con abogado", color: "var(--con-abogado)", hex: "#3987e5",
                 explicacion: "Un abogado aceptó el caso y está en curso." },
  ABIERTO:     { nombre: "Abierto", plural: "Abiertos", color: "var(--abierto)", hex: "#d95926",
                 explicacion: "En revisión, validado o esperando respuesta de un abogado." },
  CERRADO:     { nombre: "Cerrado", plural: "Cerrados", color: "var(--cerrado)", hex: "#199e70",
                 explicacion: "El caso se cerró, ningún abogado lo tomó o la falla fue solucionada." },
};
const ORDEN_SITUACIONES = ["CON_ABOGADO", "ABIERTO", "CERRADO"];
const FRECUENCIA = { UNICA: "Reporte único", MEDIA: "Frecuencia media", ALTA: "Alta frecuencia", CRITICA: "Frecuencia crítica" };
const VALLEDUPAR = [10.4735, -73.2505];

const estado = {
  puntos: [],
  misReportes: new Set(),
  filtros: { situaciones: new Set(ORDEN_SITUACIONES), categoria: "", soloAlta: false, calor: true },
  puntoActivo: null,
  urlsFotos: [],
};

// ------------------------------------------------------------------ utilidades
const $ = (sel) => document.querySelector(sel);
const escapar = (t) => String(t ?? "").replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
const fechaCorta = new Intl.DateTimeFormat("es-CO", { day: "numeric", month: "short", year: "numeric", timeZone: "UTC" });
const fechaHora = new Intl.DateTimeFormat("es-CO", { day: "numeric", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit" });
const fecha = (iso) => fechaCorta.format(new Date(iso));
const plural = (n, uno, varios) => `${n} ${n === 1 ? uno : varios}`;

function chipSituacion(clave) {
  const s = SITUACIONES[clave];
  return `<span class="situacion"><span class="muestra" style="background:${s.color}"></span>${s.nombre}</span>`;
}

// ------------------------------------------------------------------ mapa
const mapa = L.map("mapa", { zoomControl: false, attributionControl: true, minZoom: 11, maxZoom: 19, zoomSnap: 0.25 })
  .setView(VALLEDUPAR, 12);
L.control.zoom({ position: "bottomright" }).addTo(mapa);
// Teselas de OpenStreetMap (gratis, sin clave), oscurecidas con un filtro CSS (ver .teselas-oscuras).
// Su política de uso admite tráfico bajo como esta demo; en producción se usa un servidor de teselas propio.
L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
  maxZoom: 19, className: "teselas-oscuras",
  attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
}).addTo(mapa);

const capaMarcadores = L.layerGroup().addTo(mapa);
// Calor: un solo tono (azul), de oscuro (poca concentración) a claro (mucha)
const capaCalor = L.heatLayer([], {
  radius: 32, blur: 26, maxZoom: 16, minOpacity: 0.25,
  gradient: { 0.25: "#104281", 0.45: "#1c5cab", 0.65: "#3987e5", 0.85: "#86b6ef", 1: "#cde2fb" },
});
const marcadoresPorId = new Map();

/** Reportes de un punto que pasan los filtros actuales. */
function reportesVisibles(punto) {
  const f = estado.filtros;
  return punto.reportes.filter((r) => f.situaciones.has(r.situacion) && (!f.categoria || r.categoria === f.categoria));
}

/** Ícono SVG: anillo segmentado por situación (2 px de separación) y número de reportes al centro. */
function icono(reportes, activo) {
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
  marcadoresPorId.clear();
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
    const conIncidente = reportes.some((r) => r.tipo === "INCIDENTE");
    const clases = ["marcador", conIncidente ? "con-incidente" : "", estado.puntoActivo === punto.id ? "activo" : "",
                    animar ? "" : "sin-entrada"].join(" ");
    const marcador = L.marker([punto.latitud, punto.longitud], {
      icon: L.divIcon({ html: `<div class="${clases}" style="animation-delay:${Math.min(indice++ * 14, 900)}ms;width:${tam}px;height:${tam}px">${html}</div>`,
                        className: "", iconSize: [tam, tam], iconAnchor: [tam / 2, tam / 2] }),
      riseOnHover: true, keyboard: true, title: `${punto.zona}: ${reportes.length} reportes`,
    });
    marcador.bindTooltip(
      `<strong>${escapar(punto.zona)}</strong><span>${plural(reportes.length, "reporte", "reportes")}` +
      `${conIncidente ? " · " + plural(reportes.filter((r) => r.tipo === "INCIDENTE").length, "incidente", "incidentes") : ""}</span>`,
      { className: "tip", direction: "top", offset: [0, -tam / 2 - 4], opacity: 1 });
    marcador.on("click", () => abrirPunto(punto.id));
    marcador.addTo(capaMarcadores);
    marcadoresPorId.set(punto.id, marcador);
    reportes.forEach(() => calor.push([punto.latitud, punto.longitud, 0.55]));
  }
  capaCalor.setLatLngs(calor);
  if (estado.filtros.calor) capaCalor.addTo(mapa); else mapa.removeLayer(capaCalor);
  actualizarCifras(visibles);
}

function actualizarCifras(visibles) {
  const reportes = visibles.flatMap((v) => v.reportes);
  $("#k-reportes").textContent = reportes.length;
  $("#k-puntos").textContent = visibles.filter((v) => v.reportes.length >= 3).length;
  $("#k-incidentes").textContent = reportes.filter((r) => r.tipo === "INCIDENTE").length;
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

// ------------------------------------------------------------------ panel: punto
const panel = $("#panel");
const contenido = $("#panel-contenido");

function abrirPanel() {
  panel.classList.add("abierto");
  panel.setAttribute("aria-hidden", "false");
}
function cerrarPanel() {
  panel.classList.remove("abierto");
  panel.setAttribute("aria-hidden", "true");
  estado.puntoActivo = null;
  liberarFotos();
  enlace("");
  dibujar();
}

/** Enlace directo compartible: #punto=ID o #reporte=ID (sin recargar la página). */
function enlace(fragmento) {
  history.replaceState(null, "", fragmento ? `#${fragmento}` : location.pathname);
}
$("#panel-cerrar").addEventListener("click", cerrarPanel);
document.addEventListener("keydown", (e) => { if (e.key === "Escape" && panel.classList.contains("abierto")) cerrarPanel(); });

function abrirPunto(idPunto, filtroLista = "TODOS") {
  const punto = estado.puntos.find((p) => p.id === idPunto);
  if (!punto) return;
  estado.puntoActivo = idPunto;
  liberarFotos();
  enlace(`punto=${idPunto}`);
  const reportes = reportesVisibles(punto);
  const conteo = Object.fromEntries(ORDEN_SITUACIONES.map((k) => [k, reportes.filter((r) => r.situacion === k).length]));
  const lista = filtroLista === "TODOS" ? reportes : reportes.filter((r) => r.situacion === filtroLista);
  const fechas = reportes.map((r) => r.fecha).sort();
  const frecuencia = reportes.length >= 5 ? "CRITICA" : reportes.length >= 3 ? "ALTA" : reportes.length === 2 ? "MEDIA" : "UNICA";

  contenido.innerHTML = `
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

  contenido.querySelectorAll("[data-filtro]").forEach((b) => b.addEventListener("click", () => abrirPunto(idPunto, b.dataset.filtro)));
  contenido.querySelectorAll("[data-reporte]").forEach((b) => b.addEventListener("click", () => abrirReporte(Number(b.dataset.reporte), idPunto)));
  abrirPanel();
  dibujar();
  // el punto queda visible junto al panel: a la izquierda en PC, arriba de la hoja inferior en celular
  const desplazamientoPanel = window.innerWidth > 760 ? [-(panel.offsetWidth / 2), 0] : [0, -window.innerHeight * 0.3];
  mapa.flyTo(mapa.unproject(mapa.project([punto.latitud, punto.longitud], 17).subtract(desplazamientoPanel), 17), 17, { duration: 0.9 });
}

function tarjeta(r) {
  const incidente = r.tipo === "INCIDENTE";
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
          ${r.numFotos ? `<span class="candado" title="Las fotos son evidencia privada">🔒 ${plural(r.numFotos, "foto", "fotos")}</span>` : ""}
        </div>
      </button>
    </li>`;
}

// ------------------------------------------------------------------ panel: reporte
async function abrirReporte(idReporte, idPunto) {
  liberarFotos();
  enlace(`reporte=${idReporte}`);
  contenido.innerHTML = `<div class="vista"><p class="vacio">Cargando…</p></div>`;
  let d;
  try {
    d = await api.detallePublico(idReporte);
  } catch (e) {
    contenido.innerHTML = `<div class="vista"><p class="vacio">${escapar(e.message)}</p></div>`;
    return;
  }
  const s = SITUACIONES[d.situacion];
  const hitos = [{ estado: "Reportado", fecha: d.fecha }, ...d.lineaDeTiempo];
  contenido.innerHTML = `
    <div class="vista">
      <button type="button" class="volver" id="volver">← Volver a ${escapar(d.zona)}</button>
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
      <ol class="hitos">${hitos.map((h) => `<li><span>${escapar(h.estado)}</span><time>${h.estado === "Reportado" ? fecha(h.fecha) : fechaHora.format(new Date(h.fecha))}</time></li>`).join("")}</ol>
      <div class="seccion">Fotos de evidencia ${d.numFotos ? `(${d.numFotos})` : ""}</div>
      <div id="zona-fotos"></div>
    </div>`;
  $("#volver").addEventListener("click", () => abrirPunto(idPunto));
  cargarFotos(d);
}

/** Las fotos son privadas: se piden con el token y se muestran como blobs locales. */
async function cargarFotos(d) {
  const zona = $("#zona-fotos");
  const s = sesion.actual();
  const esAutor = estado.misReportes.has(d.id);
  if (!s) {
    zona.innerHTML = bloqueo(d.numFotos, "Inicia sesión como autor del reporte u operador para verlas.", true);
    zona.querySelector("[data-entrar]")?.addEventListener("click", abrirSesion);
    return;
  }
  try {
    const lista = await api.evidencias(d.id);
    zona.innerHTML = lista.length ? `<div class="fotos">${lista.map((f, i) => `<button type="button" class="foto" data-foto="${i}" aria-label="Ver foto ${i + 1}"><img alt="" loading="lazy"></button>`).join("")}</div>`
                                  : `<p class="mensaje">Este reporte aún no tiene fotos.</p>`;
    lista.forEach(async (f, i) => {
      const url = URL.createObjectURL(await api.archivo(f.urlArchivo));
      estado.urlsFotos.push(url);
      const boton = zona.querySelector(`[data-foto="${i}"]`);
      if (!boton) return;
      boton.querySelector("img").src = url;
      boton.addEventListener("click", () => verFoto(url, `Cargada el ${fechaHora.format(new Date(f.fechaCarga))} · huella SHA-256 ${f.hashSha256.slice(0, 12)}…`));
    });
    if (esAutor) zona.insertAdjacentHTML("beforeend", controlSubida());
    zona.querySelector("#subir-foto")?.addEventListener("change", (e) => subirFoto(d, e.target.files[0]));
  } catch (e) {
    const motivo = e instanceof ErrorApi && e.estado === 403
      ? "Solo el autor del reporte y el personal de revisión pueden ver estas fotos."
      : e.message;
    zona.innerHTML = bloqueo(d.numFotos, motivo, false);
  }
}

function bloqueo(n, motivo, conBoton) {
  return `<div class="bloqueo"><strong>🔒 ${n ? plural(n, "foto privada", "fotos privadas") : "Fotos privadas"}</strong>
    <span>${escapar(motivo)}</span>${conBoton ? '<button type="button" class="boton boton-claro" data-entrar>Iniciar sesión</button>' : ""}</div>`;
}

function controlSubida() {
  return `<label class="subir boton"><input type="file" id="subir-foto" accept="image/jpeg,image/png,image/webp">＋ Agregar foto</label>
          <p class="mensaje" id="mensaje-subida">JPG, PNG o WEBP, máximo 10 MB.</p>`;
}

async function subirFoto(d, archivo) {
  if (!archivo) return;
  const mensaje = $("#mensaje-subida");
  mensaje.textContent = "Subiendo…";
  try {
    await api.subirFoto(d.id, archivo);
    mensaje.textContent = "Foto agregada.";
    await cargarPuntos();
    abrirReporte(d.id, estado.puntoActivo);
  } catch (e) {
    mensaje.textContent = e.message;
  }
}

function liberarFotos() {
  estado.urlsFotos.forEach((u) => URL.revokeObjectURL(u));
  estado.urlsFotos = [];
}

const visor = $("#visor");
function verFoto(url, pie) {
  $("#visor-img").src = url;
  $("#visor-pie").textContent = pie;
  visor.showModal();
}
$("#visor-cerrar").addEventListener("click", () => visor.close());
visor.addEventListener("click", (e) => { if (e.target === visor) visor.close(); });

// ------------------------------------------------------------------ sesión
const modal = $("#modal-sesion");
function abrirSesion() {
  $("#error-sesion").textContent = "";
  modal.showModal();
}
$("#btn-cancelar").addEventListener("click", () => modal.close());
$("#form-sesion").addEventListener("submit", async (e) => {
  e.preventDefault();
  const datos = new FormData(e.target);
  try {
    const r = await api.iniciarSesion(datos.get("correo"), datos.get("password"));
    sesion.guardar(r);
    modal.close();
    e.target.reset();
    await alIniciarSesion();
  } catch (err) {
    $("#error-sesion").textContent = err.message;
  }
});

async function alIniciarSesion() {
  const s = sesion.actual();
  const contenedor = $("#sesion");
  if (!s) {
    estado.misReportes.clear();
    contenedor.innerHTML = '<button class="boton boton-claro" id="btn-entrar" type="button">Iniciar sesión</button>';
    $("#btn-entrar").addEventListener("click", abrirSesion);
    return;
  }
  const roles = { CIUDADANO: "Ciudadano", OPERADOR: "Operador", ABOGADO: "Abogado", ADMINISTRADOR: "Administrador", ANALISTA: "Analista" };
  contenedor.innerHTML = `<div class="usuario">${escapar(s.nombre)}<small>${roles[s.rol] ?? s.rol}</small></div>
                          <button class="boton" id="btn-salir" type="button">Salir</button>`;
  $("#btn-salir").addEventListener("click", () => { sesion.cerrar(); liberarFotos(); alIniciarSesion(); if (panel.classList.contains("abierto")) cerrarPanel(); });
  if (s.rol === "CIUDADANO") {
    try { estado.misReportes = new Set((await api.misReportes()).map((r) => r.id)); } catch { estado.misReportes.clear(); }
  }
}

// ------------------------------------------------------------------ arranque
async function cargarPuntos() {
  estado.puntos = await api.puntos();
  dibujarChips();
  dibujar();
}

async function iniciar() {
  try {
    const [categorias] = await Promise.all([api.categorias(), cargarPuntos(), alIniciarSesion()]);
    $("#filtro-categoria").insertAdjacentHTML("beforeend", categorias.map((c) => `<option>${escapar(c.nombre)}</option>`).join(""));
    const [, tipo, id] = location.hash.match(/^#(punto|reporte)=(\d+)$/) ?? [];
    if (tipo) {
      abrirDesdeEnlace(tipo, Number(id));
    } else if (estado.puntos.length) {
      const limites = L.latLngBounds(estado.puntos.map((p) => [p.latitud, p.longitud]));
      mapa.flyToBounds(limites, { duration: 1.6, paddingTopLeft: [20, 150], paddingBottomRight: [20, 30] });
    }
  } catch (e) {
    contenido.innerHTML = `<div class="vista"><p class="vacio">No se pudo cargar el mapa: ${escapar(e.message)}</p></div>`;
    abrirPanel();
  }
}

function abrirDesdeEnlace(tipo, id) {
  const punto = tipo === "punto"
    ? estado.puntos.find((p) => p.id === id)
    : estado.puntos.find((p) => p.reportes.some((r) => r.id === id));
  if (!punto) return;
  abrirPunto(punto.id);
  if (tipo === "reporte") abrirReporte(id, punto.id);
}

iniciar();
