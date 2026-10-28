// =============================================================================
// Reportar una falla: CONDICIÓN (antecedente, sin daño) o INCIDENTE (hubo un daño).
// Ubicación en un mini-mapa (clic o GPS del celular), fotos con vista previa, y para incidentes:
// fecha y hora del hecho, tipo de daño, atención médica, entidad y testigos.
// Al crear un incidente, el sistema muestra cuántos reportes previos había en ese punto.
// =============================================================================
import { api } from "../api.js";
import { $, $$, escapar, fecha, plural, avisar, mostrarErrores, conBotonOcupado, cargando } from "../ui.js";
import * as mapaPrincipal from "./mapa.js";

const MAX_FOTOS = 5;
const MAX_BYTES = 10 * 1024 * 1024;
const TIPOS_FOTO = ["image/jpeg", "image/png", "image/webp"];
const VALLEDUPAR = [10.4735, -73.2505];

export async function mostrar(contenedor) {
  contenedor.innerHTML = cargando();
  let categorias, zonas, entidades;
  try {
    [categorias, zonas, entidades] = await Promise.all([api.categorias(), api.zonas(), api.entidades()]);
  } catch (e) {
    contenedor.innerHTML = `<div class="contenido"><p class="error">${escapar(e.message)}</p></div>`;
    return;
  }
  const ahora = new Date(Date.now() - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 16);
  contenedor.innerHTML = `
    <div class="contenido vista">
      <header class="encabezado">
        <p class="etiqueta-superior">Nuevo reporte</p>
        <h1>Reportar una falla</h1>
        <p class="texto-2">Cada reporte queda con fecha, ubicación y fotos. Si después alguien sufre un daño en ese mismo
          punto, tu reporte ayuda a demostrar que la entidad responsable ya lo sabía.</p>
      </header>
      <form id="f-reporte" class="formulario tarjeta-formulario" novalidate>
        <fieldset class="tipos">
          <legend>¿Qué quieres reportar?</legend>
          <label class="tipo"><input type="radio" name="tipo" value="CONDICION" checked>
            <span><b>Una falla</b><small>Un hueco, una alcantarilla sin tapa, una luminaria dañada… Nadie salió herido.</small></span></label>
          <label class="tipo"><input type="radio" name="tipo" value="INCIDENTE">
            <span><b>Un incidente</b><small>La falla me causó un daño: una lesión, un daño a mi vehículo o a mis cosas.</small></span></label>
        </fieldset>

        <div class="fila-2">
          <label>Categoría<select name="idCategoria" required><option value="">Elige…</option>
            ${categorias.map((c) => `<option value="${c.id}" title="${escapar(c.descripcion ?? "")}">${escapar(c.nombre)}</option>`).join("")}</select></label>
          <label>Barrio o zona<select name="idZona" required><option value="">Elige…</option>
            ${zonas.map((z) => `<option value="${z.id}">${escapar(z.nombre)}</option>`).join("")}</select>
            <small id="zona-sugerencia" aria-live="polite">Se sugiere solo al marcar el punto en el mapa.</small></label>
        </div>

        <div class="campo">
          <span class="etiqueta-campo">Ubicación exacta</span>
          <div id="mapa-selector" class="mapa-selector" aria-label="Toca el mapa para marcar dónde está la falla"></div>
          <div class="fila-acciones">
            <button type="button" class="boton" id="btn-gps">📍 Usar mi ubicación</button>
            <span class="texto-2" id="coordenadas">Toca el mapa para marcar el punto.</span>
          </div>
          <input type="hidden" name="latitud"><input type="hidden" name="longitud">
        </div>

        <label>Describe la falla<textarea name="descripcion" rows="3" minlength="10" maxlength="2000" required
          placeholder="Ej.: Alcantarilla sin tapa frente a la panadería, Calle 16 con Carrera 9. De noche no se ve."></textarea>
          <small><span id="contador">0</span>/2000</small></label>

        <fieldset class="campo">
          <legend>¿Qué tan grave te parece? (opcional)</legend>
          <div class="escala">${[1, 2, 3, 4, 5].map((n) => `<label><input type="radio" name="nivelGravedad" value="${n}"><span>${n}</span></label>`).join("")}</div>
          <small>1 = molesta · 5 = muy peligrosa</small>
        </fieldset>

        <section id="datos-incidente" class="bloque-incidente" hidden>
          <h2>Lo que te pasó</h2>
          <p class="texto-2">Estos datos son privados. Solo los ven el equipo de revisión y el abogado que tú elijas.</p>
          <div class="fila-2">
            <label>Fecha y hora del hecho<input type="datetime-local" name="fechaHoraHecho" max="${ahora}"></label>
            <label>Entidad que crees responsable<select name="idEntidad"><option value="">No sé</option>
              ${entidades.map((e) => `<option value="${e.id}">${escapar(e.nombre)}</option>`).join("")}</select></label>
          </div>
          <fieldset class="campo">
            <legend>Tipo de daño</legend>
            <div class="opciones">
              <label><input type="radio" name="tipoDano" value="LESION"> Lesión personal</label>
              <label><input type="radio" name="tipoDano" value="DANO_MATERIAL"> Daño material</label>
              <label><input type="radio" name="tipoDano" value="AMBOS"> Ambos</label>
            </div>
          </fieldset>
          <label class="casilla"><input type="checkbox" name="huboAtencionMedica" disabled>
            <span>Recibí atención médica (urgencias, clínica, EPS)</span></label>
          <label>Describe el daño<textarea name="descripcionDano" rows="3" minlength="10" maxlength="2000"
            placeholder="Ej.: Me caí de la moto, fractura de muñeca; la moto quedó con el guardabarros roto."></textarea></label>
          <div class="campo">
            <span class="etiqueta-campo">Testigos (opcional, máximo 5)</span>
            <div id="testigos"></div>
            <button type="button" class="boton" id="btn-testigo">＋ Agregar testigo</button>
          </div>
        </section>

        <div class="campo">
          <span class="etiqueta-campo">Fotos (máximo ${MAX_FOTOS}, JPG/PNG/WEBP de hasta 10 MB)</span>
          <label class="zona-fotos"><input type="file" id="fotos" accept="${TIPOS_FOTO.join(",")}" multiple>
            <span>📷 Toca para tomar o elegir fotos</span></label>
          <div class="previas" id="previas"></div>
          <small>Las fotos son evidencia privada y quedan con su huella digital (SHA-256), que prueba que no se alteraron.</small>
        </div>

        <p class="error" role="alert"></p>
        <div class="fila-acciones">
          <a class="boton" href="#/inicio">Cancelar</a>
          <button class="boton boton-claro" type="submit">Enviar reporte</button>
        </div>
      </form>
    </div>`;

  const form = $("#f-reporte");
  const fotos = [];
  const selector = crearSelector(form, zonas);
  conectarTipo(form);
  conectarTestigos();
  conectarFotos(fotos);
  form.descripcion.addEventListener("input", () => { $("#contador").textContent = form.descripcion.value.length; });

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    await conBotonOcupado(form.querySelector("button[type=submit]"), async () => {
      try {
        const resultado = await enviar(form, fotos);
        selector.remove();
        mapaPrincipal.invalidar();
        mostrarResultado(contenedor, resultado);
      } catch (err) {
        mostrarErrores(form, err);
        form.querySelector("[aria-invalid]")?.scrollIntoView({ behavior: "smooth", block: "center" });
      }
    });
  });
}

