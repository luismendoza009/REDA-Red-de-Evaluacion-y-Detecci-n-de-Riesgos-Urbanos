// =============================================================================
// Mis reportes (ciudadano): estado de cada reporte y, para incidentes validados, "Buscar abogado".
// =============================================================================
import { api } from "../api.js";
import { $$, escapar, fecha, plural, avisar, dialogo, cargando, vacio, chipSituacion, situacionDeCaso } from "../ui.js";
import * as mapa from "./mapa.js";

const PASOS = ["Pendiente", "En revisión", "Validado", "En proceso", "Solucionado"];

export async function mostrar(contenedor) {
  contenedor.innerHTML = cargando();
  let reportes;
  try {
    reportes = await api.misReportes();
  } catch (e) {
    contenedor.innerHTML = `<div class="contenido"><p class="error">${escapar(e.message)}</p></div>`;
    return;
  }
  const incidentes = reportes.filter((r) => r.tipo === "INCIDENTE").length;
  contenedor.innerHTML = `
    <div class="contenido vista">
      <header class="encabezado encabezado-accion">
        <div>
          <p class="etiqueta-superior">Ciudadano</p>
          <h1>Mis reportes</h1>
          <p class="texto-2">${plural(reportes.length, "reporte", "reportes")} · ${plural(incidentes, "incidente", "incidentes")}</p>
        </div>
        <a class="boton boton-claro" href="#/reportar">＋ Nuevo reporte</a>
      </header>
      ${reportes.length ? `<ul class="rejilla">${reportes.map(tarjeta).join("")}</ul>`
        : vacio("Aún no has hecho reportes.", '<a class="boton boton-claro" href="#/reportar">Hacer mi primer reporte</a>')}
    </div>`;

  $$("[data-buscar]", contenedor).forEach((b) => b.addEventListener("click", () => buscarAbogado(Number(b.dataset.buscar), contenedor)));
}

function tarjeta(r) {
  const incidente = r.tipo === "INCIDENTE";
  const rechazado = r.estado === "Rechazado";
  const paso = PASOS.indexOf(r.estado);
  return `
    <li class="tarjeta-pagina">
      <div class="tarjeta-cabeza">
        <span class="tarjeta-titulo">${incidente ? '<span class="tipo-incidente">⚠ Incidente</span> · ' : ""}${escapar(r.categoria)}</span>
        <time class="tarjeta-fecha">${fecha(r.fechaReporte)}</time>
      </div>
      <p class="texto-2">${escapar(r.zona)} · Reporte #${r.id} ${r.numFotos ? `· 🔒 ${plural(r.numFotos, "foto", "fotos")}` : ""}</p>
      <p class="tarjeta-desc">${escapar(r.descripcion)}</p>
      ${rechazado ? `<p class="nota nota-error">El equipo de revisión rechazó este reporte. Puedes ver el motivo en su línea de tiempo.</p>`
        : `<ol class="progreso" aria-label="Estado: ${escapar(r.estado)}">${PASOS.map((p, i) =>
          `<li class="${i <= paso ? "hecho" : ""} ${i === paso ? "actual" : ""}"><span>${p}</span></li>`).join("")}</ol>`}
      <div class="tarjeta-pie">
        ${r.estadoCaso ? `${chipSituacion(situacionDeCaso(r.estadoCaso))}<span class="estado">Caso: ${escapar(r.estadoCaso)}</span>` : ""}
        <span class="espaciador"></span>
        <a class="boton" href="#/mapa/reporte/${r.id}">Ver detalle y fotos</a>
        ${r.puedeBuscarAbogado ? `<button class="boton boton-claro" data-buscar="${r.id}">Buscar abogado</button>` : ""}
        ${r.idCaso ? `<a class="boton" href="#/mis-casos">Ver mi caso</a>` : ""}
        ${incidente && !r.idCaso && !r.puedeBuscarAbogado && !rechazado
          ? `<span class="texto-2 pequeno">Podrás buscar abogado cuando un operador valide el incidente.</span>` : ""}
      </div>
    </li>`;
}

async function buscarAbogado(idReporte, contenedor) {
  const ok = await dialogo({
    titulo: "Buscar abogado",
    aceptar: "Autorizo y publicar",
    mensaje: `<p>Los abogados verificados de REDA verán un <b>resumen anónimo</b> de tu caso: categoría, barrio, fecha,
      tipo de daño, si hubo atención médica y cuántos reportes previos hay en el punto.</p>
      <p><b>No verán</b> tu nombre, tu correo, tus fotos, tus testigos ni la descripción de tu daño.</p>
      <p>Los que se interesen se postularán y <b>tú eliges</b> con quién seguir. Solo el abogado que elijas verá el
      expediente completo. REDA no presta asesoría jurídica ni garantiza el resultado.</p>`,
  });
  if (!ok) return;
  try {
    await api.solicitarAbogado(idReporte);
    mapa.invalidar();
    avisar("Tu caso quedó publicado. Te mostraremos aquí a los abogados que se postulen.", "exito");
    location.hash = "#/mis-casos";
  } catch (e) {
    avisar(e.message, "error");
    mostrar(contenedor);
  }
}
