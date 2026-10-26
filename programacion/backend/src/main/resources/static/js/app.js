// =============================================================================
// REDA · arranque y enrutador
// Rutas con "#/" (no requieren configurar el servidor). Sin sesión solo se ven las pantallas
// de acceso y las páginas informativas; todo lo demás exige iniciar sesión, y cada sección
// exige además el rol correspondiente (el backend lo vuelve a verificar: la interfaz no es
// la que protege los datos, solo evita mostrar opciones que no corresponden).
// =============================================================================
import { sesion } from "./api.js";
import { $, $$, escapar, avisar } from "./ui.js";
import * as acceso from "./vistas/acceso.js";
import * as mapa from "./vistas/mapa.js";
import * as reportar from "./vistas/reportar.js";
import * as misReportes from "./vistas/misReportes.js";
import * as misCasos from "./vistas/misCasos.js";
import * as abogado from "./vistas/abogado.js";
import * as gestion from "./vistas/gestion.js";
import * as cuenta from "./vistas/cuenta.js";
import * as info from "./vistas/info.js";

const ROLES = { CIUDADANO: "Ciudadano", OPERADOR: "Operador", ABOGADO: "Abogado", ADMINISTRADOR: "Administrador", ANALISTA: "Analista" };

/** acceso: solo sin sesión · libre: con o sin sesión · roles: con sesión y uno de esos roles. */
const RUTAS = [
  { patron: /^\/entrar$/, acceso: true, vista: acceso.entrar },
  { patron: /^\/registro$/, acceso: true, vista: acceso.registro },
  { patron: /^\/registro-abogado$/, acceso: true, vista: acceso.registroAbogado },
  { patron: /^\/recuperar$/, acceso: true, vista: acceso.recuperar },
  { patron: /^\/restablecer\/([A-Za-z0-9_-]+)$/, acceso: true, vista: acceso.restablecer },
  { patron: /^\/(terminos|privacidad|ayuda)$/, libre: true, vista: info.mostrar },
  { patron: /^\/mapa(?:\/(punto|reporte)\/(\d+))?$/, vista: mapa.mostrar, esMapa: true },
  { patron: /^\/reportar$/, roles: ["CIUDADANO"], vista: reportar.mostrar },
  { patron: /^\/mis-reportes$/, roles: ["CIUDADANO"], vista: misReportes.mostrar },
  { patron: /^\/mis-casos$/, roles: ["CIUDADANO"], vista: misCasos.mostrar },
  { patron: /^\/abogado\/disponibles$/, roles: ["ABOGADO"], vista: abogado.disponibles },
  { patron: /^\/abogado\/mis-casos$/, roles: ["ABOGADO"], vista: abogado.misCasos },
  { patron: /^\/revision$/, roles: ["OPERADOR", "ADMINISTRADOR"], vista: gestion.revision },
  { patron: /^\/admin\/abogados$/, roles: ["ADMINISTRADOR"], vista: gestion.abogados },
  { patron: /^\/cuenta$/, vista: cuenta.mostrar },
];

const MENU = {
  CIUDADANO: [["#/mapa", "Mapa"], ["#/reportar", "Reportar"], ["#/mis-reportes", "Mis reportes"], ["#/mis-casos", "Mis casos"]],
  ABOGADO: [["#/mapa", "Mapa"], ["#/abogado/disponibles", "Casos disponibles"], ["#/abogado/mis-casos", "Mis casos"]],
  OPERADOR: [["#/mapa", "Mapa"], ["#/revision", "Revisión"]],
  ADMINISTRADOR: [["#/mapa", "Mapa"], ["#/revision", "Revisión"], ["#/admin/abogados", "Abogados"]],
  ANALISTA: [["#/mapa", "Mapa"]],
};

let mensajeSalida = null;
sesion.alSalir((motivo) => {
  mensajeSalida = motivo ?? null;
  location.hash = "#/entrar";
});

