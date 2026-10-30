package co.edu.unicesar.reda.service;

import co.edu.unicesar.reda.exception.RecursoNoEncontradoException;
import co.edu.unicesar.reda.model.Estado;
import co.edu.unicesar.reda.model.HistorialEstado;
import co.edu.unicesar.reda.model.NombreEstado;
import co.edu.unicesar.reda.model.Reporte;
import co.edu.unicesar.reda.model.Usuario;
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
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RevisionReporteServiceTest {

    @Mock ReporteRepository reportes;
    @Mock UsuarioRepository usuarios;
    @Mock EstadoRepository estados;
    @Mock NotificacionService notificaciones;
    @InjectMocks RevisionReporteService servicio;

    @Test
    void cambiarEstadoDeUnReporteQueNoExisteLanza404YNoEscribeNada() {
        when(reportes.findById(999)).thenReturn(Optional.empty());

        var e = assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.cambiarEstado(999, new CambioEstadoRequest(NombreEstado.EN_REVISION, null), 2));

        assertEquals("NO_ENCONTRADO", e.getCodigo());
        verify(reportes, never()).flush();
    }

    @Test
    void cambioDeEstadoRegistraNotificacionParaElAutorEnLaMismaOperacion() {
        Reporte reporte = mock(Reporte.class);
        Usuario responsable = mock(Usuario.class);
        HistorialEstado cambio = mock(HistorialEstado.class);
        Estado anterior = mock(Estado.class);
        Estado nuevo = mock(Estado.class);
        when(reportes.findById(7)).thenReturn(Optional.of(reporte));
        when(estados.findByNombre(NombreEstado.VALIDADO.etiqueta())).thenReturn(Optional.of(nuevo));
        when(usuarios.findById(2)).thenReturn(Optional.of(responsable));
        when(reporte.cambiarEstado(nuevo, responsable, null)).thenReturn(cambio);
        when(cambio.getEstadoAnterior()).thenReturn(anterior);
        when(cambio.getEstadoNuevo()).thenReturn(nuevo);
        when(cambio.getFechaCambio()).thenReturn(OffsetDateTime.now());
        when(cambio.getResponsable()).thenReturn(responsable);
        when(anterior.getNombre()).thenReturn("En revisión");
        when(nuevo.getNombre()).thenReturn("Validado");
        when(responsable.getNombre()).thenReturn("Operador");

        var resultado = servicio.cambiarEstado(7, new CambioEstadoRequest(NombreEstado.VALIDADO, null), 2);

        assertEquals("Validado", resultado.estadoNuevo());
        verify(reportes).flush();
        verify(notificaciones).registrarCambioReporte(reporte, cambio);
    }
}