function crearSelector(form, zonas) {
  const m = L.map("mapa-selector", { zoomControl: true, attributionControl: false }).setView(VALLEDUPAR, 13);
  L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", { maxZoom: 19, className: "teselas-oscuras" }).addTo(m);
  let marcador = null;
  // Si el ciudadano eligió el barrio a mano, la sugerencia ya no lo cambia (él conoce su barrio mejor que el mapa).
  let barrioAMano = false;
  let consulta = 0;
  const pista = $("#zona-sugerencia");
  form.idZona.addEventListener("change", (e) => {
    if (!e.isTrusted) return;
    barrioAMano = Boolean(form.idZona.value);
    pista.textContent = barrioAMano ? "Elegido por ti." : "Se sugiere solo al marcar el punto en el mapa.";
    // sin punto marcado todavía: acerca el mapa al barrio elegido para que sea fácil marcarlo
    const z = zonas.find((x) => x.id === Number(form.idZona.value));
    if (!marcador && z?.latitud != null) m.setView([z.latitud, z.longitud], 16);
  });
  const sugerirBarrio = async (lat, lon) => {
    if (barrioAMano) return;
    const mia = ++consulta; // si el ciudadano toca varias veces seguidas, solo vale la última respuesta
    let sugerida = null;
    try { sugerida = await api.zonaSugerida(lat, lon); } catch { return; }
    if (mia !== consulta || barrioAMano) return;
    if (sugerida) {
      form.idZona.value = sugerida.id;
      form.idZona.removeAttribute("aria-invalid");
      pista.textContent = `Sugerido por el punto marcado (a ${sugerida.distanciaMetros} m). Cámbialo si no es tu barrio.`;
    } else {
      form.idZona.value = "";
      pista.textContent = "No reconocemos el barrio de este punto: elígelo de la lista.";
    }
  };
  const marcar = (lat, lon) => {
    const ll = [Number(lat.toFixed(6)), Number(lon.toFixed(6))];
    if (marcador) marcador.setLatLng(ll); else marcador = L.circleMarker(ll, { radius: 9, color: "#f0efec", weight: 3, fillColor: "#d95926", fillOpacity: 1 }).addTo(m);
    form.latitud.value = ll[0];
    form.longitud.value = ll[1];
    $("#coordenadas").textContent = `Punto marcado: ${ll[0]}, ${ll[1]}`;
    sugerirBarrio(ll[0], ll[1]);
  };
  m.on("click", (e) => marcar(e.latlng.lat, e.latlng.lng));
  $("#btn-gps").addEventListener("click", () => {
    if (!navigator.geolocation) { avisar("Tu navegador no permite obtener la ubicación.", "error"); return; }
    $("#coordenadas").textContent = "Buscando tu ubicación…";
    navigator.geolocation.getCurrentPosition(
      (p) => { marcar(p.coords.latitude, p.coords.longitude); m.setView([p.coords.latitude, p.coords.longitude], 17); },
      () => { $("#coordenadas").textContent = "No se pudo obtener tu ubicación. Marca el punto en el mapa."; },
      { enableHighAccuracy: true, timeout: 10000 });
  });
  setTimeout(() => m.invalidateSize(), 50);
  return m;
}

