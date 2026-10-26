// =============================================================================
// Inicio: la primera pantalla después de entrar. Cambia según el rol:
//  - resumen en cifras con datos reales de la API (no hay endpoint de dashboard: se reusan los existentes),
//  - avisos de lo que requiere acción del usuario,
//  - accesos directos ("¿Qué quieres hacer?") y cómo funciona el proceso para ese rol.
// =============================================================================
import { api, sesion, ErrorApi } from "../api.js";
import { escapar, fechaHora, plural, cargando } from "../ui.js";
import { icono } from "../iconos.js";

const DESCRIPCION_ROL = {
  CIUDADANO: "Documenta las fallas de la ciudad y, si una te causó un daño, encuentra un abogado para tu caso.",
  ABOGADO: "Revisa casos documentados con evidencia y antecedentes, postúlate y atiende los que te elijan.",
  OPERADOR: "Revisa los reportes que llegan: valida los reales y rechaza los que no corresponden.",
  ADMINISTRADOR: "Supervisa la revisión de reportes y verifica a los abogados antes de que vean casos.",
  ANALISTA: "Consulta la información pública de reportes de la ciudad.",
};

export async function mostrar(contenedor) {
  contenedor.innerHTML = cargando();
  const s = sesion.actual();
  const armar = { CIUDADANO: ciudadano, ABOGADO: abogado, OPERADOR: gestion, ADMINISTRADOR: gestion }[s.rol] ?? analista;
  const cuerpo = await armar(s.rol);
  contenedor.innerHTML = `
    <div class="contenido vista">
      <header class="encabezado bienvenida">
        <p class="etiqueta-superior">${saludo()}</p>
        <h1>${escapar(s.nombre.split(" ")[0])}, ¿qué hacemos hoy?</h1>
        <p class="texto-2">${DESCRIPCION_ROL[s.rol] ?? ""}</p>
      </header>
      ${cuerpo}
      <p class="aviso-legal">REDA documenta fallas en la infraestructura pública. No presta asesoría jurídica ni garantiza el resultado de un caso.</p>
    </div>`;
}

// ------------------------------------------------------------------ ciudadano
async function ciudadano() {
  const [reportes, casos] = await Promise.all([api.misReportes().catch(() => []), api.misCasos().catch(() => [])]);
  const enRevision = reportes.filter((r) => ["Pendiente", "En revisión"].includes(r.estado)).length;
  const listos = reportes.filter((r) => r.puedeBuscarAbogado).length;
  const porElegir = casos.filter((c) => !c.abogadoElegido && c.postulaciones.some((p) => p.estado === "PENDIENTE"));

  const avisos = [];
  if (listos) avisos.push(aviso(`${plural(listos, "incidente validado", "incidentes validados")}: ya puedes buscar abogado.`,
    "#/mis-reportes", "Ir a Mis reportes"));
  porElegir.forEach((c) => avisos.push(aviso(
    `${plural(c.postulaciones.filter((p) => p.estado === "PENDIENTE").length, "abogado se postuló", "abogados se postularon")} a tu caso #${c.id}. Elige con quién seguir.`,
    "#/mis-casos", "Ver abogados")));

  return `
    ${cifras([
      ["Mis reportes", reportes.length],
      ["Incidentes", reportes.filter((r) => r.tipo === "INCIDENTE").length],
      ["En revisión", enRevision],
      ["Mis casos", casos.length],
    ])}
    ${bloqueAvisos(avisos)}
    ${acciones([
      ["#/reportar", "reportar", "Reportar una falla o un incidente", "Hueco, alcantarilla, luminaria… con foto y ubicación. Si te causó un daño, márcalo como incidente.", true],
      ["#/mis-reportes", "reportes", "Ver mis reportes", "Sigue el estado de cada reporte y busca abogado cuando un incidente sea validado."],
      ["#/mis-casos", "casos", "Mis casos y abogados", "Compara a los abogados que se postulan y elige con quién seguir."],
      ["#/mapa", "mapa", "Mapa de reportes", "Mira qué se ha reportado en tu barrio y en toda la ciudad."],
    ])}
    ${pasos("Cómo funciona", [
      ["Reporta", "Foto, ubicación en el mapa y descripción. Si hubo daño, cuéntalo."],
      ["Validación", "Un operador de REDA revisa que el reporte sea real."],
      ["Antecedentes", "REDA busca reportes previos del mismo punto: la entidad «ya sabía»."],
      ["Abogado", "Publicas tu caso de forma anónima y eliges entre los abogados interesados."],
    ])}`;
}

