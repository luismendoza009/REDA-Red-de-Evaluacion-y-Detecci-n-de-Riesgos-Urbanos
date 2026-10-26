// =============================================================================
// Mi cuenta: foto de perfil, datos, perfil profesional del abogado y cambio de contraseña.
// Regla: un abogado arma su perfil público (foto, especialidad, presentación) solo cuando REDA
// verificó su tarjeta profesional. La interfaz lo muestra bloqueado; el backend lo vuelve a validar.
// =============================================================================
import { api, sesion } from "../api.js";
import { $, escapar, fecha, plural, avisar, mostrarErrores, conBotonOcupado, cargando } from "../ui.js";
import { avatar, insigniaVerificado, cargarAvatares, ponerFotoUsuario } from "../avatares.js";

const ROLES = { CIUDADANO: "Ciudadano", OPERADOR: "Operador", ABOGADO: "Abogado", ADMINISTRADOR: "Administrador", ANALISTA: "Analista" };
const TIPOS_FOTO = ["image/jpeg", "image/png", "image/webp"];
const MAX_BYTES_FOTO = 5 * 1024 * 1024;

export async function mostrar(contenedor) {
  contenedor.innerHTML = cargando();
  let p;
  try {
    p = await api.perfil();
  } catch (e) {
    contenedor.innerHTML = `<div class="contenido"><p class="error">${escapar(e.message)}</p></div>`;
    return;
  }
  const ab = p.abogado;
  const perfilBloqueado = ab && !ab.verificado;
  contenedor.innerHTML = `
    <div class="contenido contenido-estrecho vista">
      <header class="encabezado">
        <p class="etiqueta-superior">${ROLES[p.rol] ?? p.rol}</p>
        <h1>Mi cuenta</h1>
        <p class="texto-2">Miembro desde el ${fecha(p.fechaRegistro)}</p>
      </header>

      <section class="tarjeta-formulario">
        <h2>Foto de perfil</h2>
        <div class="foto-perfil ${perfilBloqueado ? "bloqueado" : ""}">
          <span id="mi-foto">${avatar(p.nombre, p.foto, "avatar-xl")}</span>
          <div>
            <p class="nombre-perfil"><b>${escapar(p.nombre)}</b> ${insigniaVerificado(ab?.verificado)}</p>
            <p class="texto-2 pequeno">${ab
              ? "Los ciudadanos ven tu foto al comparar a los abogados que se postulan a su caso."
              : "Solo la ves tú y el personal de REDA. Los abogados y otros ciudadanos no la ven."}
              JPG, PNG o WEBP de hasta 5 MB.</p>
            <div class="fila-acciones">
              <label class="boton boton-claro">${p.foto ? "Cambiar foto" : "Subir foto"}
                <input type="file" id="archivo-foto" accept="${TIPOS_FOTO.join(",")}" hidden ${perfilBloqueado ? "disabled" : ""}></label>
              ${p.foto ? '<button type="button" class="boton" id="quitar-foto">Quitar</button>' : ""}
            </div>
          </div>
        </div>
        ${perfilBloqueado ? `<p class="nota nota-espera">⏳ Tu perfil de abogado se habilita cuando un administrador verifique
          tu tarjeta profesional. Hasta entonces no puedes subir foto ni editar tu perfil, y los ciudadanos no te ven.</p>` : ""}
      </section>

      <form id="f-perfil" class="formulario tarjeta-formulario" novalidate>
        <h2>Datos</h2>
        <label>Nombre completo<input name="nombre" value="${escapar(p.nombre)}" maxlength="120" required></label>
        <label>Correo<input value="${escapar(p.correo)}" disabled><small>El correo identifica tu cuenta y no se puede cambiar.</small></label>
        <p class="error" role="alert"></p>
        <div class="fila-acciones"><button class="boton boton-claro" type="submit">Guardar</button></div>
      </form>

      ${ab ? formularioAbogado(ab) : ""}

      <form id="f-password" class="formulario tarjeta-formulario" novalidate>
        <h2>Cambiar contraseña</h2>
        <label>Contraseña actual<input type="password" name="actual" autocomplete="current-password" required></label>
        <label>Nueva contraseña<input type="password" name="nueva" autocomplete="new-password" required>
          <small>Mínimo 8 caracteres, con al menos una letra y un número.</small></label>
        <p class="error" role="alert"></p>
        <div class="fila-acciones"><button class="boton boton-claro" type="submit">Cambiar contraseña</button></div>
      </form>

      <section class="tarjeta-formulario">
        <h2>Tus datos personales</h2>
        <p class="texto-2">Puedes conocer, actualizar y rectificar tus datos, y revocar la autorización de tratamiento
          (Ley 1581 de 2012). Consulta la <a href="#/privacidad">política de privacidad</a>.</p>
      </section>
    </div>`;

  cargarAvatares(contenedor);
  conectarFoto(contenedor, p);
  conectarDatos();
  if (ab) conectarAbogado(contenedor);
  conectarPassword();
}

