// =============================================================================
// Mi cuenta: datos del perfil y cambio de contraseña.
// =============================================================================
import { api, sesion } from "../api.js";
import { $, escapar, fecha, plural, avisar, mostrarErrores, conBotonOcupado, cargando } from "../ui.js";

const ROLES = { CIUDADANO: "Ciudadano", OPERADOR: "Operador", ABOGADO: "Abogado", ADMINISTRADOR: "Administrador", ANALISTA: "Analista" };

export async function mostrar(contenedor) {
  contenedor.innerHTML = cargando();
  let p;
  try {
    p = await api.perfil();
  } catch (e) {
    contenedor.innerHTML = `<div class="contenido"><p class="error">${escapar(e.message)}</p></div>`;
    return;
  }
  contenedor.innerHTML = `
    <div class="contenido contenido-estrecho vista">
      <header class="encabezado">
        <p class="etiqueta-superior">${ROLES[p.rol] ?? p.rol}</p>
        <h1>Mi cuenta</h1>
        <p class="texto-2">Miembro desde el ${fecha(p.fechaRegistro)}</p>
      </header>

      <form id="f-perfil" class="formulario tarjeta-formulario" novalidate>
        <h2>Datos</h2>
        <label>Nombre completo<input name="nombre" value="${escapar(p.nombre)}" maxlength="120" required></label>
        <label>Correo<input value="${escapar(p.correo)}" disabled><small>El correo identifica tu cuenta y no se puede cambiar.</small></label>
        ${p.abogado ? `
          <div class="nota ${p.abogado.verificado ? "" : "nota-espera"}">
            ${p.abogado.verificado ? "✓ Tarjeta profesional verificada" : "⏳ Tarjeta profesional pendiente de verificación"}
            · T.P. ${escapar(p.abogado.tarjetaProfesional)} · ${escapar(p.abogado.especialidad)} · ${escapar(p.abogado.ciudad)}
            · ${plural(p.abogado.aniosExperiencia, "año", "años")} de experiencia
          </div>
          <p class="reputacion">${p.abogado.numCalificaciones
            ? `Tu calificación de clientes: <b>${p.abogado.calificacionPromedio.toFixed(1)} de 5</b>
               (${plural(p.abogado.numCalificaciones, "calificación", "calificaciones")})`
            : "Aún no tienes calificaciones de clientes. Los ciudadanos te califican cuando cierras sus casos."}</p>` : ""}
        <p class="error" role="alert"></p>
        <div class="fila-acciones"><button class="boton boton-claro" type="submit">Guardar</button></div>
      </form>

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

  const fPerfil = $("#f-perfil");
  fPerfil.addEventListener("submit", async (e) => {
    e.preventDefault();
    await conBotonOcupado(fPerfil.querySelector("button"), async () => {
      try {
        const nuevo = await api.actualizarPerfil(fPerfil.nombre.value);
        sesion.guardar({ ...sesion.actual(), nombre: nuevo.nombre });
        $("#usuario-nombre").textContent = nuevo.nombre.split(" ")[0];
        avisar("Datos guardados.", "exito");
      } catch (err) { mostrarErrores(fPerfil, err); }
    });
  });
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
