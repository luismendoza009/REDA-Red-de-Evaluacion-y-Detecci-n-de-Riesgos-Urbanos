package co.edu.unicesar.reda.service;

import co.edu.unicesar.reda.exception.RecursoNoEncontradoException;
import co.edu.unicesar.reda.model.NombreEstado;
import co.edu.unicesar.reda.peticion.CambioEstadoRequest;
import co.edu.unicesar.reda.repository.EstadoRepository;
import co.edu.unicesar.reda.repository.ReporteRepository;
import co.edu.unicesar.reda.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RevisionReporteServiceTest {

    @Mock ReporteRepository reportes;
    @Mock UsuarioRepository usuarios;
    @Mock EstadoRepository estados;
    @InjectMocks RevisionReporteService servicio;

    @Test
    void cambiarEstadoDeUnReporteQueNoExisteLanza404YNoEscribeNada() {
        when(reportes.findById(999)).thenReturn(Optional.empty());

        var e = assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.cambiarEstado(999, new CambioEstadoRequest(NombreEstado.EN_REVISION, null), 2));

        assertEquals("NO_ENCONTRADO", e.getCodigo());
        verify(reportes, never()).flush();
    }
}
