package co.edu.unicesar.reda.model;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormatoImagenTest {

    private static byte[] bytes(int... valores) {
        byte[] b = new byte[valores.length];
        for (int i = 0; i < valores.length; i++) {
            b[i] = (byte) valores[i];
        }
        return b;
    }

    @Test
    void reconoceLosFormatosPermitidosPorSusPrimerosBytes() {
        assertEquals(FormatoImagen.JPEG, FormatoImagen.detectar(bytes(0xFF, 0xD8, 0xFF, 0xE0, 0, 0)).orElseThrow());
        assertEquals(FormatoImagen.PNG, FormatoImagen.detectar(bytes(0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0)).orElseThrow());
        assertEquals(FormatoImagen.WEBP, FormatoImagen.detectar(bytes('R', 'I', 'F', 'F', 1, 2, 3, 4, 'W', 'E', 'B', 'P')).orElseThrow());
    }

    @Test
    void unArchivoDeTextoConExtensionJpgNoPasa() {
        byte[] texto = "esto no es una imagen".getBytes(StandardCharsets.UTF_8);
        assertTrue(FormatoImagen.detectar(texto).isEmpty());
    }

    @Test
    void archivosVaciosOCortosNoPasan() {
        assertTrue(FormatoImagen.detectar(new byte[0]).isEmpty());
        assertTrue(FormatoImagen.detectar(bytes(0xFF, 0xD8)).isEmpty());
        assertTrue(FormatoImagen.detectar(null).isEmpty());
    }

    @Test
    void unRiffQueNoEsWebpNoPasa() {
        // RIFF también es la cabecera de los .wav y .avi
        assertTrue(FormatoImagen.detectar(bytes('R', 'I', 'F', 'F', 1, 2, 3, 4, 'W', 'A', 'V', 'E')).isEmpty());
    }
}
