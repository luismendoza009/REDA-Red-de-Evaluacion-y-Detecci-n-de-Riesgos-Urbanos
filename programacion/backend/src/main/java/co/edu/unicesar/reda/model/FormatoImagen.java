package co.edu.unicesar.reda.model;

import java.util.Arrays;
import java.util.Optional;

/**
 * Formatos de foto permitidos como evidencia (coinciden con el CHECK ck_evidencias_mime).
 * El formato se reconoce por los primeros bytes del archivo ("número mágico"), NO por la extensión
 * ni por el Content-Type que manda el navegador: ambos los puede falsificar cualquiera.
 */
public enum FormatoImagen {
    JPEG("image/jpeg", "jpg"),
    PNG("image/png", "png"),
    WEBP("image/webp", "webp");

    private final String mime;
    private final String extension;

    FormatoImagen(String mime, String extension) {
        this.mime = mime;
        this.extension = extension;
    }

    public String mime() { return mime; }
    public String extension() { return extension; }

    public static Optional<FormatoImagen> detectar(byte[] b) {
        if (empiezaCon(b, 0, 0xFF, 0xD8, 0xFF)) {
            return Optional.of(JPEG);
        }
        if (empiezaCon(b, 0, 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A)) {
            return Optional.of(PNG);
        }
        if (empiezaCon(b, 0, 'R', 'I', 'F', 'F') && empiezaCon(b, 8, 'W', 'E', 'B', 'P')) {
            return Optional.of(WEBP);
        }
        return Optional.empty();
    }

    public static Optional<FormatoImagen> porMime(String mime) {
        return Arrays.stream(values()).filter(f -> f.mime.equals(mime)).findFirst();
    }

    private static boolean empiezaCon(byte[] datos, int desde, int... firma) {
        if (datos == null || datos.length < desde + firma.length) {
            return false;
        }
        for (int i = 0; i < firma.length; i++) {
            if ((datos[desde + i] & 0xFF) != firma[i]) {
                return false;
            }
        }
        return true;
    }
}