async function enrutar() {
  const ruta = location.hash.replace(/^#/, "") || "/";
  const s = sesion.actual();
  const coincidencia = RUTAS.map((r) => ({ r, m: ruta.match(r.patron) })).find((x) => x.m);

  if (!coincidencia) {
    location.replace(s ? "#/mapa" : "#/entrar");
    return;
  }
  const { r, m } = coincidencia;
  if (r.acceso && s) { location.replace("#/mapa"); return; }
  if (!r.acceso && !r.libre && !s) { location.replace("#/entrar"); return; }
  if (r.roles && !r.roles.includes(s.rol)) {
    avisar("Esa sección no está disponible para tu rol.", "error");
    location.replace("#/mapa");
    return;
  }

  if (!s) {
    mostrarPantalla("acceso");
    const contenedor = $("#acceso-contenido");
    await r.vista(contenedor, m.slice(1), { mensaje: mensajeSalida });
    mensajeSalida = null;
    return;
  }

  mostrarPantalla("app");
  armarBarra(s);
  document.body.classList.toggle("en-mapa", !!r.esMapa);
  $$("#menu a").forEach((a) => a.classList.toggle("activo", ruta.startsWith(a.getAttribute("href").slice(1))));
  const pagina = $("#pagina");
  pagina.hidden = !!r.esMapa;
  if (r.esMapa) {
    await r.vista(null, m.slice(1));
  } else {
    mapa.ocultar();
    pagina.scrollTo(0, 0);
    await r.vista(pagina, m.slice(1));
    pagina.focus({ preventScroll: true });
  }
}

function mostrarPantalla(cual) {
  $("#pantalla-acceso").hidden = cual !== "acceso";
  $("#pantalla-app").hidden = cual !== "app";
  document.body.classList.toggle("modo-acceso", cual === "acceso");
}

let barraDe = null;
function armarBarra(s) {
  document.body.dataset.rol = s.rol;
  if (barraDe === s.token) return;
  barraDe = s.token;
  $("#menu").innerHTML = (MENU[s.rol] ?? MENU.ANALISTA).map(([href, texto]) => `<a href="${href}">${escapar(texto)}</a>`).join("");
  $("#usuario-nombre").textContent = s.nombre.split(" ")[0];
  $("#avatar").textContent = s.nombre.split(" ").slice(0, 2).map((p) => p[0]).join("").toUpperCase();
  $("#usuario-rol").innerHTML = `${escapar(s.nombre)}<small>${ROLES[s.rol] ?? s.rol}</small>`;
}

// menús desplegables (usuario y, en celular, secciones)
function alternar(boton, panel) {
  const abrir = boton.getAttribute("aria-expanded") !== "true";
  boton.setAttribute("aria-expanded", abrir);
  panel.hidden = !abrir;
  panel.classList.toggle("abierto", abrir);
}
$("#btn-usuario").addEventListener("click", () => alternar($("#btn-usuario"), $("#usuario-opciones")));
$("#btn-menu").addEventListener("click", () => {
  const abrir = $("#btn-menu").getAttribute("aria-expanded") !== "true";
  $("#btn-menu").setAttribute("aria-expanded", abrir);
  $("#menu").classList.toggle("abierto", abrir);
});
document.addEventListener("click", (e) => {
  if (!e.target.closest(".usuario-menu")) {
    $("#btn-usuario").setAttribute("aria-expanded", "false");
    $("#usuario-opciones").hidden = true;
  }
  if (e.target.closest("#menu a")) {
    $("#btn-menu").setAttribute("aria-expanded", "false");
    $("#menu").classList.remove("abierto");
  }
});
$("#usuario-opciones").addEventListener("click", (e) => { if (e.target.closest("a")) $("#usuario-opciones").hidden = true; });
$("#btn-salir").addEventListener("click", () => {
  barraDe = null;
  mapa.reiniciar();
  sesion.cerrar("Cerraste sesión.");
});

// visor de fotos (lo usan el mapa y el expediente del abogado)
$("#visor-cerrar").addEventListener("click", () => $("#visor").close());
$("#visor").addEventListener("click", (e) => { if (e.target === $("#visor")) $("#visor").close(); });

window.addEventListener("hashchange", enrutar);
enrutar();
