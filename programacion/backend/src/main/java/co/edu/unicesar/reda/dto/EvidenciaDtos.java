package co.edu.unicesar.reda.dto;

import java.time.OffsetDateTime;

public final class EvidenciaDtos {

    private EvidenciaDtos() {
    }

    /** Datos de una foto. No incluye la ruta en disco: el archivo se pide por su id (y con permiso). */
    public record EvidenciaResponse(Integer id, String tipoMime, int tamanoBytes, String hashSha256,
                                    OffsetDateTime fechaCaptura, OffsetDateTime fechaCarga, String urlArchivo) {
    }

    /** Contenido del archivo para responderlo por HTTP. */
    public record ArchivoResponse(byte[] contenido, String tipoMime) {
    }
}