// ------------------------------------------------------------------ abogado
async function abogado() {
  let disponibles = [];
  let verificado = true;
  try {
    disponibles = await api.casosDisponibles();
  } catch (e) {
    if (e instanceof ErrorApi && e.estado === 403) verificado = false;
  }
  const mios = verificado ? await api.misCasosAbogado().catch(() => []) : [];
  const postulado = disponibles.filter((c) => c.miPostulacion === "PENDIENTE").length;
  const enCurso = mios.filter((c) => c.estado === "Aceptado por abogado").length;

  const avisos = verificado ? [] : [aviso("Tu cuenta está en verificación. Cuando un administrador confirme tu tarjeta profesional podrás ver casos y postularte.",
    "#/cuenta", "Completar mi perfil", true)];
  const nuevos = disponibles.filter((c) => !c.miPostulacion).length;
  if (nuevos) avisos.push(aviso(`${plural(nuevos, "caso busca", "casos buscan")} abogado y aún no te has postulado.`,
    "#/abogado/disponibles", "Ver casos"));

  return `
    ${cifras([
      ["Casos buscando abogado", verificado ? disponibles.length : "–"],
      ["Mis postulaciones", verificado ? postulado : "–"],
      ["Casos en curso", verificado ? enCurso : "–"],
      ["Casos cerrados", verificado ? mios.filter((c) => c.estado === "Cerrado").length : "–"],
    ])}
    ${bloqueAvisos(avisos)}
    ${acciones([
      ["#/abogado/disponibles", "buscar", "Casos disponibles", "Resumen anónimo de cada caso: daño, antecedentes, testigos y valor por tomarlo.", true],
      ["#/abogado/mis-casos", "casos", "Mis casos", "Expediente completo de los casos donde el ciudadano te eligió."],
      ["#/cuenta", "cuenta", "Mi perfil profesional", "Especialidad, experiencia y presentación que ven los ciudadanos."],
      ["#/mapa", "mapa", "Mapa de reportes", "Puntos de la ciudad con más reportes e incidentes."],
    ])}
    ${pasos("Cómo funciona", [
      ["Revisa", "Ves casos validados, sin datos personales del ciudadano."],
      ["Postúlate", "Envía un mensaje explicando cómo llevarías el caso."],
      ["Te eligen", "El ciudadano compara postulaciones y elige un abogado."],
      ["Expediente", "Accedes a fotos, testigos y antecedentes. REDA cobra una cuota fija, sin cuota litis."],
    ])}`;
}

