package co.edu.unicesar.reda.respuesta;

/** Contenido del archivo para responderlo por HTTP. */
public record ArchivoResponse(byte[] contenido, String tipoMime) {
}
