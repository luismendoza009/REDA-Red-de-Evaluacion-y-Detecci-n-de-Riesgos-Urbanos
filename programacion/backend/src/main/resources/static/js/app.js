// =============================================================================
// REDA · arranque y enrutador
// Rutas con "#/" (no requieren configurar el servidor). Sin sesión solo se ven las pantallas
// de acceso y las páginas informativas; todo lo demás exige iniciar sesión, y cada sección
// exige además el rol correspondiente (el backend lo vuelve a verificar: la interfaz no es
// la que protege los datos, solo evita mostrar opciones que no corresponden).
// =============================================================================
import { api, sesion } from "./api.js";
import { ponerFotoUsuario } from "./avatares.js";
import { icono } from "./iconos.js";
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
import * as inicio from "./vistas/inicio.js";

const ROLES = { CIUDADANO: "Ciudadano", OPERADOR: "Operador", ABOGADO: "Abogado", ADMINISTRADOR: "Administrador", ANALISTA: "Analista" };

/** acceso: solo sin sesión · libre: con o sin sesión · roles: con sesión y uno de esos roles. */
const RUTAS = [
  { patron: /^\/entrar$/, acceso: true, vista: acceso.entrar },
  { patron: /^\/registro$/, acceso: true, vista: acceso.registro },
  { patron: /^\/registro-abogado$/, acceso: true, vista: acceso.registroAbogado },
  { patron: /^\/recuperar$/, acceso: true, vista: acceso.recuperar },
  { patron: /^\/restablecer\/([A-Za-z0-9_-]+)$/, acceso: true, vista: acceso.restablecer },
  { patron: /^\/(terminos|privacidad|ayuda)$/, libre: true, vista: info.mostrar },
  { patron: /^\/inicio$/, vista: inicio.mostrar },
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

/**
 * Menú lateral por rol: grupos de opciones [ruta, texto, icono]. Una ruta que empieza por "/" (no "#/")
 * se abre en otra pestaña (la documentación Swagger de la API).
 */
const INICIO = ["#/inicio", "Inicio", "inicio"];
const MAPA = ["#/mapa", "Mapa de reportes", "mapa"];
const MENU = {
  CIUDADANO: [
    ["", [INICIO]],
    ["Mis reportes", [["#/reportar", "Nuevo reporte", "reportar"], ["#/mis-reportes", "Mis reportes", "reportes"]]],
    ["Asesoría legal", [["#/mis-casos", "Mis casos y abogados", "casos"]]],
    ["Ciudad", [MAPA]],
  ],
  ABOGADO: [
    ["", [INICIO]],
    ["Casos", [["#/abogado/disponibles", "Casos disponibles", "buscar"], ["#/abogado/mis-casos", "Mis casos", "casos"]]],
    ["Ciudad", [MAPA]],
  ],
  OPERADOR: [
    ["", [INICIO]],
    ["Revisión", [["#/revision", "Bandeja de revisión", "revision"]]],
    ["Ciudad", [MAPA]],
  ],
  ADMINISTRADOR: [
    ["", [INICIO]],
    ["Gestión", [["#/revision", "Bandeja de revisión", "revision"], ["#/admin/abogados", "Verificar abogados", "verificar"]]],
    ["Ciudad", [MAPA]],
    ["Técnico", [["/swagger-ui.html", "Documentación de la API", "api"]]],
  ],
  ANALISTA: [["", [INICIO]], ["Ciudad", [MAPA]]],
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
    location.replace(s ? "#/inicio" : "#/entrar");
    return;
  }
  const { r, m } = coincidencia;
  if (r.acceso && s) { location.replace("#/inicio"); return; }
  if (!r.acceso && !r.libre && !s) { location.replace("#/entrar"); return; }
  if (r.roles && !r.roles.includes(s.rol)) {
    avisar("Esa sección no está disponible para tu rol.", "error");
    location.replace("#/inicio");
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
  armarMenu(s);
  cerrarMenuMovil();
  document.body.classList.toggle("en-mapa", !!r.esMapa);
  marcarActivo(ruta);
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

let menuDe = null;
function armarMenu(s) {
  document.body.dataset.rol = s.rol;
  if (menuDe === s.token) return;
  menuDe = s.token;
  $("#menu").innerHTML = (MENU[s.rol] ?? MENU.ANALISTA).map(([grupo, opciones]) => `
    <div class="menu-grupo">
      ${grupo ? `<p class="menu-titulo">${escapar(grupo)}</p>` : ""}
      ${opciones.map(([href, texto, ic]) => href.startsWith("#")
        ? `<a href="${href}">${icono(ic)}<span>${escapar(texto)}</span></a>`
        : `<a href="${href}" target="_blank" rel="noopener">${icono(ic)}<span>${escapar(texto)}</span><small>↗</small></a>`).join("")}
    </div>`).join("");
  $$(".lateral-pie [data-icono]").forEach((el) => {
    if (!el.querySelector("svg")) el.insertAdjacentHTML("afterbegin", icono(el.dataset.icono));
  });
  $("#usuario-nombre").textContent = s.nombre;
  $("#usuario-rol").textContent = ROLES[s.rol] ?? s.rol;
  ponerFotoUsuario(s.nombre, null);
  // la foto no viene en el token: se pide el perfil una vez por sesión
  api.perfil().then((p) => {
    if (menuDe !== s.token) return;
    ponerFotoUsuario(p.nombre, p.foto);
    if (p.abogado?.verificado) $("#usuario-rol").textContent = "Abogado · ✓ verificado";
  }).catch(() => { /* sin perfil: se quedan las iniciales */ });
}

/** Resalta la opción del menú de la sección actual y la usa como título en el celular. */
function marcarActivo(ruta) {
  let activo = null;
  $$("#menu a, .lateral-pie a").forEach((a) => {
    const href = a.getAttribute("href");
    const es = href.startsWith("#") && ruta.startsWith(href.slice(1));
    a.classList.toggle("activo", es);
    if (es) { a.setAttribute("aria-current", "page"); activo ??= a; } else a.removeAttribute("aria-current");
  });
  $("#titulo-movil").textContent = activo?.textContent.trim() || "REDA";
}

// menú lateral en el celular: se abre como cajón con un velo detrás
function abrirMenuMovil(abrir) {
  $("#btn-menu").setAttribute("aria-expanded", abrir);
  $("#lateral").classList.toggle("abierto", abrir);
  $("#velo").hidden = !abrir;
}
const cerrarMenuMovil = () => abrirMenuMovil(false);
$("#btn-menu").addEventListener("click", () => abrirMenuMovil(!$("#lateral").classList.contains("abierto")));
$("#velo").addEventListener("click", cerrarMenuMovil);
document.addEventListener("keydown", (e) => { if (e.key === "Escape" && $("#lateral").classList.contains("abierto")) cerrarMenuMovil(); });
$("#btn-salir").addEventListener("click", () => {
  menuDe = null;
  mapa.reiniciar();
  sesion.cerrar("Cerraste sesión.");
});

let instalacionPendiente = null;
const botonesInstalar = $$(".btn-instalar");
const esIOS = /iPad|iPhone|iPod/.test(navigator.userAgent)
  || (navigator.platform === "MacIntel" && navigator.maxTouchPoints > 1);
const estaInstalada = () => window.matchMedia("(display-mode: standalone)").matches || navigator.standalone === true;

botonesInstalar.forEach((boton) => {
  boton.insertAdjacentHTML("afterbegin", icono("instalar"));
  boton.addEventListener("click", async () => {
    if (!instalacionPendiente) {
      avisar("En Safari, toca Compartir y elige «Añadir a pantalla de inicio».", "info");
      return;
    }
    try {
      await instalacionPendiente.prompt();
      const { outcome } = await instalacionPendiente.userChoice;
      if (outcome === "accepted") botonesInstalar.forEach((elemento) => { elemento.hidden = true; });
      instalacionPendiente = null;
    } catch (error) {
      avisar("No fue posible iniciar la instalación. Inténtalo desde el menú del navegador.", "error");
      console.error("No se pudo mostrar el diálogo de instalación de REDA.", error);
    }
  });
});

function actualizarBotonesInstalar() {
  const mostrar = !estaInstalada() && (Boolean(instalacionPendiente) || esIOS);
  botonesInstalar.forEach((boton) => { boton.hidden = !mostrar; });
}

window.addEventListener("beforeinstallprompt", (evento) => {
  evento.preventDefault();
  instalacionPendiente = evento;
  actualizarBotonesInstalar();
});
window.addEventListener("appinstalled", () => {
  instalacionPendiente = null;
  actualizarBotonesInstalar();
  avisar("REDA quedó instalada en este dispositivo.", "exito");
});
actualizarBotonesInstalar();

// visor de fotos (lo usan el mapa y el expediente del abogado)
$("#visor-cerrar").addEventListener("click", () => $("#visor").close());
$("#visor").addEventListener("click", (e) => { if (e.target === $("#visor")) $("#visor").close(); });

window.addEventListener("hashchange", enrutar);
enrutar();
