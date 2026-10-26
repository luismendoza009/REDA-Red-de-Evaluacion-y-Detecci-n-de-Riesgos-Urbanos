package co.edu.unicesar.reda.service;

import co.edu.unicesar.reda.exception.ConflictoException;
import co.edu.unicesar.reda.exception.ReglaNegocioException;
import co.edu.unicesar.reda.model.Reporte;
import co.edu.unicesar.reda.model.Usuario;
import co.edu.unicesar.reda.repository.EvidenciaRepository;
import co.edu.unicesar.reda.repository.ReporteRepository;
import co.edu.unicesar.reda.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EvidenciaServiceTest {

    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 1, 2, 3, 4};
    private static final byte[] TEXTO = "no soy una foto".getBytes();

    @Mock ReporteRepository reportes;
    @Mock UsuarioRepository usuarios;
    @Mock EvidenciaRepository evidencias;
    @Mock AlmacenamientoArchivos almacenamiento;
    @InjectMocks EvidenciaService servicio;

    private Reporte reporte;
    private final Usuario autor = mock(Usuario.class);
    private final Usuario vecino = mock(Usuario.class);

    @BeforeEach
    void preparar() {
        reporte = mock(Reporte.class);
        when(reportes.findById(1)).thenReturn(Optional.of(reporte));
        when(usuarios.findById(10)).thenReturn(Optional.of(autor));
        when(usuarios.findById(11)).thenReturn(Optional.of(vecino));
        doThrow(new ReglaNegocioException("NO_ES_AUTOR", "no")).when(reporte).verificarPuedeRecibirEvidencia(eq(vecino), anyInt());
    }

    @Test
    void quienNoEsAutorRecibeNoEsAutorAunqueLaFotoYaExista() {
        when(evidencias.existsByHashSha256(anyString())).thenReturn(true);

        var e = assertThrows(ReglaNegocioException.class, () -> servicio.subir(1, JPEG, 11));

        assertEquals("NO_ES_AUTOR", e.getCodigo(), "no debe revelar que la foto ya existe");
        verifyNoInteractions(almacenamiento);
    }

    @Test
    void unArchivoQueNoEsImagenNoSeGuardaEnDisco() {
        var e = assertThrows(ReglaNegocioException.class, () -> servicio.subir(1, TEXTO, 10));
        assertEquals("FORMATO_NO_PERMITIDO", e.getCodigo());
        verifyNoInteractions(almacenamiento);
    }

    @Test
    void unaFotoDuplicadaNoSeGuardaEnDisco() {
        when(evidencias.existsByHashSha256(EvidenciaService.sha256(JPEG))).thenReturn(true);
        assertThrows(ConflictoException.class, () -> servicio.subir(1, JPEG, 10));
        verifyNoInteractions(almacenamiento);
    }

    @Test
    void elHashEsElSha256EnHexadecimal() {
        // valor conocido de SHA-256 para la cadena "abc"
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                EvidenciaService.sha256("abc".getBytes()));
    }
}