// ------------------------------------------------------------------ operador y administrador
async function gestion(rol) {
  const admin = rol === "ADMINISTRADOR";
  const [bandeja, puntos, abogados] = await Promise.all([
    api.bandejaRevision().catch(() => []),
    api.puntos().catch(() => []),
    admin ? api.abogadosAdmin().catch(() => []) : Promise.resolve([]),
  ]);
  const incidentes = bandeja.filter((r) => r.tipo === "INCIDENTE").length;
  const sinVerificar = abogados.filter((a) => !a.verificado).length;
  const total = puntos.reduce((n, p) => n + p.reportes.length, 0);

  const avisos = [];
  if (incidentes) avisos.push(aviso(`${plural(incidentes, "incidente espera", "incidentes esperan")} revisión: van primero porque hubo un daño.`,
    "#/revision", "Revisar ahora"));
  if (sinVerificar) avisos.push(aviso(`${plural(sinVerificar, "abogado espera", "abogados esperan")} verificación de su tarjeta profesional.`,
    "#/admin/abogados", "Verificar"));

  const urgentes = bandeja.slice(0, 5);
  return `
    ${cifras([
      ["Por revisar", bandeja.length],
      ["Incidentes por revisar", incidentes],
      ["Reportes en el mapa", total],
      admin ? ["Abogados por verificar", sinVerificar] : ["Puntos con 3+ reportes", puntos.filter((p) => p.reportes.length >= 3).length],
    ])}
    ${bloqueAvisos(avisos)}
    ${acciones([
      ["#/revision", "revision", "Bandeja de revisión", "Valida, rechaza o actualiza el estado de los reportes. Cada cambio queda en el historial.", true],
      ...(admin ? [["#/admin/abogados", "verificar", "Verificar abogados", "Confirma la tarjeta profesional para activar la cuenta del abogado."]] : []),
      ["#/mapa", "mapa", "Mapa de reportes", "Concentración de reportes e incidentes por barrio."],
      ...(admin ? [["/swagger-ui.html", "api", "Documentación de la API", "Swagger: lista de todos los servicios del backend para probarlos."]] : []),
    ])}
    <section class="seccion-inicio">
      <h2>Lo siguiente en la bandeja</h2>
      ${urgentes.length ? `<ul class="lista-inicio">${urgentes.map((r) => `
        <li>
          <span class="lista-marca ${r.tipo === "INCIDENTE" ? "es-incidente" : ""}">${r.tipo === "INCIDENTE" ? icono("alerta") : icono("reportes")}</span>
          <span><b>${escapar(r.categoria)}</b> · ${escapar(r.zona)}<small>Reporte #${r.id} · ${escapar(r.estado)} · ${fechaHora(r.fechaReporte)}</small></span>
          <a class="boton" href="#/mapa/reporte/${r.id}">Ver</a>
        </li>`).join("")}</ul>` : `<p class="texto-2">No hay reportes pendientes. ¡Bandeja al día!</p>`}
    </section>`;
}

// ------------------------------------------------------------------ analista (solo consulta)
async function analista() {
  const puntos = await api.puntos().catch(() => []);
  const total = puntos.reduce((n, p) => n + p.reportes.length, 0);
  return `
    ${cifras([["Reportes en el mapa", total], ["Puntos con 3+ reportes", puntos.filter((p) => p.reportes.length >= 3).length]])}
    ${acciones([["#/mapa", "mapa", "Mapa de reportes", "Concentración de reportes e incidentes por barrio.", true]])}`;
}

// ------------------------------------------------------------------ piezas
function saludo() {
  const h = new Date().getHours();
  return h < 12 ? "Buenos días" : h < 19 ? "Buenas tardes" : "Buenas noches";
}

const cifras = (lista) => `
  <section class="kpis" aria-label="Resumen">
    ${lista.map(([nombre, valor]) => `<div class="kpi"><span class="kpi-valor">${valor}</span><span class="kpi-nombre">${escapar(nombre)}</span></div>`).join("")}
  </section>`;

const aviso = (texto, href, accion, espera = false) => ({ texto, href, accion, espera });
const bloqueAvisos = (avisos) => !avisos.length ? "" : `
  <section class="seccion-inicio" aria-label="Pendientes">
    <h2>Requiere tu atención</h2>
    <ul class="avisos-inicio">${avisos.map((a) => `
      <li class="${a.espera ? "espera" : ""}">${icono("alerta")}<span>${escapar(a.texto)}</span>
        <a class="boton boton-claro" href="${a.href}">${escapar(a.accion)}</a></li>`).join("")}
    </ul>
  </section>`;

const acciones = (lista) => `
  <section class="seccion-inicio">
    <h2>¿Qué quieres hacer?</h2>
    <div class="accesos">
      ${lista.map(([href, ic, titulo, texto, principal]) => `
        <a class="acceso-directo ${principal ? "principal" : ""}" href="${href}" ${href.startsWith("#") ? "" : 'target="_blank" rel="noopener"'}>
          <span class="acceso-icono">${icono(ic)}</span>
          <b>${escapar(titulo)}</b>
          <span class="texto-2">${escapar(texto)}</span>
          <span class="acceso-ir">${icono("flecha")}</span>
        </a>`).join("")}
    </div>
  </section>`;

const pasos = (titulo, lista) => `
  <section class="seccion-inicio">
    <h2>${escapar(titulo)}</h2>
    <ol class="pasos">${lista.map(([t, d]) => `<li><b>${escapar(t)}</b><span class="texto-2">${escapar(d)}</span></li>`).join("")}</ol>
  </section>`;
