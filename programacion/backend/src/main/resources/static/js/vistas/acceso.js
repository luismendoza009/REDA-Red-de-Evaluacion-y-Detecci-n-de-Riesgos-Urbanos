// =============================================================================
// Pantallas sin sesión: entrar, crear cuenta (ciudadano o abogado), recuperar y restablecer contraseña.
// =============================================================================
import { api, sesion } from "../api.js";
import { $, escapar, mostrarErrores, conBotonOcupado } from "../ui.js";

const AYUDA_PASSWORD = "Mínimo 8 caracteres, con al menos una letra y un número.";

function pintar(contenedor, html, enfocar = true) {
  contenedor.closest(".acceso")?.classList.remove("acceso--login");
  contenedor.innerHTML = `<div class="vista">${html}</div>`;
  if (enfocar) contenedor.querySelector("input")?.focus();
}

function enviarCon(form, accion) {
  let intervaloCuenta = null;
  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const boton = form.querySelector("button[type=submit]");
    await conBotonOcupado(boton, async () => {
      try {
        await accion(Object.fromEntries(new FormData(form)));
      } catch (error) {
        clearInterval(intervaloCuenta);
        mostrarErrores(form, error);
        const mensaje = form.querySelector(".error");
        if (Number.isInteger(error.intentosRestantes)) {
          mensaje.textContent = error.intentosRestantes > 0
            ? `${error.message}. Te quedan ${error.intentosRestantes} ${error.intentosRestantes === 1 ? "intento" : "intentos"} antes del siguiente bloqueo.`
            : error.message;
        }
        if (Number.isInteger(error.segundosRestantes) && error.segundosRestantes > 0) {
          let restantes = error.segundosRestantes;
          const detalleIntentos = Number.isInteger(error.intentosRestantes)
            ? ` Te quedan ${error.intentosRestantes} intentos antes del siguiente bloqueo.`
            : "";
          const actualizar = () => {
            if (!form.isConnected) {
              clearInterval(intervaloCuenta);
              return;
            }
            const minutos = String(Math.floor(restantes / 60)).padStart(2, "0");
            const segundos = String(restantes % 60).padStart(2, "0");
            mensaje.textContent = restantes > 0
              ? `Demasiados intentos. Intenta de nuevo en ${minutos}:${segundos}.${detalleIntentos}`
              : `Ya puedes intentar iniciar sesión de nuevo.${detalleIntentos}`;
            restantes--;
            if (restantes < 0) clearInterval(intervaloCuenta);
          };
          actualizar();
          intervaloCuenta = setInterval(actualizar, 1000);
        }
      }
    });
  });
}

/** Al entrar o registrarse se guarda la sesión y el enrutador lleva al mapa. */
function entrarCon(respuesta) {
  sesion.guardar(respuesta);
  location.hash = "#/inicio";
}

export function entrar(contenedor, _params, { mensaje } = {}) {
  pintar(contenedor, `
    <h2>Iniciar sesión</h2>
    <p class="texto-2">Entra para ver el mapa, tus reportes y tus casos.</p>
    ${mensaje ? `<p class="nota">${escapar(mensaje)}</p>` : ""}
    <form id="f-entrar" class="formulario" novalidate>
      <label>Correo<input type="email" name="correo" autocomplete="username" required></label>
      <div class="campo-con-etiqueta">
        <label for="password-login">Contraseña</label>
        <span class="campo-password">
        <input id="password-login" type="password" name="password" autocomplete="current-password" required>
        <button class="alternar-password" type="button" aria-label="Mostrar contraseña" aria-pressed="false" data-alternar-password>
          <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7-10-7-10-7Z"/><circle cx="12" cy="12" r="3"/></svg>
          <svg viewBox="0 0 24 24" aria-hidden="true" hidden><path d="m3 3 18 18"/><path d="M10.6 10.6a2 2 0 0 0 2.8 2.8"/><path d="M9.9 5.2A10.7 10.7 0 0 1 12 5c6.4 0 10 7 10 7a14.8 14.8 0 0 1-3.1 3.8M6.2 6.2C3.5 8 2 12 2 12s3.6 7 10 7a10 10 0 0 0 3.2-.5"/></svg>
        </button>
        </span>
      </div>
      <p class="error" role="alert"></p>
      <button class="boton boton-claro boton-ancho" type="submit">Entrar</button>
    </form>
    <p class="enlaces"><a href="#/recuperar">¿Olvidaste tu contraseña?</a></p>
    <hr>
    <p class="enlaces">¿No tienes cuenta? <a href="#/registro">Crear cuenta</a> · <a href="#/registro-abogado">Soy abogado</a></p>`,
  !window.matchMedia("(max-width: 760px)").matches);
  contenedor.closest(".acceso")?.classList.add("acceso--login");
  const form = $("#f-entrar");
  const password = form.querySelector('input[name="password"]');
  const alternar = form.querySelector("[data-alternar-password]");
  alternar.addEventListener("click", () => {
    const mostrar = password.type === "password";
    password.type = mostrar ? "text" : "password";
    alternar.setAttribute("aria-label", mostrar ? "Ocultar contraseña" : "Mostrar contraseña");
    alternar.setAttribute("aria-pressed", String(mostrar));
    alternar.querySelectorAll("svg").forEach((icono, indice) => {
      icono.hidden = indice === (mostrar ? 0 : 1);
    });
  });
  enviarCon(form, async (d) => entrarCon(await api.iniciarSesion(d.correo, d.password)));
}

