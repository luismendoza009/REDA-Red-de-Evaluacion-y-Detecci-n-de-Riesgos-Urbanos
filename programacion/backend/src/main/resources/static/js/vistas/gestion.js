// =============================================================================
// Paneles internos
//  - Revisión (operador/administrador): reportes pendientes, primero los incidentes; cambiar su estado.
//  - Abogados (administrador): verificar la tarjeta profesional para activar la cuenta.
// =============================================================================
import { api } from "../api.js";
import { $$, escapar, fecha, plural, avisar, dialogo, cargando, vacio, ESTADOS } from "../ui.js";
import * as mapa from "./mapa.js";

const ACCIONES = {
  EN_REVISION: { texto: "Tomar para revisión", clase: "" },
  VALIDADO: { texto: "Validar", clase: "boton-claro" },
  RECHAZADO: { texto: "Rechazar", clase: "boton-peligro" },
  EN_PROCESO: { texto: "Marcar en proceso", clase: "" },
  SOLUCIONADO: { texto: "Marcar solucionado", clase: "" },
};

export async function revision(contenedor) {
  contenedor.innerHTML = cargando();
  let bandeja;
  try {
    bandeja = await api.bandejaRevision();
  } catch (e) {
    contenedor.innerHTML = `<div class="contenido"><p class="error">${escapar(e.message)}</p></div>`;
    return;
  }
  const incidentes = bandeja.filter((r) => r.tipo === "INCIDENTE").length;
  contenedor.innerHTML = `
    <div class="contenido vista">
      <header class="encabezado">
        <p class="etiqueta-superior">Operador</p>
        <h1>Bandeja de revisión</h1>
        <p class="texto-2">${plural(bandeja.length, "reporte espera", "reportes esperan")} revisión ·
          ${plural(incidentes, "incidente", "incidentes")} primero, porque hubo un daño.
          Valida solo si la foto y la ubicación corresponden a una falla real de infraestructura pública.</p>
      </header>
      ${bandeja.length ? `<ul class="rejilla">${bandeja.map(itemRevision).join("")}</ul>` : vacio("No hay reportes pendientes. ¡Bandeja al día!")}
    </div>`;
  $$("[data-accion]", contenedor).forEach((b) => b.addEventListener("click", () =>
    cambiar(Number(b.dataset.id), b.dataset.accion, contenedor)));
}

function itemRevision(r) {
  return `
    <li class="tarjeta-pagina ${r.tipo === "INCIDENTE" ? "prioritaria" : ""}">
      <div class="tarjeta-cabeza">
        <span class="tarjeta-titulo">${r.tipo === "INCIDENTE" ? '<span class="tipo-incidente">⚠ Incidente</span> · ' : ""}${escapar(r.categoria)}</span>
        <time class="tarjeta-fecha">${fecha(r.fechaReporte)}</time>
      </div>
      <p class="texto-2">${escapar(r.zona)} · Reporte #${r.id} · ${escapar(r.estado)} · ${plural(r.numFotos, "foto", "fotos")}
        ${r.nivelGravedad ? ` · gravedad percibida ${r.nivelGravedad}/5` : ""}</p>
      <p class="tarjeta-desc">${escapar(r.descripcion)}</p>
      <div class="tarjeta-pie">
        <a class="boton" href="#/mapa/reporte/${r.id}">Ver fotos y ubicación</a>
        <span class="espaciador"></span>
        ${r.siguientesEstados.map((s) => `<button class="boton ${ACCIONES[s]?.clase ?? ""}" data-id="${r.id}" data-accion="${s}">
          ${escapar(ACCIONES[s]?.texto ?? ESTADOS[s])}</button>`).join("")}
      </div>
    </li>`;
}

async function cambiar(id, nuevoEstado, contenedor) {
  const rechazo = nuevoEstado === "RECHAZADO";
  const observacion = await dialogo({
    titulo: `${ACCIONES[nuevoEstado]?.texto ?? ESTADOS[nuevoEstado]} · reporte #${id}`,
    aceptar: "Confirmar",
    peligro: rechazo,
    mensaje: rechazo ? "<p>El ciudadano verá el motivo del rechazo.</p>" : "<p>El cambio queda en el historial con tu nombre.</p>",
    campo: { etiqueta: rechazo ? "Motivo del rechazo (obligatorio)" : "Observación (opcional)", requerido: rechazo, max: 1000,
             ayuda: rechazo ? "Ej.: La foto no corresponde a la ubicación." : "" },
  });
  if (observacion === null) return;
  try {
    await api.cambiarEstado(id, nuevoEstado, observacion || null);
    mapa.invalidar();
    avisar(`Reporte #${id}: ${ESTADOS[nuevoEstado]}.`, "exito");
  } catch (e) {
    avisar(e.message, "error");
  }
  revision(contenedor);
}

// ------------------------------------------------------------------ administración de abogados
export async function abogados(contenedor) {
  contenedor.innerHTML = cargando();
  let lista;
  try {
    lista = await api.abogadosAdmin();
  } catch (e) {
    contenedor.innerHTML = `<div class="contenido"><p class="error">${escapar(e.message)}</p></div>`;
    return;
  }
  const pendientes = lista.filter((a) => !a.verificado).length;
  contenedor.innerHTML = `
    <div class="contenido vista">
      <header class="encabezado">
        <p class="etiqueta-superior">Administrador</p>
        <h1>Abogados aliados</h1>
        <p class="texto-2">${plural(pendientes, "solicitud pendiente", "solicitudes pendientes")} de verificación. Antes de
          activar, confirma la tarjeta profesional en el registro oficial (Consejo Superior de la Judicatura, por verificar).</p>
      </header>
      <div class="tabla-contenedor">
        <table class="tabla">
          <thead><tr><th>Abogado</th><th>Tarjeta</th><th>Especialidad</th><th>Experiencia</th><th>Registro</th><th>Estado</th><th></th></tr></thead>
          <tbody>${lista.map((a) => `
            <tr>
              <td><b>${escapar(a.nombre)}</b><br><span class="texto-2 pequeno">${escapar(a.correo)}</span></td>
              <td>${escapar(a.tarjetaProfesional)}</td>
              <td>${escapar(a.especialidad)}<br><span class="texto-2 pequeno">${escapar(a.ciudad)}</span></td>
              <td>${plural(a.aniosExperiencia, "año", "años")}</td>
              <td>${fecha(a.fechaRegistro)}</td>
              <td>${a.verificado ? '<span class="etiqueta etiqueta-ok">Verificado</span>' : '<span class="etiqueta etiqueta-espera">Pendiente</span>'}</td>
              <td>${a.verificado ? "" : `<button class="boton boton-claro" data-verificar="${a.id}">Verificar</button>`}</td>
            </tr>`).join("")}</tbody>
        </table>
      </div>
    </div>`;
  $$("[data-verificar]", contenedor).forEach((b) => b.addEventListener("click", async () => {
    const ok = await dialogo({ titulo: "Verificar abogado", aceptar: "Verificar y activar",
      mensaje: "<p>Confirmo que revisé la tarjeta profesional. El abogado podrá ver casos disponibles y postularse.</p>" });
    if (!ok) return;
    try { await api.verificarAbogado(Number(b.dataset.verificar)); avisar("Abogado verificado.", "exito"); } catch (e) { avisar(e.message, "error"); }
    abogados(contenedor);
  }));
}
