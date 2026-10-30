// =============================================================================
// Utilidades de interfaz compartidas por todas las vistas.
// =============================================================================

export const $ = (sel, raiz = document) => raiz.querySelector(sel);
export const $$ = (sel, raiz = document) => [...raiz.querySelectorAll(sel)];

/** Todo texto que viene de usuarios se inserta escapado (prevención de XSS). */
export const escapar = (t) => String(t ?? "").replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));

const fmtFecha = new Intl.DateTimeFormat("es-CO", { day: "numeric", month: "short", year: "numeric" });
const fmtFechaUtc = new Intl.DateTimeFormat("es-CO", { day: "numeric", month: "short", year: "numeric", timeZone: "UTC" });
const fmtPesos = new Intl.NumberFormat("es-CO", { style: "currency", currency: "COP", maximumFractionDigits: 0 });

/** "2026-05-19" (solo fecha) se formatea en UTC para no correr el día por la zona horaria. */
export const fecha = (v) => (/^\d{4}-\d{2}-\d{2}$/.test(v) ? fmtFechaUtc : fmtFecha).format(new Date(v));
export const pesos = (v) => fmtPesos.format(Number(v));
export const plural = (n, uno, varios) => `${n} ${n === 1 ? uno : varios}`;

export const SITUACIONES = {
  CON_ABOGADO: { nombre: "Con abogado", plural: "Con abogado", color: "var(--con-abogado)", hex: "#3987e5",
                 explicacion: "Un abogado tomó el caso y está en curso." },
  ABIERTO:     { nombre: "Abierto", plural: "Abiertos", color: "var(--abierto)", hex: "#d95926",
                 explicacion: "En revisión, validado o buscando abogado." },
  CERRADO:     { nombre: "Cerrado", plural: "Cerrados", color: "var(--cerrado)", hex: "#199e70",
                 explicacion: "El caso se cerró, nadie lo tomó o la falla fue solucionada." },
};
export const ORDEN_SITUACIONES = ["CON_ABOGADO", "ABIERTO", "CERRADO"];

export const DANOS = { LESION: "Lesión personal", DANO_MATERIAL: "Daño material", AMBOS: "Lesión y daño material" };
export const ESTADOS = { PENDIENTE: "Pendiente", EN_REVISION: "En revisión", VALIDADO: "Validado",
                         EN_PROCESO: "En proceso", SOLUCIONADO: "Solucionado", RECHAZADO: "Rechazado" };

export function chipSituacion(clave) {
  const s = SITUACIONES[clave];
  return s ? `<span class="situacion"><span class="muestra" style="background:${s.color}"></span>${s.nombre}</span>` : "";
}

/** Situación pública de un caso según su estado (misma regla que la vista v_mapa_publico). */
export function situacionDeCaso(estadoCaso) {
  if (["Cerrado", "Rechazado"].includes(estadoCaso)) return "CERRADO";
  if (estadoCaso === "Aceptado por abogado") return "CON_ABOGADO";
  return "ABIERTO";
}

// ------------------------------------------------------------------ avisos (toasts)
export function avisar(mensaje, tipo = "info") {
  let zona = $("#avisos");
  if (!zona) {
    zona = document.createElement("div");
    zona.id = "avisos";
    zona.setAttribute("role", "status");
    zona.setAttribute("aria-live", "polite");
    document.body.append(zona);
  }
  const aviso = document.createElement("div");
  aviso.className = `aviso-flotante ${tipo}`;
  aviso.textContent = mensaje;
  zona.append(aviso);
  setTimeout(() => aviso.classList.add("saliendo"), 4200);
  setTimeout(() => aviso.remove(), 4700);
}

// ------------------------------------------------------------------ diálogos
/**
 * Diálogo propio (no confirm() del navegador). Devuelve el valor del campo de texto si se pide
 * (o true), o null si se cancela.
 */
export function dialogo({ titulo, mensaje, aceptar = "Aceptar", peligro = false, campo = null }) {
  return new Promise((resolver) => {
    const d = document.createElement("dialog");
    d.className = "modal";
    d.innerHTML = `
      <form method="dialog" class="modal-cuerpo">
        <h2>${escapar(titulo)}</h2>
        <div class="texto-2">${mensaje}</div>
        ${campo ? `<label>${escapar(campo.etiqueta)}<textarea name="valor" rows="4" minlength="${campo.min ?? 0}"
                     maxlength="${campo.max ?? 1500}" ${campo.requerido ? "required" : ""}
                     placeholder="${escapar(campo.ayuda ?? "")}"></textarea></label>` : ""}
        <div class="modal-acciones">
          <button class="boton" value="cancelar" type="button" data-cancelar>Cancelar</button>
          <button class="boton ${peligro ? "boton-peligro" : "boton-claro"}" value="aceptar" type="submit">${escapar(aceptar)}</button>
        </div>
      </form>`;
    document.body.append(d);
    const cerrar = (valor) => { d.close(); d.remove(); resolver(valor); };
    $("[data-cancelar]", d).addEventListener("click", () => cerrar(null));
    d.addEventListener("cancel", (e) => { e.preventDefault(); cerrar(null); });
    $("form", d).addEventListener("submit", (e) => {
      e.preventDefault();
      cerrar(campo ? $("textarea", d).value.trim() : true);
    });
    d.showModal();
  });
}

// ------------------------------------------------------------------ formularios
/** Muestra los errores de validación del backend junto a cada campo. */
export function mostrarErrores(form, error) {
  $$(".error-campo", form).forEach((e) => e.remove());
  $$("[aria-invalid]", form).forEach((e) => e.removeAttribute("aria-invalid"));
  const general = $(".error", form);
  if (general) general.textContent = Object.keys(error.campos ?? {}).length ? "Revisa los campos marcados." : error.message;
  for (const [campo, mensaje] of Object.entries(error.campos ?? {})) {
    const control = form.querySelector(`[name="${campo.split(".").pop()}"]`);
    if (!control) continue;
    control.setAttribute("aria-invalid", "true");
    const p = document.createElement("p");
    p.className = "error-campo";
    p.textContent = mensaje;
    control.closest("label, .campo")?.append(p);
  }
}

/** Ejecuta una acción mostrando el botón como "ocupado" para evitar doble envío. */
export async function conBotonOcupado(boton, accion) {
  const texto = boton.textContent;
  boton.disabled = true;
  boton.dataset.ocupado = "true";
  try {
    return await accion();
  } finally {
    boton.disabled = false;
    delete boton.dataset.ocupado;
    boton.textContent = texto;
  }
}

export const vacio = (mensaje, accion = "") => `<div class="estado-vacio"><p>${mensaje}</p>${accion}</div>`;
export const cargando = () => `<div class="estado-vacio"><span class="girador" aria-hidden="true"></span><p>Cargando…</p></div>`;
