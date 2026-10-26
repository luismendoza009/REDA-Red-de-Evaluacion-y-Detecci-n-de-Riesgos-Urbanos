// =============================================================================
// Vista del abogado
//  - Casos disponibles: resumen ANÓNIMO (sin nombre, fotos, testigos ni descripción del daño) y postulación.
//  - Mis casos: expediente COMPLETO solo de los casos donde el ciudadano lo eligió; cierre del caso.
// =============================================================================
import { api, ErrorApi } from "../api.js";
import { $, $$, escapar, fecha, fechaHora, pesos, plural, avisar, dialogo, cargando, vacio, DANOS } from "../ui.js";
import { cargarFotos, liberarFotos } from "./mapa.js";

function pendienteDeVerificacion(contenedor, e) {
  if (e instanceof ErrorApi && e.estado === 403) {
    contenedor.innerHTML = `
      <div class="contenido vista">
        <div class="resultado">
          <div class="resultado-icono espera" aria-hidden="true">⏳</div>
          <h1>Tu cuenta está en verificación</h1>
          <p class="texto-2">${escapar(e.message)}. Cuando un administrador confirme tu tarjeta profesional podrás ver
            los casos disponibles y postularte. Mientras tanto puedes completar tu perfil en <a href="#/cuenta">Mi cuenta</a>.</p>
        </div>
      </div>`;
    return true;
  }
  return false;
}

export async function disponibles(contenedor) {
  contenedor.innerHTML = cargando();
  let casos;
  try {
    casos = await api.casosDisponibles();
  } catch (e) {
    if (!pendienteDeVerificacion(contenedor, e)) contenedor.innerHTML = `<div class="contenido"><p class="error">${escapar(e.message)}</p></div>`;
    return;
  }
  contenedor.innerHTML = `
    <div class="contenido vista">
      <header class="encabezado">
        <p class="etiqueta-superior">Abogado</p>
        <h1>Casos que buscan abogado</h1>
        <p class="texto-2">Ves un resumen anónimo. Si el ciudadano te elige, verás el expediente completo y se te cobrará la
          cuota fija indicada. REDA no cobra cuota litis ni participa en tus honorarios.</p>
      </header>
      ${casos.length ? `<div class="rejilla">${casos.map(tarjetaDisponible).join("")}</div>`
        : vacio("Por ahora no hay casos buscando abogado. Vuelve pronto.")}
    </div>`;
  $$("[data-postular]", contenedor).forEach((b) => b.addEventListener("click", () => postular(Number(b.dataset.postular), contenedor)));
  $$("[data-retirar]", contenedor).forEach((b) => b.addEventListener("click", () => retirar(Number(b.dataset.retirar), contenedor)));
}

function tarjetaDisponible(c) {
  const fuerte = c.antecedentes >= 2;
  return `
    <article class="tarjeta-pagina">
      <div class="tarjeta-cabeza">
        <span class="tarjeta-titulo">${escapar(c.categoria)}</span>
        <span class="tarjeta-fecha">Publicado ${fecha(c.fechaPublicacion)}</span>
      </div>
      <p class="texto-2">${escapar(c.zona)} · Caso #${c.id}</p>
      <dl class="datos datos-3">
        <div><dt>Daño</dt><dd>${escapar(DANOS[c.tipoDano] ?? c.tipoDano)}</dd></div>
        <div><dt>Atención médica</dt><dd>${c.huboAtencionMedica ? "Sí" : "No"}</dd></div>
        <div><dt>Fecha del hecho</dt><dd>${fecha(c.fechaHecho)}</dd></div>
        <div class="${fuerte ? "destacado" : ""}"><dt>Reportes previos</dt><dd>${c.antecedentes}${c.diasConocimientoPrevio != null ? ` · ${c.diasConocimientoPrevio} días antes` : ""}</dd></div>
        <div><dt>Testigos · fotos</dt><dd>${c.testigos} · ${c.fotos}</dd></div>
        <div><dt>Entidad señalada</dt><dd>${escapar(c.entidadResponsable ?? "Sin identificar")}</dd></div>
      </dl>
      <div class="tarjeta-pie">
        <span class="texto-2 pequeno">${plural(c.postulados, "abogado postulado", "abogados postulados")} · cuota si te eligen: <b>${pesos(c.tarifa)}</b></span>
        <span class="espaciador"></span>
        ${c.miPostulacion === "PENDIENTE"
          ? `<span class="etiqueta etiqueta-mia">Te postulaste</span><button class="boton" data-retirar="${c.id}">Retirar</button>`
          : c.miPostulacion === "RETIRADA" ? `<span class="texto-2 pequeno">Retiraste tu postulación</span>`
          : `<button class="boton boton-claro" data-postular="${c.id}">Postularme</button>`}
      </div>
    </article>`;
}

async function postular(idCaso, contenedor) {
  const mensaje = await dialogo({
    titulo: "Postularme a este caso",
    aceptar: "Enviar postulación",
    mensaje: `<p>El ciudadano verá tu nombre, especialidad, años de experiencia, tu presentación y este mensaje.</p>`,
    campo: { etiqueta: "¿Por qué quieres el caso y cómo lo llevarías?", min: 20, max: 1500, requerido: true,
             ayuda: "Ej.: Llevo casos de reparación directa contra el municipio; con los reportes previos el caso es sólido…" },
  });
  if (!mensaje) return;
  try {
    await api.postular(idCaso, mensaje);
    avisar("Postulación enviada. El ciudadano decidirá.", "exito");
  } catch (e) {
    avisar(Object.values(e.campos ?? {})[0] ? `El mensaje ${Object.values(e.campos)[0]}` : e.message, "error");
  }
  disponibles(contenedor);
}