function formularioAbogado(ab) {
  const bloqueado = !ab.verificado;
  return `
    <form id="f-abogado" class="formulario tarjeta-formulario" novalidate>
      <h2>Perfil profesional ${insigniaVerificado(ab.verificado)}</h2>
      <p class="texto-2">T.P. ${escapar(ab.tarjetaProfesional)} · ${ab.verificado
        ? "verificada por REDA" : "pendiente de verificación"}</p>
      <p class="reputacion">${ab.numCalificaciones
        ? `Tu calificación de clientes: <b>${ab.calificacionPromedio.toFixed(1)} de 5</b>
           (${plural(ab.numCalificaciones, "calificación", "calificaciones")})`
        : "Aún no tienes calificaciones de clientes. Los ciudadanos te califican cuando cierras sus casos."}</p>
      <fieldset ${bloqueado ? "disabled" : ""}>
        <div class="fila-2">
          <label>Especialidad<input name="especialidad" value="${escapar(ab.especialidad)}" maxlength="80" required></label>
          <label>Ciudad<input name="ciudad" value="${escapar(ab.ciudad)}" maxlength="80" required></label>
        </div>
        <label>Años de experiencia<input name="aniosExperiencia" type="number" min="0" max="70" value="${ab.aniosExperiencia}" required></label>
        <label>Presentación<textarea name="presentacion" maxlength="600" rows="4"
          placeholder="Cuéntale al ciudadano cómo trabajas y qué casos has llevado.">${escapar(ab.presentacion ?? "")}</textarea>
          <small>El ciudadano la lee al comparar postulaciones.</small></label>
      </fieldset>
      <p class="error" role="alert"></p>
      ${bloqueado ? "" : '<div class="fila-acciones"><button class="boton boton-claro" type="submit">Guardar perfil</button></div>'}
    </form>`;
}

function conectarFoto(contenedor, p) {
  const entrada = $("#archivo-foto");
  entrada?.addEventListener("change", async () => {
    const archivo = entrada.files[0];
    entrada.value = "";
    if (!archivo) return;
    if (!TIPOS_FOTO.includes(archivo.type)) { avisar("La foto debe ser JPG, PNG o WEBP.", "error"); return; }
    if (archivo.size > MAX_BYTES_FOTO) { avisar("La foto no puede pesar más de 5 MB.", "error"); return; }
    try {
      await api.cambiarFoto(archivo);
      avisar("Foto actualizada.", "exito");
      mostrar(contenedor);
      ponerFotoUsuario(p.nombre, (await api.perfil()).foto);
    } catch (err) { avisar(err.message, "error"); }
  });
  $("#quitar-foto")?.addEventListener("click", async () => {
    try {
      await api.quitarFoto();
      avisar("Foto quitada.");
      ponerFotoUsuario(p.nombre, null);
      mostrar(contenedor);
    } catch (err) { avisar(err.message, "error"); }
  });
}

function conectarDatos() {
  const fPerfil = $("#f-perfil");
  fPerfil.addEventListener("submit", async (e) => {
    e.preventDefault();
    await conBotonOcupado(fPerfil.querySelector("button"), async () => {
      try {
        const nuevo = await api.actualizarPerfil(fPerfil.nombre.value);
        sesion.guardar({ ...sesion.actual(), nombre: nuevo.nombre });
        $("#usuario-nombre").textContent = nuevo.nombre;
        avisar("Datos guardados.", "exito");
      } catch (err) { mostrarErrores(fPerfil, err); }
    });
  });
}

function conectarAbogado(contenedor) {
  const f = $("#f-abogado");
  f.addEventListener("submit", async (e) => {
    e.preventDefault();
    await conBotonOcupado(f.querySelector("button[type=submit]"), async () => {
      try {
        await api.actualizarPerfilAbogado({
          especialidad: f.especialidad.value, ciudad: f.ciudad.value,
          aniosExperiencia: Number(f.aniosExperiencia.value), presentacion: f.presentacion.value || null,
        });
        avisar("Perfil profesional guardado.", "exito");
        mostrar(contenedor);
      } catch (err) { mostrarErrores(f, err); }
    });
  });
}

function conectarPassword() {
  const fPass = $("#f-password");
  fPass.addEventListener("submit", async (e) => {
    e.preventDefault();
    await conBotonOcupado(fPass.querySelector("button"), async () => {
      try {
        const r = await api.cambiarPassword(fPass.actual.value, fPass.nueva.value);
        fPass.reset();
        mostrarErrores(fPass, { campos: {}, message: "" });
        avisar(r.mensaje, "exito");
      } catch (err) { mostrarErrores(fPass, err); }
    });
  });
}