function conectarTipo(form) {
  const actualizar = () => {
    const incidente = form.tipo.value === "INCIDENTE";
    $("#datos-incidente").hidden = !incidente;
    form.fechaHoraHecho.required = incidente;
    form.descripcionDano.required = incidente;
  };
  $$("input[name=tipo]", form).forEach((r) => r.addEventListener("change", actualizar));
  // la atención médica solo aplica si hubo lesión (misma regla que el dominio y la base)
  $$("input[name=tipoDano]", form).forEach((r) => r.addEventListener("change", () => {
    const conLesion = ["LESION", "AMBOS"].includes(form.tipoDano.value);
    form.huboAtencionMedica.disabled = !conLesion;
    if (!conLesion) form.huboAtencionMedica.checked = false;
  }));
}

function conectarTestigos() {
  const lista = $("#testigos");
  $("#btn-testigo").addEventListener("click", () => {
    if (lista.children.length >= 5) { avisar("Máximo 5 testigos.", "error"); return; }
    lista.insertAdjacentHTML("beforeend", `
      <div class="testigo fila-2">
        <label>Nombre<input class="t-nombre" maxlength="120"></label>
        <label>Teléfono o correo<input class="t-contacto" maxlength="150"></label>
        <button type="button" class="boton boton-icono" aria-label="Quitar testigo">✕</button>
      </div>`);
    lista.lastElementChild.querySelector("button").addEventListener("click", (e) => e.target.closest(".testigo").remove());
  });
}

function conectarFotos(fotos) {
  const previas = $("#previas");
  const pintar = () => {
    previas.innerHTML = fotos.map((f, i) => `<div class="previa"><img src="${f.url}" alt="Foto ${i + 1}">
      <button type="button" class="boton boton-icono" data-quitar="${i}" aria-label="Quitar foto ${i + 1}">✕</button></div>`).join("");
    $$("[data-quitar]", previas).forEach((b) => b.addEventListener("click", () => {
      const [quitada] = fotos.splice(Number(b.dataset.quitar), 1);
      URL.revokeObjectURL(quitada.url);
      pintar();
    }));
  };
  $("#fotos").addEventListener("change", (e) => {
    for (const archivo of e.target.files) {
      if (fotos.length >= MAX_FOTOS) { avisar(`Máximo ${MAX_FOTOS} fotos.`, "error"); break; }
      if (!TIPOS_FOTO.includes(archivo.type)) { avisar(`${archivo.name}: solo JPG, PNG o WEBP.`, "error"); continue; }
      if (archivo.size > MAX_BYTES) { avisar(`${archivo.name}: pesa más de 10 MB.`, "error"); continue; }
      fotos.push({ archivo, url: URL.createObjectURL(archivo) });
    }
    e.target.value = "";
    pintar();
  });
}

