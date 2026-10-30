package co.edu.unicesar.reda.service;

import co.edu.unicesar.reda.exception.RecursoNoEncontradoException;
import co.edu.unicesar.reda.model.Estado;
import co.edu.unicesar.reda.model.HistorialEstado;
import co.edu.unicesar.reda.model.Notificacion;
import co.edu.unicesar.reda.model.Reporte;
import co.edu.unicesar.reda.model.Usuario;
import co.edu.unicesar.reda.repository.NotificacionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock NotificacionRepository notificaciones;
    @InjectMocks NotificacionService servicio;

    @Test
    void listaSoloLosAvisosDelUsuarioYDevuelveElTotalNoLeido() {
        Notificacion notificacion = mock(Notificacion.class);
        HistorialEstado historial = mock(HistorialEstado.class);
        Reporte reporte = mock(Reporte.class);
        Estado anterior = mock(Estado.class);
        Estado nuevo = mock(Estado.class);
        OffsetDateTime fecha = OffsetDateTime.now();
        when(notificaciones.findByDestinatario_IdOrderByFechaCreacionDesc(eq(42), any(Pageable.class)))
                .thenReturn(List.of(notificacion));
        when(notificaciones.countByDestinatario_IdAndFechaLecturaIsNull(42)).thenReturn(1L);
        when(notificacion.getId()).thenReturn(9);
        when(notificacion.getHistorial()).thenReturn(historial);
        when(notificacion.getFechaCreacion()).thenReturn(fecha);
        when(historial.getReporte()).thenReturn(reporte);
        when(historial.getEstadoAnterior()).thenReturn(anterior);
        when(historial.getEstadoNuevo()).thenReturn(nuevo);
        when(reporte.getId()).thenReturn(15);
        when(anterior.getNombre()).thenReturn("Pendiente");
        when(nuevo.getNombre()).thenReturn("En revisión");

        var respuesta = servicio.listar(42);

        assertEquals(1, respuesta.noLeidas());
        assertEquals(1, respuesta.items().size());
        assertEquals(15, respuesta.items().getFirst().idReporte());
        assertEquals("Pendiente", respuesta.items().getFirst().estadoAnterior());
        assertEquals("En revisión", respuesta.items().getFirst().estadoNuevo());
        verify(notificaciones).findByDestinatario_IdOrderByFechaCreacionDesc(eq(42), any(Pageable.class));
    }

    @Test
    void marcaComoLeidaSoloUnaNotificacionPropia() {
        Notificacion notificacion = mock(Notificacion.class);
        when(notificaciones.findByIdAndDestinatario_Id(9, 42)).thenReturn(Optional.of(notificacion));

        servicio.marcarLeida(9, 42);

        verify(notificacion).marcarLeida();
    }

    @Test
    void noRevelaNotificacionesDeOtraPersona() {
        when(notificaciones.findByIdAndDestinatario_Id(9, 42)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.marcarLeida(9, 42));
        verify(notificaciones, never()).save(any(Notificacion.class));
    }

    @Test
    void registraUnaNotificacionVinculadaAlHistorialYAlAutor() {
        Reporte reporte = mock(Reporte.class);
        Usuario autor = mock(Usuario.class);
        HistorialEstado cambio = mock(HistorialEstado.class);
        when(reporte.getAutor()).thenReturn(autor);
        ArgumentCaptor<Notificacion> notificacion = ArgumentCaptor.forClass(Notificacion.class);

        servicio.registrarCambioReporte(reporte, cambio);

        verify(notificaciones).save(notificacion.capture());
        assertEquals(autor, notificacion.getValue().getDestinatario());
        assertEquals(cambio, notificacion.getValue().getHistorial());
        assertNull(notificacion.getValue().getFechaLectura());
    }
}