export function registro(contenedor) {
  pintar(contenedor, `
    <h2>Crear cuenta</h2>
    <p class="texto-2">Para reportar fallas y documentar lo que te pasó. Es gratis.</p>
    <form id="f-registro" class="formulario" novalidate>
      <label>Nombre completo<input name="nombre" autocomplete="name" maxlength="120" required></label>
      <label>Correo<input type="email" name="correo" autocomplete="email" maxlength="150" required></label>
      <label>Contraseña<input type="password" name="password" autocomplete="new-password" required>
        <small>${AYUDA_PASSWORD}</small></label>
      <label class="casilla"><input type="checkbox" name="aceptaTratamientoDatos" required>
        <span>Acepto los <a href="#/terminos" target="_blank">términos</a> y la
        <a href="#/privacidad" target="_blank">política de tratamiento de datos</a> (Ley 1581 de 2012).</span></label>
      <p class="error" role="alert"></p>
      <button class="boton boton-claro boton-ancho" type="submit">Crear cuenta</button>
    </form>
    <p class="enlaces">¿Ya tienes cuenta? <a href="#/entrar">Iniciar sesión</a></p>`);
  enviarCon($("#f-registro"), async (d) => entrarCon(await api.registrar({
    nombre: d.nombre, correo: d.correo, password: d.password, aceptaTratamientoDatos: d.aceptaTratamientoDatos === "on",
  })));
}

export function registroAbogado(contenedor) {
  pintar(contenedor, `
    <h2>Registro de abogados</h2>
    <p class="texto-2">Recibe casos documentados de personas que sufrieron un daño por fallas en la infraestructura pública.
      Tu cuenta se activa cuando verifiquemos tu tarjeta profesional.</p>
    <form id="f-abogado" class="formulario" novalidate>
      <label>Nombre completo<input name="nombre" autocomplete="name" maxlength="120" required></label>
      <label>Correo profesional<input type="email" name="correo" autocomplete="email" maxlength="150" required></label>
      <label>Contraseña<input type="password" name="password" autocomplete="new-password" required>
        <small>${AYUDA_PASSWORD}</small></label>
      <div class="fila-2">
        <label>Tarjeta profesional<input name="tarjetaProfesional" inputmode="numeric" pattern="[0-9]{4,10}" required></label>
        <label>Años de experiencia<input type="number" name="aniosExperiencia" min="0" max="70" value="0" required></label>
      </div>
      <div class="fila-2">
        <label>Especialidad<input name="especialidad" maxlength="80" value="Responsabilidad del Estado" required></label>
        <label>Ciudad<input name="ciudad" maxlength="80" value="Valledupar" required></label>
      </div>
      <label>Preséntate a los ciudadanos (opcional)<textarea name="presentacion" rows="3" maxlength="600"
        placeholder="Tu experiencia, cómo trabajas, dónde atiendes…"></textarea></label>
      <label class="casilla"><input type="checkbox" name="aceptaTratamientoDatos" required>
        <span>Acepto los <a href="#/terminos" target="_blank">términos</a>, la
        <a href="#/privacidad" target="_blank">política de datos</a> y la cuota fija por cada caso en que un ciudadano me elija.</span></label>
      <p class="error" role="alert"></p>
      <button class="boton boton-claro boton-ancho" type="submit">Enviar solicitud</button>
    </form>
    <p class="enlaces"><a href="#/entrar">Volver a iniciar sesión</a></p>`);
  enviarCon($("#f-abogado"), async (d) => entrarCon(await api.registrarAbogado({
    ...d, aniosExperiencia: Number(d.aniosExperiencia), aceptaTratamientoDatos: d.aceptaTratamientoDatos === "on",
  })));
}

export function recuperar(contenedor) {
  pintar(contenedor, `
    <h2>Recuperar contraseña</h2>
    <p class="texto-2">Te enviaremos un enlace para crear una nueva. Vence en 30 minutos.</p>
    <form id="f-recuperar" class="formulario" novalidate>
      <label>Correo de tu cuenta<input type="email" name="correo" autocomplete="email" required></label>
      <p class="error" role="alert"></p>
      <button class="boton boton-claro boton-ancho" type="submit">Enviar enlace</button>
    </form>
    <p class="enlaces"><a href="#/entrar">Volver a iniciar sesión</a></p>`);
  enviarCon($("#f-recuperar"), async (d) => {
    const r = await api.recuperar(d.correo);
    pintar(contenedor, `
      <h2>Revisa tu correo</h2>
      <p>${escapar(r.mensaje)}</p>
      <p class="nota">Entorno de demostración: no hay servidor de correo, así que el mensaje se escribe en la
        consola donde corre el servidor (busca «CORREO SIMULADO»).</p>
      <p class="enlaces"><a href="#/entrar">Volver a iniciar sesión</a></p>`);
  });
}

export function restablecer(contenedor, [token]) {
  pintar(contenedor, `
    <h2>Nueva contraseña</h2>
    <form id="f-restablecer" class="formulario" novalidate>
      <label>Nueva contraseña<input type="password" name="nuevaPassword" autocomplete="new-password" required>
        <small>${AYUDA_PASSWORD}</small></label>
      <label>Repite la contraseña<input type="password" name="repetir" autocomplete="new-password" required></label>
      <p class="error" role="alert"></p>
      <button class="boton boton-claro boton-ancho" type="submit">Guardar contraseña</button>
    </form>`);
  enviarCon($("#f-restablecer"), async (d) => {
    if (d.nuevaPassword !== d.repetir) {
      throw Object.assign(new Error("Las contraseñas no coinciden"), { campos: { repetir: "no coincide con la contraseña" } });
    }
    const r = await api.restablecer(token, d.nuevaPassword);
    pintar(contenedor, `<h2>Contraseña actualizada</h2><p>${escapar(r.mensaje)}</p>
      <a class="boton boton-claro boton-ancho" href="#/entrar">Iniciar sesión</a>`);
  });
}
