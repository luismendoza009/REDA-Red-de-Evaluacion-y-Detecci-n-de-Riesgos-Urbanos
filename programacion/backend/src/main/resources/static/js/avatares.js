// =============================================================================
// Avatares: foto de perfil con las iniciales como respaldo, e insignia de abogado verificado.
// La foto se pide con el token (no es pública), por eso no basta un <img src>: se descarga
// como blob y se guarda en memoria para no pedirla otra vez en la misma sesión.
// =============================================================================
import { api } from "./api.js";
import { $$, escapar } from "./ui.js";

const enMemoria = new Map(); // ruta con versión -> URL local del blob

export const iniciales = (nombre) =>
  String(nombre ?? "").trim().split(/\s+/).slice(0, 2).map((p) => p[0] ?? "").join("").toUpperCase();

/** Marca un avatar: muestra iniciales y, si hay foto, cargarAvatares() la pone encima. */
export function avatar(nombre, foto, clase = "") {
  return `<span class="avatar ${clase}" aria-hidden="true"${foto ? ` data-foto="${escapar(foto)}"` : ""}>${escapar(iniciales(nombre))}</span>`;
}

/** Insignia visible SOLO si REDA verificó la tarjeta profesional. */
export const insigniaVerificado = (verificado) => (verificado
  ? `<span class="verificado" title="REDA verificó su tarjeta profesional"><span aria-hidden="true">✓</span> Abogado verificado</span>`
  : "");

/** Reemplaza las iniciales por la foto en todos los avatares con data-foto dentro de `raiz`. */
export async function cargarAvatares(raiz = document) {
  await Promise.all($$("[data-foto]", raiz).map(async (el) => {
    const ruta = el.dataset.foto;
    try {
      if (!enMemoria.has(ruta)) enMemoria.set(ruta, URL.createObjectURL(await api.archivo(ruta)));
      if (el.dataset.foto !== ruta) return; // cambió mientras descargaba
      el.innerHTML = `<img src="${enMemoria.get(ruta)}" alt="">`;
      el.classList.add("con-foto");
    } catch {
      /* sin permiso o sin archivo: se quedan las iniciales */
    }
  }));
}

/** Cambia el avatar de un elemento ya pintado (p. ej. la barra superior tras subir una foto nueva). */
export function ponerFoto(el, nombre, foto) {
  el.classList.remove("con-foto");
  el.textContent = iniciales(nombre);
  if (foto) el.dataset.foto = foto; else delete el.dataset.foto;
  return cargarAvatares(el.parentElement ?? document);
}
