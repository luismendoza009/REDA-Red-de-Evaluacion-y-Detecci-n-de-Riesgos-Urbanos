// =============================================================================
// Acceso a la API REST de REDA y manejo de la sesión.
// El token se guarda en sessionStorage: se borra al cerrar la pestaña.
// =============================================================================

const CLAVE_SESION = "reda.sesion";
const oyentesSalida = [];

export const sesion = {
  actual() {
    try {
      return JSON.parse(sessionStorage.getItem(CLAVE_SESION));
    } catch {
      return null;
    }
  },
  guardar(datos) {
    try { sessionStorage.setItem(CLAVE_SESION, JSON.stringify(datos)); } catch { /* modo privado */ }
  },
  cerrar(motivo) {
    try { sessionStorage.removeItem(CLAVE_SESION); } catch { /* nada que borrar */ }
    oyentesSalida.forEach((f) => f(motivo));
  },
  /** Se llama cuando la sesión termina (cerrar sesión o token vencido). */
  alSalir(funcion) {
    oyentesSalida.push(funcion);
  },
  tieneRol(...roles) {
    const s = this.actual();
    return !!s && roles.includes(s.rol);
  },
};

/** Error de la API con el formato único del backend (codigo + mensaje + campos). */
export class ErrorApi extends Error {
  constructor(estado, cuerpo) {
    super(cuerpo?.mensaje ?? (estado === 0 ? "No hay conexión con el servidor" : `Error ${estado}`));
    this.estado = estado;
    this.codigo = cuerpo?.codigo ?? "DESCONOCIDO";
    this.campos = cuerpo?.campos ?? {};
    this.segundosRestantes = cuerpo?.segundosRestantes ?? null;
    this.intentosRestantes = cuerpo?.intentosRestantes ?? null;
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
  let r;
  try {
    r = await fetch(ruta, { method: metodo, headers: cabeceras, body: cuerpo });
  } catch {
    throw new ErrorApi(0, null);
  }
  if (!r.ok) {
    let detalle = null;
    try { detalle = await r.json(); } catch { /* respuesta sin cuerpo */ }
    if (r.status === 401 && s?.token) sesion.cerrar("Tu sesión venció. Vuelve a iniciar sesión.");
    throw new ErrorApi(r.status, detalle);
  }
  if (binario) return r.blob();
  return r.status === 204 ? null : r.json();
}

const post = (ruta, json) => pedir(ruta, { metodo: "POST", json });

export const api = {
  // cuentas
  iniciarSesion: (correo, password) => post("/api/auth/login", { correo, password }),
  registrar: (datos) => post("/api/auth/registro", datos),
  registrarAbogado: (datos) => post("/api/auth/registro-abogado", datos),
  recuperar: (correo) => post("/api/auth/recuperar", { correo }),
  restablecer: (token, nuevaPassword) => post("/api/auth/restablecer", { token, nuevaPassword }),
  perfil: () => pedir("/api/cuenta"),
  actualizarPerfil: (nombre) => pedir("/api/cuenta", { metodo: "PATCH", json: { nombre } }),
  cambiarPassword: (actual, nueva) => post("/api/cuenta/password", { actual, nueva }),
  actualizarPerfilAbogado: (datos) => pedir("/api/cuenta/abogado", { metodo: "PATCH", json: datos }),
  cambiarFoto: (archivo) => {
    const f = new FormData();
    f.append("archivo", archivo);
    return pedir("/api/cuenta/foto", { metodo: "PUT", formulario: f });
  },
  quitarFoto: () => pedir("/api/cuenta/foto", { metodo: "DELETE" }),
  // catálogos y mapa
  categorias: () => pedir("/api/catalogos/categorias"),
  zonas: () => pedir("/api/catalogos/zonas"),
  zonaSugerida: (lat, lon) => pedir(`/api/catalogos/zonas/sugerida?latitud=${lat}&longitud=${lon}`),
  entidades: () => pedir("/api/catalogos/entidades"),
  puntos: () => pedir("/api/mapa/puntos"),
  detallePublico: (id) => pedir(`/api/mapa/reportes/${id}`),
  // reportes
  crearCondicion: (falla) => post("/api/reportes/condiciones", falla),
  crearIncidente: (datos) => post("/api/reportes/incidentes", datos),
  misReportes: () => pedir("/api/reportes/mios"),
  cambiarEstado: (id, nuevoEstado, observacion) =>
    pedir(`/api/reportes/${id}/estado`, { metodo: "PATCH", json: { nuevoEstado, observacion } }),
  bandejaRevision: () => pedir("/api/revision/bandeja"),
  // fotos (privadas)
  evidencias: (idReporte) => pedir(`/api/reportes/${idReporte}/evidencias`),
  archivo: (url) => pedir(url, { binario: true }),
  subirFoto(idReporte, archivo) {
    const datos = new FormData();
    datos.append("archivo", archivo);
    return pedir(`/api/reportes/${idReporte}/evidencias`, { metodo: "POST", formulario: datos });
  },
  // casos
  solicitarAbogado: (idReporte) => post("/api/casos", { idReporte, autorizaCompartirResumen: true }),
  misCasos: () => pedir("/api/casos/mios"),
  elegirAbogado: (idCaso, idPostulacion) => post(`/api/casos/${idCaso}/eleccion`, { idPostulacion }),
  calificarAbogado: (idCaso, puntaje, comentario) => post(`/api/casos/${idCaso}/calificacion`, { puntaje, comentario }),
  casosDisponibles: () => pedir("/api/abogado/casos-disponibles"),
  postular: (idCaso, mensaje) => post(`/api/abogado/casos/${idCaso}/postulacion`, { mensaje }),
  retirarPostulacion: (idCaso) => pedir(`/api/abogado/casos/${idCaso}/postulacion`, { metodo: "DELETE" }),
  misCasosAbogado: () => pedir("/api/abogado/mis-casos"),
  cerrarCaso: (idCaso, observacion) => post(`/api/abogado/casos/${idCaso}/cierre`, { observacion }),
  // administración
  abogadosAdmin: () => pedir("/api/admin/abogados"),
  verificarAbogado: (id) => post(`/api/admin/abogados/${id}/verificacion`),
};
