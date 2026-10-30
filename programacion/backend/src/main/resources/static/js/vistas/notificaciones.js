import { api } from "../api.js";
import { $, $$, escapar, fechaHora, avisar, cargando, vacio } from "../ui.js";

export async function mostrar(contenedor) {
  contenedor.innerHTML = cargando();
  let respuesta;
  try {
    respuesta = await api.notificaciones();
  } catch (error) {
    contenedor.innerHTML = `<div class="contenido"><p class="error">${escapar(error.message)}</p></div>`;
    return;
  }
  const { items, noLeidas } = respuesta;
  contenedor.innerHTML = `
    <div class="contenido vista">
      <header class="encabezado">
        <p class="etiqueta-superior">Avisos personales</p>
        <h1>Notificaciones</h1>
        <p class="texto-2">${noLeidas ? `${noLeidas} sin leer` : "Estás al día."}</p>
      </header>
      ${items.length ? `<ul class="lista-notificaciones">${items.map(tarjeta).join("")}</ul>`
        : vacio("Todavía no tienes notificaciones.", '<a class="boton" href="#/mis-reportes">Ver mis reportes</a>')}
    </div>`;

  $$("[data-notificacion]", contenedor).forEach((boton) => boton.addEventListener("click", async () => {
    boton.disabled = true;
    try {
      if (boton.dataset.leida !== "true") {
        await api.marcarNotificacionLeida(Number(boton.dataset.notificacion));
      }
      location.hash = `#/mapa/reporte/${Number(boton.dataset.reporte)}`;
    } catch (error) {
      boton.disabled = false;
      avisar(error.message, "error");
    }
  }));
}

function tarjeta(notificacion) {
  const resumen = `El estado de tu reporte #${notificacion.idReporte} cambió de ${notificacion.estadoAnterior} a ${notificacion.estadoNuevo}.`;
  return `
    <li>
      <button type="button" class="notificacion ${notificacion.leida ? "" : "no-leida"}"
        data-notificacion="${notificacion.id}" data-reporte="${notificacion.idReporte}"
        data-leida="${notificacion.leida}" aria-label="${escapar(resumen)} ${notificacion.leida ? "" : "Sin leer."}">
        <span class="notificacion-punto" aria-hidden="true"></span>
        <span class="notificacion-contenido">
          <strong>${escapar(resumen)}</strong>
          <small>${escapar(fechaHora(notificacion.fechaCreacion))} · Abrir la línea de tiempo del reporte</small>
        </span>
      </button>
    </li>`;
}
