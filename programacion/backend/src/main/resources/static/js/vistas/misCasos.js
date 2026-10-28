// =============================================================================
// Mis casos (ciudadano): los abogados que se postularon, compararlos y ELEGIR con quién seguir.
// =============================================================================
import { api } from "../api.js";
import { $$, escapar, fecha, fechaHora, plural, avisar, dialogo, cargando, vacio, chipSituacion, situacionDeCaso, DANOS,
         mostrarErrores } from "../ui.js";
import * as mapa from "./mapa.js";
import { avatar, insigniaVerificado, cargarAvatares } from "../avatares.js";

export async function mostrar(contenedor) {
  contenedor.innerHTML = cargando();
  let casos;
  try {
    casos = await api.misCasos();
  } catch (e) {
    contenedor.innerHTML = `<div class="contenido"><p class="error">${escapar(e.message)}</p></div>`;
    return;
  }
  contenedor.innerHTML = `
    <div class="contenido vista">
      <header class="encabezado">
        <p class="etiqueta-superior">Ciudadano</p>
        <h1>Mis casos</h1>
        <p class="texto-2">Aquí ves a los abogados que se interesan en tus casos y eliges con quién seguir.</p>
      </header>
      ${casos.length ? casos.map(caso).join("")
        : vacio("Aún no tienes casos. Cuando un operador valide uno de tus incidentes, podrás buscar abogado desde Mis reportes.",
          '<a class="boton" href="#/mis-reportes">Ir a Mis reportes</a>')}
    </div>`;
  cargarAvatares(contenedor);
  $$("[data-elegir]", contenedor).forEach((b) => b.addEventListener("click", () =>
    elegir(Number(b.dataset.caso), Number(b.dataset.elegir), b.dataset.nombre, contenedor)));
  $$("[data-calificar]", contenedor).forEach((form) => form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const datos = new FormData(form);
    if (!datos.get("puntaje")) { mostrarErrores(form, { message: "Elige de 1 a 5 estrellas.", campos: {} }); return; }
    try {
      await api.calificarAbogado(Number(form.dataset.calificar), Number(datos.get("puntaje")), datos.get("comentario").trim() || null);
      avisar("Gracias por calificar a tu abogado.", "exito");
      mostrar(contenedor);
    } catch (err) {
      mostrarErrores(form, err);
    }
  }));
}

function caso(c) {
  const situacion = situacionDeCaso(c.estado);
  const pendientes = c.postulaciones.filter((p) => p.estado === "PENDIENTE");
  return `
    <section class="caso">
      <header class="caso-cabeza">
        <div>
          <p class="etiqueta-superior">Caso #${c.id} · Reporte #${c.idReporte}</p>
          <h2>${escapar(c.categoria)} · ${escapar(c.zona)}</h2>
          <p class="texto-2">${escapar(DANOS[c.tipoDano] ?? c.tipoDano)} · abierto el ${fecha(c.fechaCreacion)}</p>
        </div>
        <div class="caso-estado">${chipSituacion(situacion)}<span class="estado">${escapar(c.estado)}</span></div>
      </header>
      ${c.abogadoElegido ? `
        <div class="abogado-elegido">
          <p class="etiqueta-superior">Tu abogado</p>
          ${perfil(c.abogadoElegido)}
          ${c.abogadoElegido.correoContacto ? `<p>Contacto: <b>${escapar(c.abogadoElegido.correoContacto)}</b></p>` : ""}
          <p class="texto-2 pequeno">Este abogado ya puede ver tu expediente completo (fotos, testigos y la descripción del daño).</p>
        </div>` : ""}
      ${formularioCalificacion(c)}
      ${c.estado === "Remitido" ? (pendientes.length ? `
        <h3>${plural(pendientes.length, "abogado interesado", "abogados interesados")}</h3>
        <p class="texto-2">Compara y elige. Al elegir, las demás postulaciones se cierran.</p>
        <div class="postulaciones">${pendientes.map((p) => postulacion(c, p)).join("")}</div>`
        : `<p class="nota">Tu caso está publicado. Aún no se ha postulado ningún abogado; te los mostraremos aquí.</p>`) : ""}
      <div class="fila-acciones"><a class="boton" href="#/mapa/reporte/${c.idReporte}">Ver el reporte y sus fotos</a></div>
    </section>`;
}

