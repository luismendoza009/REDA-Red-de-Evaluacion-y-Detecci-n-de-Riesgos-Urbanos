// =============================================================================
// Acceso a la API REST de REDA y manejo de la sesión.
// El token se guarda en sessionStorage: se borra al cerrar la pestaña.
// =============================================================================

const CLAVE_SESION = "reda.sesion";

export const sesion = {
  actual() {
    try {
      return JSON.parse(sessionStorage.getItem(CLAVE_SESION));
    } catch {
      return null;
    }
  },
  guardar(datos) {
    try { sessionStorage.setItem(CLAVE_SESION, JSON.stringify(datos)); } catch { /* modo privado: la sesión vive solo en memoria */ }
  },
  cerrar() {
    try { sessionStorage.removeItem(CLAVE_SESION); } catch { /* nada que borrar */ }
  },
};

/** Error de la API con el formato único del backend (codigo + mensaje). */
export class ErrorApi extends Error {
  constructor(estado, cuerpo) {
    super(cuerpo?.mensaje ?? `Error ${estado}`);
    this.estado = estado;
    this.codigo = cuerpo?.codigo ?? "DESCONOCIDO";
  }
}

async function pedir(ruta, { metodo = "GET", json, formulario, binario = false } = {}) {
  const cabeceras = {};
  const s = sesion.actual();
  if (s?.token) cabeceras.Authorization = `Bearer ${s.token}`;
  let cuerpo;
  if (json !== undefined) {
    cabeceras["Content-Type"] = "application/json";
    cuerpo = JSON.stringify(json);
  } else if (formulario) {
    cuerpo = formulario; // el navegador pone el Content-Type multipart con su "boundary"
  }
  const r = await fetch(ruta, { method: metodo, headers: cabeceras, body: cuerpo });
  if (!r.ok) {
    let detalle = null;
    try { detalle = await r.json(); } catch { /* respuesta sin cuerpo */ }
    if (r.status === 401 && s?.token) sesion.cerrar(); // token vencido
    throw new ErrorApi(r.status, detalle);
  }
  if (binario) return r.blob();
  return r.status === 204 ? null : r.json();
}

export const api = {
  puntos: () => pedir("/api/publico/puntos"),
  detallePublico: (id) => pedir(`/api/publico/reportes/${id}`),
  categorias: () => pedir("/api/catalogos/categorias"),
  iniciarSesion: (correo, password) => pedir("/api/auth/login", { metodo: "POST", json: { correo, password } }),
  misReportes: () => pedir("/api/reportes/mios"),
  evidencias: (idReporte) => pedir(`/api/reportes/${idReporte}/evidencias`),
  archivo: (url) => pedir(url, { binario: true }),
  subirFoto(idReporte, archivo) {
    const datos = new FormData();
    datos.append("archivo", archivo);
    return pedir(`/api/reportes/${idReporte}/evidencias`, { metodo: "POST", formulario: datos });
  },
};