async function enviar(form, fotos) {
  const d = new FormData(form);
  if (!d.get("latitud")) {
    throw Object.assign(new Error("Marca en el mapa dónde está la falla."), { campos: {} });
  }
  const falla = {
    idCategoria: Number(d.get("idCategoria")) || null,
    idZona: Number(d.get("idZona")) || null,
    descripcion: d.get("descripcion"),
    latitud: Number(d.get("latitud")),
    longitud: Number(d.get("longitud")),
    nivelGravedad: d.get("nivelGravedad") ? Number(d.get("nivelGravedad")) : null,
  };
  let respuesta;
  if (d.get("tipo") === "INCIDENTE") {
    const testigos = $$(".testigo").map((t) => ({ nombre: $(".t-nombre", t).value.trim(), contacto: $(".t-contacto", t).value.trim() }))
      .filter((t) => t.nombre || t.contacto);
    respuesta = await api.crearIncidente({
      falla,
      tipoDano: d.get("tipoDano"),
      descripcionDano: d.get("descripcionDano"),
      fechaHoraHecho: d.get("fechaHoraHecho") ? new Date(d.get("fechaHoraHecho")).toISOString() : null,
      huboAtencionMedica: d.get("huboAtencionMedica") === "on",
      idEntidad: Number(d.get("idEntidad")) || null,
      testigos,
    });
  } else {
    respuesta = { reporte: await api.crearCondicion(falla), antecedentes: [] };
  }
  // las fotos se suben después de crear el reporte; si alguna falla, el reporte ya existe
  const fallidas = [];
  for (const f of fotos) {
    try { await api.subirFoto(respuesta.reporte.id, f.archivo); } catch (e) { fallidas.push(`${f.archivo.name}: ${e.message}`); }
    URL.revokeObjectURL(f.url);
  }
  return { ...respuesta, fallidas, totalFotos: fotos.length };
}

function mostrarResultado(contenedor, { reporte, antecedentes, diasConocimientoPrevio, fallidas, totalFotos }) {
  const incidente = reporte.tipo === "INCIDENTE";
  contenedor.innerHTML = `
    <div class="contenido vista">
      <div class="resultado">
        <div class="resultado-icono" aria-hidden="true">✓</div>
        <h1>Reporte #${reporte.id} recibido</h1>
        <p class="texto-2">Un operador lo revisará. Puedes seguir su estado en <a href="#/mis-reportes">Mis reportes</a>.
          ${totalFotos ? `Fotos guardadas: ${totalFotos - fallidas.length} de ${totalFotos}.` : ""}</p>
        ${fallidas.length ? `<p class="error">${fallidas.map(escapar).join("<br>")}</p>` : ""}
        ${incidente ? (antecedentes.length ? `
          <div class="hallazgo">
            <p class="etiqueta-superior">La entidad ya sabía</p>
            <p class="hallazgo-cifra">${plural(antecedentes.length, "reporte previo", "reportes previos")}</p>
            <p>En ese mismo punto ya se había reportado esta falla. El primero, el ${fecha(antecedentes[0].fechaReporte)}:
              <b>${plural(diasConocimientoPrevio, "día", "días")}</b> antes de lo que te pasó.</p>
          </div>` : `<p class="nota">No encontramos reportes previos en ese punto. Tu caso igual queda documentado.</p>`) : ""}
        ${incidente ? `<p>Cuando un operador valide tu incidente, podrás <b>buscar abogado</b> desde
          <a href="#/mis-reportes">Mis reportes</a> y elegir entre los que se interesen.</p>` : ""}
        <div class="fila-acciones centrada">
          <a class="boton" href="#/mapa/reporte/${reporte.id}">Ver en el mapa</a>
          <a class="boton boton-claro" href="#/reportar" id="otro">Hacer otro reporte</a>
        </div>
      </div>
    </div>`;
  // volver a "#/reportar" estando ya en esa ruta no dispara hashchange: se recarga a mano
  $("#otro").addEventListener("click", (e) => { e.preventDefault(); mostrar(contenedor); });
}