async function retirar(idCaso, contenedor) {
  const ok = await dialogo({ titulo: "Retirar postulación", mensaje: "<p>El ciudadano dejará de verte entre los interesados.</p>",
                             aceptar: "Retirar", peligro: true });
  if (!ok) return;
  try { await api.retirarPostulacion(idCaso); avisar("Postulación retirada."); } catch (e) { avisar(e.message, "error"); }
  disponibles(contenedor);
}

// ------------------------------------------------------------------ mis casos (expediente completo)
export async function misCasos(contenedor) {
  liberarFotos();
  contenedor.innerHTML = cargando();
  let casos;
  try {
    casos = await api.misCasosAbogado();
  } catch (e) {
    if (!pendienteDeVerificacion(contenedor, e)) contenedor.innerHTML = `<div class="contenido"><p class="error">${escapar(e.message)}</p></div>`;
    return;
  }
  contenedor.innerHTML = `
    <div class="contenido vista">
      <header class="encabezado">
        <p class="etiqueta-superior">Abogado</p>
        <h1>Mis casos</h1>
        <p class="texto-2">Casos en los que el ciudadano te eligió. Tienes acceso al expediente completo.</p>
      </header>
      ${casos.length ? casos.map(expediente).join("") : vacio("Aún no te han elegido en ningún caso.",
        '<a class="boton" href="#/abogado/disponibles">Ver casos disponibles</a>')}
    </div>`;
  for (const c of casos) {
    cargarFotos($(`#fotos-caso-${c.id}`), c.reporte.id, null, false);
  }
  $$("[data-cerrar]", contenedor).forEach((b) => b.addEventListener("click", () => cerrar(Number(b.dataset.cerrar), contenedor)));
}

function expediente(c) {
  const r = c.reporte;
  const i = r.incidente;
  return `
    <section class="caso">
      <header class="caso-cabeza">
        <div>
          <p class="etiqueta-superior">Caso #${c.id} · ${escapar(c.estado)}</p>
          <h2>${escapar(r.categoria)} · ${escapar(r.zona)}</h2>
          <p class="texto-2">Abierto el ${fecha(c.fechaCreacion)}${c.fechaCierre ? ` · cerrado el ${fecha(c.fechaCierre)}` : ""}
            ${c.valorCobrado ? ` · cuota REDA ${pesos(c.valorCobrado)}` : ""}</p>
        </div>
        ${c.estado === "Aceptado por abogado" ? `<button class="boton" data-cerrar="${c.id}">Cerrar caso</button>` : ""}
      </header>
      <div class="expediente">
        <div>
          <h3>Ciudadano</h3>
          <p><b>${escapar(c.nombreCiudadano)}</b><br><a href="mailto:${escapar(c.correoCiudadano)}">${escapar(c.correoCiudadano)}</a></p>
          <h3>El hecho</h3>
          <dl class="datos">
            <div><dt>Fecha y hora</dt><dd>${fechaHora(i.fechaHoraHecho)}</dd></div>
            <div><dt>Daño</dt><dd>${escapar(DANOS[i.tipoDano] ?? i.tipoDano)}</dd></div>
            <div><dt>Atención médica</dt><dd>${i.huboAtencionMedica ? "Sí" : "No"}</dd></div>
            <div><dt>Entidad señalada</dt><dd>${escapar(i.entidadResponsable ?? "Sin identificar")}</dd></div>
          </dl>
          <p class="descripcion"><b>Daño:</b> ${escapar(i.descripcionDano)}</p>
          <p class="descripcion"><b>Falla:</b> ${escapar(r.descripcion)}</p>
          <h3>Testigos</h3>
          ${c.testigos.length ? `<ul class="lista-simple">${c.testigos.map((t) => `<li>${escapar(t.nombre)} · ${escapar(t.contacto)}</li>`).join("")}</ul>`
            : '<p class="texto-2">Sin testigos registrados.</p>'}
        </div>
        <div>
          <h3>Antecedentes en el punto (${c.antecedentes.length})</h3>
          ${c.antecedentes.length ? `<ol class="hitos">${c.antecedentes.map((a) =>
            `<li><span>#${a.idReporte} · ${escapar(a.estado)} · a ${a.distanciaMetros} m</span><time>${fecha(a.fechaReporte)}</time></li>`).join("")}</ol>`
            : '<p class="texto-2">No hay reportes previos en el punto.</p>'}
          <h3>Fotos de evidencia</h3>
          <div id="fotos-caso-${c.id}">${cargando()}</div>
          <h3>Línea de tiempo del reporte</h3>
          <ol class="hitos">${[{ estadoNuevo: "Reportado", fecha: r.fechaReporte }, ...r.historial].map((h) =>
            `<li><span>${escapar(h.estadoNuevo)}</span><time>${fechaHora(h.fecha)}</time></li>`).join("")}</ol>
        </div>
      </div>
      ${c.estado === "Cerrado" && c.observacion ? `<p class="nota">Cierre: ${escapar(c.observacion)}</p>` : ""}
    </section>`;
}

async function cerrar(idCaso, contenedor) {
  const observacion = await dialogo({
    titulo: "Cerrar caso",
    aceptar: "Cerrar caso",
    peligro: true,
    mensaje: "<p>El ciudadano verá el caso como cerrado.</p>",
    campo: { etiqueta: "¿Cómo terminó?", min: 10, max: 1000, requerido: true, ayuda: "Ej.: Conciliación con la entidad…" },
  });
  if (!observacion) return;
  try { await api.cerrarCaso(idCaso, observacion); avisar("Caso cerrado.", "exito"); } catch (e) { avisar(e.message, "error"); }
  misCasos(contenedor);
}
