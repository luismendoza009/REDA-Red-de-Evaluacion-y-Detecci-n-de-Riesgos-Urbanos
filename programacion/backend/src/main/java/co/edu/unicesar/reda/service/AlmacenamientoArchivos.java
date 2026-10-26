package co.edu.unicesar.reda.service;

import java.util.Optional;

/**
 * Dónde se guardan físicamente las fotos.
 * DIP / Variaciones protegidas: hoy es una carpeta del servidor; en producción sería un almacenamiento
 * de objetos (S3, Azure Blob...). Los servicios no cambian: solo se agrega otra implementación.
 */
public interface AlmacenamientoArchivos {

    /** Guarda el archivo con un nombre generado y devuelve su ruta relativa (la que se guarda en la base). */
    String guardar(byte[] contenido, String extension);

    /** Lee el archivo; vacío si no existe (p. ej. las fotos de la semilla simulada). */
    Optional<byte[]> leer(String rutaRelativa);
}
