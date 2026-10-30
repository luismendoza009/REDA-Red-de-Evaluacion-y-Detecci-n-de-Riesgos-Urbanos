package co.edu.unicesar.reda.respuesta;

import java.time.LocalDate;

/** Datos de una foto. No incluye la ruta en disco: el archivo se pide por su id (y con permiso). */
public record EvidenciaResponse(Integer id, String tipoMime, int tamanoBytes, String hashSha256,
                                LocalDate fechaCaptura, LocalDate fechaCarga, String urlArchivo) {
}