function perfil(a) {
  return `
    <div class="perfil-abogado">
      ${avatar(a.nombre, a.foto, "avatar-grande")}
      <div>
        <p class="nombre-perfil"><b>${escapar(a.nombre)}</b> ${insigniaVerificado(a.verificado)}</p>
        <p class="texto-2">${escapar(a.especialidad)} · ${escapar(a.ciudad)}</p>
        ${reputacion(a)}
        <p class="datos-linea"><span>${plural(a.aniosExperiencia, "año", "años")} de experiencia</span>
          <span>${plural(a.casosTomadosEnReda, "caso", "casos")} en REDA</span></p>
        ${a.presentacion ? `<p class="presentacion">${escapar(a.presentacion)}</p>` : ""}
      </div>
    </div>
    ${a.comentarios?.length ? `<details class="opiniones"><summary>Lo que dicen sus clientes (${a.comentarios.length})</summary>
      <ul>${a.comentarios.map((c) => `<li>${estrellas(c.puntaje)}
        ${c.comentario ? `<p>${escapar(c.comentario)}</p>` : '<p class="texto-2">Sin comentario.</p>'}
        <small>${escapar(c.contexto)}</small></li>`).join("")}</ul></details>` : ""}`;
}

/** Estrellas con texto: la calificación nunca se comunica solo con el dibujo. */
export function estrellas(valor) {
  // 4,5 se dibuja con 4 estrellas (no 5) para no exagerar; el número exacto va al lado
  const llenas = Math.max(0, Math.ceil(valor - 0.5));
  return `<span class="estrellas" role="img" aria-label="${valor} de 5 estrellas">${"★".repeat(llenas)}<span class="vacias">${"★".repeat(5 - llenas)}</span></span>`;
}

function reputacion(a) {
  if (!a.numCalificaciones) return `<p class="reputacion texto-2">Aún sin calificaciones de clientes</p>`;
  return `<p class="reputacion">${estrellas(a.calificacionPromedio)} <b>${a.calificacionPromedio.toFixed(1)}</b>
    <span class="texto-2">· ${plural(a.numCalificaciones, "calificación", "calificaciones")}</span></p>`;
}

function formularioCalificacion(c) {
  if (c.miCalificacion) {
    return `<div class="nota">Calificaste a tu abogado con ${estrellas(c.miCalificacion)}${c.miComentario ? `: «${escapar(c.miComentario)}»` : "."}
      Gracias: tu opinión ayuda a otros ciudadanos a elegir.</div>`;
  }
  if (!c.puedeCalificar) return "";
  return `
    <form class="calificar formulario" data-calificar="${c.id}" novalidate>
      <h3>¿Cómo te fue con tu abogado?</h3>
      <div class="selector-estrellas" role="radiogroup" aria-label="Calificación de 1 a 5 estrellas">
        ${[1, 2, 3, 4, 5].map((n) => `<label><input type="radio" name="puntaje" value="${n}" required><span aria-hidden="true">★</span>
          <span class="visualmente-oculto">${n} de 5</span></label>`).join("")}
      </div>
      <label>Cuéntale a otros ciudadanos por qué (opcional)<textarea name="comentario" rows="2" maxlength="1000"
        placeholder="Ej.: Me explicó cada paso y estuvo pendiente del caso."></textarea>
        <small>Se publica sin tu nombre. No incluyas datos personales.</small></label>
      <p class="error" role="alert"></p>
      <div class="fila-acciones"><button class="boton boton-claro" type="submit">Enviar calificación</button></div>
    </form>`;
}

function postulacion(c, p) {
  return `
    <article class="postulacion">
      ${perfil(p.abogado)}
      <blockquote>${escapar(p.mensaje)}</blockquote>
      <div class="fila-acciones">
        <span class="texto-2 pequeno">Se postuló el ${fechaHora(p.fecha)}</span>
        <button class="boton boton-claro" data-caso="${c.id}" data-elegir="${p.id}" data-nombre="${escapar(p.abogado.nombre)}">
          Elegir a ${escapar(p.abogado.nombre.split(" ")[0])}</button>
      </div>
    </article>`;
}

async function elegir(idCaso, idPostulacion, nombre, contenedor) {
  const ok = await dialogo({
    titulo: `¿Elegir a ${nombre}?`,
    aceptar: "Sí, elegir",
    mensaje: `<p>Al elegirlo, <b>autorizas</b> que vea tu nombre, tu correo, tus fotos, tus testigos y la descripción del
      daño, para que pueda estudiar tu caso. Las demás postulaciones se cerrarán.</p>
      <p class="texto-2">REDA no cobra nada al ciudadano. Lo que acuerdes con el abogado es entre ustedes.</p>`,
  });
  if (!ok) return;
  try {
    await api.elegirAbogado(idCaso, idPostulacion);
    mapa.invalidar();
    avisar(`Listo: ${nombre} llevará tu caso.`, "exito");
  } catch (e) {
    avisar(e.message, "error");
  }
  mostrar(contenedor);
}
