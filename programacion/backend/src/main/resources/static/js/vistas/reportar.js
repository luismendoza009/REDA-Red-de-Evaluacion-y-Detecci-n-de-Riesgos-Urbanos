// =============================================================================
// Reportar una falla: CONDICIÓN (antecedente, sin daño) o INCIDENTE (hubo un daño).
// Ubicación en un mini-mapa (clic o GPS del celular), fotos con vista previa, y para incidentes:
// fecha del hecho, tipo de daño, atención médica, entidad y testigos.
// Al crear un incidente, el sistema muestra cuántos reportes previos había en ese punto.
// =============================================================================
import { api, sesion } from "../api.js";
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
  const hoy = new Intl.DateTimeFormat("en-CA", {
    year: "numeric", month: "2-digit", day: "2-digit", timeZone: "America/Bogota",
  }).format(new Date());
  contenedor.innerHTML = `
    <div class="contenido vista">
      <header class="encabezado">
        <p class="etiqueta-superior">Nuevo reporte</p>
        <h1>Reportar una falla</h1>
        <p class="texto-2">Cada reporte queda con fecha, ubicación y fotos. Si después alguien sufre un daño en ese mismo
          punto, tu reporte ayuda a demostrar que la entidad responsable ya lo sabía.</p>
      </header>
      <form id="f-reporte" class="formulario tarjeta-formulario" novalidate>
        <aside class="borrador-local nota" aria-label="Guardado del borrador">
          <p>Este borrador se guarda solo en este dispositivo. Se conservan el tipo, la categoría, el barrio, el punto, la descripción general y la gravedad; las fotos, los testigos y los datos del daño personal no se guardan.</p>
          <div class="borrador-local-acciones">
            <span id="borrador-estado" role="status" aria-live="polite">Al completar el formulario, se guardará automáticamente.</span>
            <div class="fila-acciones">
              <button type="button" class="boton" id="borrador-restaurar" hidden>Restaurar borrador</button>
              <button type="button" class="boton" id="borrador-eliminar" hidden>Eliminar borrador</button>
            </div>
          </div>
        </aside>
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
          <div class="busqueda-direccion">
            <label for="direccion-busqueda">Buscar dirección o lugar
              <input id="direccion-busqueda" type="search" maxlength="200" autocomplete="off"
                placeholder="Ej.: Calle 16 con Carrera 9, Valledupar">
            </label>
            <small>Al buscar, la dirección y datos técnicos de conexión se envían a Nominatim de OpenStreetMap.
              REDA conserva temporalmente la consulta y sus resultados en memoria para reducir solicitudes.
              No incluyas datos personales. Consulta la
              <a href="https://operations.osmfoundation.org/policies/nominatim/" target="_blank" rel="noopener noreferrer">política de uso</a>
              y la <a href="https://wiki.osmfoundation.org/wiki/Privacy_Policy" target="_blank" rel="noopener noreferrer">política de privacidad</a>.
              Solo se consulta cuando pulsas el botón.</small>
            <label class="casilla"><input type="checkbox" id="consentimiento-direccion">
              <span>Entiendo que la dirección escrita se enviará al servicio público de OpenStreetMap para buscarla.</span></label>
            <div class="fila-acciones">
              <button type="button" class="boton" id="btn-buscar-direccion" disabled>Buscar dirección</button>
              <span id="estado-busqueda-direccion" role="status" aria-live="polite">Activa el consentimiento para buscar.</span>
            </div>
            <ul id="resultados-direccion" class="resultados-direccion" aria-label="Resultados de búsqueda"></ul>
            <small class="atribucion-mapa">Búsqueda y mapa: © <a href="https://www.openstreetmap.org/copyright"
              target="_blank" rel="noopener noreferrer">OpenStreetMap contributors</a>.</small>
          </div>
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
            <label>Fecha del hecho<input type="date" name="fechaHecho" max="${hoy}"></label>
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
  const actualizarProgresoFoto = conectarFotos(fotos);
  const borrarBorrador = conectarBorrador(form, selector);
  form.descripcion.addEventListener("input", () => { $("#contador").textContent = form.descripcion.value.length; });

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    await conBotonOcupado(form.querySelector("button[type=submit]"), async () => {
      const entradaFotos = $("#fotos");
      const bloquearFotos = (bloquear) => {
        entradaFotos.disabled = bloquear;
        $$("[data-quitar]", $("#previas")).forEach((boton) => { boton.disabled = bloquear; });
      };
      bloquearFotos(true);
      try {
        const resultado = await enviar(form, fotos, actualizarProgresoFoto);
        const borradorEliminado = borrarBorrador();
        selector.remove();
        mapaPrincipal.invalidar();
        mostrarResultado(contenedor, resultado);
        if (!borradorEliminado) avisar("El reporte se envió, pero no se pudo borrar su copia local. Elimínala desde el formulario si este dispositivo es compartido.", "error");
      } catch (err) {
        bloquearFotos(false);
        mostrarErrores(form, err);
        form.querySelector("[aria-invalid]")?.scrollIntoView({ behavior: "smooth", block: "center" });
      }
    });
  });
}

function crearSelector(form, zonas) {
  const m = L.map("mapa-selector", { zoomControl: true }).setView(VALLEDUPAR, 13);
  L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19,
    className: "teselas-oscuras",
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap contributors</a>',
  }).addTo(m);
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
      form.idZona.dispatchEvent(new Event("change", { bubbles: true }));
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
    form.latitud.dispatchEvent(new Event("input", { bubbles: true }));
  };
  const restaurarPunto = (lat, lon) => {
    const ll = [Number(lat), Number(lon)];
    if (!ll.every(Number.isFinite) || Math.abs(ll[0]) > 90 || Math.abs(ll[1]) > 180) return;
    const punto = L.latLng(ll);
    if (marcador) marcador.setLatLng(punto);
    else marcador = L.circleMarker(punto, { radius: 9, color: "#f0efec", weight: 3, fillColor: "#d95926", fillOpacity: 1 }).addTo(m);
    form.latitud.value = ll[0].toFixed(6);
    form.longitud.value = ll[1].toFixed(6);
    $("#coordenadas").textContent = `Punto marcado: ${form.latitud.value}, ${form.longitud.value}`;
    m.setView(punto, 17);
    barrioAMano = Boolean(form.idZona.value);
    pista.textContent = barrioAMano
      ? "Barrio restaurado desde tu borrador. Cámbialo si no corresponde."
      : "Se sugiere un barrio para el punto restaurado.";
    if (!barrioAMano) sugerirBarrio(ll[0], ll[1]);
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
  conectarBusquedaDireccion(m, marcar);
  setTimeout(() => m.invalidateSize(), 50);
  return { remove: () => m.remove(), restaurarPunto };
}

function conectarBusquedaDireccion(mapa, marcar) {
  const campo = $("#direccion-busqueda");
  const consentimiento = $("#consentimiento-direccion");
  const boton = $("#btn-buscar-direccion");
  const estado = $("#estado-busqueda-direccion");
  const resultados = $("#resultados-direccion");
  let buscando = false;
  const actualizarBoton = () => {
    boton.disabled = buscando || !consentimiento.checked || campo.value.trim().length < 3;
  };
  const buscar = async () => {
    const consulta = campo.value.trim();
    if (!consentimiento.checked || consulta.length < 3 || buscando) return;
    buscando = true;
    actualizarBoton();
    resultados.replaceChildren();
    estado.textContent = "Buscando dirección…";
    try {
      const lugares = await api.buscarDireccion(consulta);
      if (!lugares.length) {
        estado.textContent = "No encontramos resultados. Prueba con otra dirección o marca el punto en el mapa.";
        return;
      }
      estado.textContent = `Elige un resultado (${lugares.length}) para marcar el punto en el mapa.`;
      lugares.forEach((lugar) => {
        const elemento = document.createElement("li");
        const opcion = document.createElement("button");
        opcion.type = "button";
        opcion.className = "boton resultado-direccion";
        opcion.textContent = lugar.nombre;
        opcion.addEventListener("click", () => {
          marcar(lugar.latitud, lugar.longitud);
          mapa.setView([lugar.latitud, lugar.longitud], 17);
          estado.textContent = `Punto seleccionado: ${lugar.nombre}. Verifica que el marcador esté en el lugar correcto.`;
          resultados.replaceChildren();
        });
        elemento.append(opcion);
        resultados.append(elemento);
      });
    } catch (error) {
      estado.textContent = error.message || "No se pudo buscar la dirección. Usa el mapa o el GPS.";
    } finally {
      buscando = false;
      actualizarBoton();
    }
  };
  consentimiento.addEventListener("change", actualizarBoton);
  campo.addEventListener("input", actualizarBoton);
  boton.addEventListener("click", buscar);
  campo.addEventListener("keydown", (evento) => {
    if (evento.key === "Enter") {
      evento.preventDefault();
      buscar();
    }
  });
}

function conectarBorrador(form, selector) {
  const estado = $("#borrador-estado");
  const restaurar = $("#borrador-restaurar");
  const eliminar = $("#borrador-eliminar");
  const idUsuario = Number(sesion.actual()?.idUsuario);
  if (!Number.isSafeInteger(idUsuario) || idUsuario < 1) {
    estado.textContent = "No se pudo asociar el borrador a tu cuenta; el guardado automático está desactivado.";
    return () => true;
  }
  const clave = `reda.borrador.reporte.${idUsuario}`;
  let temporizador = null;
  let hayBorrador = false;
  let pausado = false;

  const mostrarError = (accion, error) => {
    estado.textContent = `No se pudo ${accion} el borrador en este dispositivo.`;
    console.error(`No se pudo ${accion} el borrador local del reporte.`, error);
  };
  const quitar = (notificar = false) => {
    clearTimeout(temporizador);
    try {
      localStorage.removeItem(clave);
      hayBorrador = false;
      restaurar.hidden = true;
      eliminar.hidden = true;
      estado.textContent = "Borrador eliminado de este dispositivo.";
      if (notificar) avisar("Borrador eliminado de este dispositivo.", "exito");
      return true;
    } catch (error) {
      mostrarError("eliminar", error);
      if (notificar) avisar("No se pudo eliminar el borrador de este dispositivo.", "error");
      return false;
    }
  };
  const valorBorrador = () => ({
    version: 1,
    guardadoEn: new Date().toISOString(),
    tipo: form.tipo.value,
    idCategoria: form.idCategoria.value,
    idZona: form.idZona.value,
    descripcion: form.descripcion.value,
    latitud: form.latitud.value,
    longitud: form.longitud.value,
    nivelGravedad: form.querySelector('input[name="nivelGravedad"]:checked')?.value ?? "",
  });
  const guardar = () => {
    temporizador = null;
    if (pausado) return;
    const borrador = valorBorrador();
    const tieneContenido = borrador.descripcion.trim() || borrador.idCategoria || borrador.idZona
      || (borrador.latitud && borrador.longitud);
    if (!tieneContenido) {
      if (hayBorrador) quitar();
      return;
    }
    try {
      localStorage.setItem(clave, JSON.stringify(borrador));
      hayBorrador = true;
      restaurar.hidden = false;
      eliminar.hidden = false;
      estado.textContent = `Guardado automáticamente en este dispositivo a las ${new Date(borrador.guardadoEn).toLocaleTimeString("es-CO", { hour: "2-digit", minute: "2-digit" })}.`;
    } catch (error) {
      mostrarError("guardar", error);
    }
  };
  const programarGuardado = () => {
    clearTimeout(temporizador);
    estado.textContent = "Guardando borrador…";
    temporizador = setTimeout(guardar, 450);
  };
  const valorGuardado = (() => {
    try {
      const texto = localStorage.getItem(clave);
      if (!texto) return null;
      const borrador = JSON.parse(texto);
      if (borrador?.version !== 1 || typeof borrador.descripcion !== "string") {
        localStorage.removeItem(clave);
        return null;
      }
      return borrador;
    } catch (error) {
      mostrarError("leer", error);
      return null;
    }
  })();

  if (valorGuardado) {
    hayBorrador = true;
    restaurar.hidden = false;
    eliminar.hidden = false;
    const fechaGuardado = new Date(valorGuardado.guardadoEn);
    estado.textContent = `Hay un borrador de ${Number.isNaN(fechaGuardado.valueOf()) ? "una fecha anterior" : fechaGuardado.toLocaleString("es-CO", { dateStyle: "short", timeStyle: "short" })}.`;
    restaurar.addEventListener("click", () => {
      pausado = true;
      const tipos = $$('input[name="tipo"]', form);
      const tipoGuardado = tipos.find((radio) => radio.value === valorGuardado.tipo);
      if (tipoGuardado) {
        tipoGuardado.checked = true;
        tipoGuardado.dispatchEvent(new Event("change", { bubbles: true }));
      }
      if ([...form.idCategoria.options].some((option) => option.value === valorGuardado.idCategoria)) {
        form.idCategoria.value = valorGuardado.idCategoria;
      }
      if ([...form.idZona.options].some((option) => option.value === valorGuardado.idZona)) {
        form.idZona.value = valorGuardado.idZona;
      }
      form.descripcion.value = valorGuardado.descripcion.slice(0, 2000);
      $("#contador").textContent = form.descripcion.value.length;
      const gravedad = form.querySelector(`input[name="nivelGravedad"][value="${Number(valorGuardado.nivelGravedad)}"]`);
      if (gravedad) gravedad.checked = true;
      selector.restaurarPunto(valorGuardado.latitud, valorGuardado.longitud);
      pausado = false;
      estado.textContent = "Borrador restaurado. Los datos del daño, los testigos y las fotos debes agregarlos de nuevo.";
      avisar("Borrador restaurado. Revisa los datos antes de enviar.", "exito");
    });
  }
  eliminar.addEventListener("click", () => quitar(true));
  form.addEventListener("input", programarGuardado);
  form.addEventListener("change", programarGuardado);
  return () => quitar();
}

function conectarTipo(form) {
  const actualizar = () => {
    const incidente = form.tipo.value === "INCIDENTE";
    $("#datos-incidente").hidden = !incidente;
    form.fechaHecho.required = incidente;
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
    previas.innerHTML = fotos.map((f, i) => `<div class="previa" data-foto-id="${f.id}"><img src="${f.url}" alt="Foto ${i + 1}">
      <div class="previa-estado"><small data-foto-estado role="status">Lista para enviar</small>
        <progress data-foto-progreso max="100" value="0" aria-label="Progreso de la foto ${i + 1}" hidden></progress></div>
      <button type="button" class="boton boton-icono" data-quitar="${i}" aria-label="Quitar foto ${i + 1}">✕</button></div>`).join("");
    $$("[data-quitar]", previas).forEach((b) => b.addEventListener("click", () => {
      const [quitada] = fotos.splice(Number(b.dataset.quitar), 1);
      URL.revokeObjectURL(quitada.url);
      pintar();
    }));
  };
  const actualizarProgreso = (foto, porcentaje, texto) => {
    const tarjeta = previas.querySelector(`[data-foto-id="${foto.id}"]`);
    if (!tarjeta) return;
    const progreso = $("[data-foto-progreso]", tarjeta);
    const estado = $("[data-foto-estado]", tarjeta);
    progreso.hidden = false;
    if (porcentaje == null) progreso.removeAttribute("value");
    else progreso.value = porcentaje;
    estado.textContent = texto;
  };
  let siguienteId = 0;
  $("#fotos").addEventListener("change", (e) => {
    for (const archivo of e.target.files) {
      if (fotos.length >= MAX_FOTOS) { avisar(`Máximo ${MAX_FOTOS} fotos.`, "error"); break; }
      if (!TIPOS_FOTO.includes(archivo.type)) { avisar(`${archivo.name}: solo JPG, PNG o WEBP.`, "error"); continue; }
      if (archivo.size > MAX_BYTES) { avisar(`${archivo.name}: pesa más de 10 MB.`, "error"); continue; }
      fotos.push({ id: siguienteId++, archivo, url: URL.createObjectURL(archivo) });
    }
    e.target.value = "";
    pintar();
  });
  return actualizarProgreso;
}

async function enviar(form, fotos, actualizarProgreso) {
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
      fechaHecho: d.get("fechaHecho") || null,
      huboAtencionMedica: d.get("huboAtencionMedica") === "on",
      idEntidad: Number(d.get("idEntidad")) || null,
      testigos,
    });
  } else {
    respuesta = { reporte: await api.crearCondicion(falla), antecedentes: [] };
  }
  // las fotos se suben después de crear el reporte; si alguna falla, el reporte ya existe
  const fallidas = [];
  for (const [indice, f] of fotos.entries()) {
    actualizarProgreso(f, null, `Subiendo foto ${indice + 1} de ${fotos.length}…`);
    try {
      await api.subirFoto(respuesta.reporte.id, f.archivo, (porcentaje) => {
        actualizarProgreso(f, porcentaje, porcentaje == null ? "Subiendo foto…" : `Subiendo… ${porcentaje}%`);
      });
      actualizarProgreso(f, 100, "Foto subida correctamente.");
    } catch (e) {
      fallidas.push(`${f.archivo.name}: ${e.message}`);
      actualizarProgreso(f, null, `No se pudo subir: ${e.message}`);
    }
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
