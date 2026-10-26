// =============================================================================
// Páginas informativas: ayuda, términos y privacidad. Se ven con o sin sesión.
// Los textos legales son BORRADORES y deben validarse con un abogado antes de usarse.
// =============================================================================
import { sesion } from "../api.js";

const BORRADOR = `<p class="nota nota-espera">Borrador para el proyecto académico. Debe ser revisado y validado por un
  abogado antes de publicarse. No constituye asesoría jurídica.</p>`;

const PAGINAS = {
  ayuda: () => `
    <h1>Ayuda</h1>
    <h2>¿Qué es REDA?</h2>
    <p>REDA documenta fallas en la infraestructura pública de Valledupar (huecos, alcantarillas sin tapa, luminarias
      dañadas, señales caídas…). Cada reporte queda con fecha, ubicación y fotos con huella digital. Si alguien sufre un
      daño por una de esas fallas, los reportes previos del mismo punto ayudan a demostrar que la entidad responsable
      ya conocía el problema.</p>
    <h2>¿Qué diferencia hay entre una falla y un incidente?</h2>
    <p><b>Falla (condición):</b> el problema existe pero nadie salió herido. Reportarla sirve como antecedente.<br>
      <b>Incidente:</b> la falla te causó un daño (una lesión, un daño a tu vehículo o a tus cosas).</p>
    <h2>¿Cómo consigo un abogado?</h2>
    <ol>
      <li>Reporta el incidente con fotos, ubicación y lo que te pasó.</li>
      <li>Un operador de REDA lo revisa y lo valida.</li>
      <li>En <b>Mis reportes</b> pulsa <b>Buscar abogado</b>. Los abogados verificados ven un resumen anónimo.</li>
      <li>Los interesados se postulan. En <b>Mis casos</b> los comparas y <b>eliges</b> con quién seguir.</li>
      <li>Solo el abogado que elijas ve tu expediente completo.</li>
    </ol>
    <h2>¿Cuánto cuesta?</h2>
    <p>Para el ciudadano, nada. REDA cobra al abogado una cuota fija cuando un ciudadano lo elige. REDA no cobra
      porcentajes del resultado ni participa en los honorarios.</p>
    <h2>¿Quién ve mis fotos?</h2>
    <p>Las fotos son evidencia privada: solo tú, el equipo de revisión y el abogado que elijas. En el mapa los demás solo
      ven que el reporte tiene fotos.</p>
    <h2>Importante</h2>
    <p>REDA no es un abogado, no presta asesoría jurídica y no garantiza el resultado de ningún caso. Para demandar al
      Estado existen plazos legales; consúltalos con tu abogado cuanto antes.</p>`,

  terminos: () => `
    <h1>Términos de uso</h1>${BORRADOR}
    <h2>1. Qué ofrece REDA</h2>
    <p>Una plataforma para documentar fallas en la infraestructura pública y para que las personas afectadas por ellas
      puedan contactar abogados independientes. REDA no presta servicios jurídicos.</p>
    <h2>2. Tu cuenta</h2>
    <p>Eres responsable de la veracidad de lo que reportas y de mantener segura tu contraseña. Reportar hechos falsos o
      fotos que no corresponden puede llevar al rechazo de los reportes y a la cancelación de la cuenta.</p>
    <h2>3. Abogados aliados</h2>
    <p>Los abogados son profesionales independientes, verificados por REDA con su tarjeta profesional. La relación
      profesional y los honorarios se acuerdan directamente entre el ciudadano y el abogado. REDA cobra al abogado una
      cuota fija por cada caso en que un ciudadano lo elige; no cobra cuota litis ni porcentaje alguno.</p>
    <h2>4. Sin garantía de resultado</h2>
    <p>REDA no garantiza que un caso prospere ni que una entidad responda. La información de la plataforma no es
      asesoría jurídica.</p>
    <h2>5. Contenido</h2>
    <p>Al subir fotos y descripciones autorizas a REDA a almacenarlas y mostrarlas según la política de privacidad.</p>`,

  privacidad: () => `
    <h1>Política de privacidad y tratamiento de datos</h1>${BORRADOR}
    <p>Marco de referencia: Ley 1581 de 2012 y sus decretos reglamentarios (protección de datos personales en Colombia).</p>
    <h2>Qué datos tratamos y para qué</h2>
    <ul>
      <li><b>Datos de cuenta</b> (nombre, correo): para identificarte y comunicarnos contigo.</li>
      <li><b>Reportes</b> (ubicación, descripción, fotos, fechas): para documentar fallas y construir antecedentes.</li>
      <li><b>Datos del daño</b>, incluida la información de salud (lesiones, atención médica): son <b>datos sensibles</b>.
        Solo los ven el equipo de revisión y el abogado que tú elijas.</li>
      <li><b>Testigos</b> (nombre y contacto): solo para el equipo de revisión y el abogado elegido.</li>
    </ul>
    <h2>Quién ve qué</h2>
    <ul>
      <li>En el mapa (con sesión): categoría, barrio, fecha, estado, descripción de la falla y ubicación aproximada.
        Nunca tu nombre, tus fotos ni los datos del daño.</li>
      <li>Abogados antes de que los elijas: un resumen anónimo.</li>
      <li>El abogado que elijas: tu expediente completo, porque al elegirlo lo autorizas.</li>
    </ul>
    <h2>Tus derechos</h2>
    <p>Conocer, actualizar y rectificar tus datos, pedir prueba de la autorización, ser informado sobre su uso,
      revocar la autorización o pedir la supresión cuando proceda, y presentar quejas ante la Superintendencia de
      Industria y Comercio.</p>
    <h2>Seguridad</h2>
    <p>Contraseñas cifradas con BCrypt, fotos con huella SHA-256, acceso por roles y registro de cambios.</p>`,
};

export function mostrar(contenedor, [pagina]) {
  // sin sesión la página se muestra en la pantalla de acceso; con sesión, como página de la aplicación
  const conSesion = !!sesion.actual();
  contenedor.innerHTML = `
    <div class="${conSesion ? "contenido contenido-estrecho" : ""} vista texto-largo">
      ${PAGINAS[pagina]()}
      <p class="enlaces"><a href="${conSesion ? "#/mapa" : "#/entrar"}">← Volver</a></p>
    </div>`;
}
