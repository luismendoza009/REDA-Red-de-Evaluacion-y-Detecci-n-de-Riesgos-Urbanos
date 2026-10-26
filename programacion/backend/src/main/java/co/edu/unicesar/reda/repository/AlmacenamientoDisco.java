package co.edu.unicesar.reda.repository;

import co.edu.unicesar.reda.config.PropiedadesReda;
import co.edu.unicesar.reda.service.AlmacenamientoArchivos;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Guarda las fotos en una carpeta del servidor, organizadas por año/mes, con nombre aleatorio (UUID):
 * nunca se usa el nombre original (podría traer datos personales o caracteres peligrosos como "../").
 */
@Repository
public class AlmacenamientoDisco implements AlmacenamientoArchivos {

    private final Path raiz;

    public AlmacenamientoDisco(PropiedadesReda propiedades) {
        this.raiz = Path.of(propiedades.almacenamiento().directorio()).toAbsolutePath().normalize();
    }

    @Override
    public String guardar(byte[] contenido, String extension) {
        LocalDate hoy = LocalDate.now();
        String relativa = String.format("evidencias/%d/%02d/%s.%s", hoy.getYear(), hoy.getMonthValue(),
                UUID.randomUUID().toString().replace("-", ""), extension);
        Path destino = resolver(relativa);
        try {
            Files.createDirectories(destino.getParent());
            Files.write(destino, contenido);
            return relativa;
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar la foto", e);
        }
    }

    @Override
    public Optional<byte[]> leer(String rutaRelativa) {
        Path archivo = resolver(rutaRelativa);
        try {
            return Files.isRegularFile(archivo) ? Optional.of(Files.readAllBytes(archivo)) : Optional.empty();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer la foto", e);
        }
    }

    /** Impide salir de la carpeta raíz con rutas como "../../windows" (path traversal). */
    private Path resolver(String rutaRelativa) {
        Path ruta = raiz.resolve(rutaRelativa).normalize();
        if (!ruta.startsWith(raiz)) {
            throw new IllegalArgumentException("Ruta fuera del almacenamiento: " + rutaRelativa);
        }
        return ruta;
    }
}
