package co.edu.unicesar.reda.service;

import java.util.Optional;

/**
 * Dónde se guardan físicamente las fotos.
 * DIP / Variaciones protegidas: hoy es una carpeta del servidor; en producción sería un almacenamiento
 * de objetos (S3, Azure Blob...). Los servicios no cambian: solo se agrega otra implementación.
 */
public interface AlmacenamientoArchivos {

    /** Guarda una foto de evidencia con un nombre generado y devuelve su ruta relativa (la que va en la base). */
    default String guardar(byte[] contenido, String extension) {
        return guardar("evidencias", contenido, extension);
    }

    /** Guarda el archivo dentro de {@code carpeta} (evidencias, perfiles...) con un nombre generado. */
    String guardar(String carpeta, byte[] contenido, String extension);

    /** Borra un archivo que ya no se usa (p. ej. la foto de perfil anterior). No falla si no existe. */
    void borrar(String rutaRelativa);

    /** Lee el archivo; vacío si no existe (p. ej. las fotos de la semilla simulada). */
    Optional<byte[]> leer(String rutaRelativa);
}
