package co.edu.unicesar.reda.service;

import co.edu.unicesar.reda.exception.RecursoNoEncontradoException;
import co.edu.unicesar.reda.model.HistorialEstado;
import co.edu.unicesar.reda.model.Notificacion;
import co.edu.unicesar.reda.model.Reporte;
import co.edu.unicesar.reda.repository.NotificacionRepository;
import co.edu.unicesar.reda.respuesta.NotificacionResponse;
import co.edu.unicesar.reda.respuesta.NotificacionesResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificacionService {

    private static final int MAXIMAS_RECIENTES = 50;

    private final NotificacionRepository notificaciones;

    public NotificacionService(NotificacionRepository notificaciones) {
        this.notificaciones = notificaciones;
    }

    @Transactional(readOnly = true)
    public NotificacionesResponse listar(Integer idUsuario) {
        List<NotificacionResponse> items = notificaciones
                .findByDestinatario_IdOrderByFechaCreacionDesc(idUsuario, PageRequest.of(0, MAXIMAS_RECIENTES))
                .stream().map(NotificacionService::respuesta).toList();
        return new NotificacionesResponse(items, noLeidas(idUsuario));
    }

    @Transactional(readOnly = true)
    public long noLeidas(Integer idUsuario) {
        return notificaciones.countByDestinatario_IdAndFechaLecturaIsNull(idUsuario);
    }

    @Transactional
    public void marcarLeida(Integer id, Integer idUsuario) {
        notificaciones.findByIdAndDestinatario_Id(id, idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Notificación", id))
                .marcarLeida();
    }

    @Transactional
    public void registrarCambioReporte(Reporte reporte, HistorialEstado cambio) {
        notificaciones.save(Notificacion.cambioDeReporte(reporte.getAutor(), cambio));
    }

    private static NotificacionResponse respuesta(Notificacion notificacion) {
        HistorialEstado cambio = notificacion.getHistorial();
        return new NotificacionResponse(notificacion.getId(), cambio.getReporte().getId(),
                cambio.getEstadoAnterior().getNombre(), cambio.getEstadoNuevo().getNombre(),
                notificacion.getFechaCreacion(), notificacion.getFechaLectura() != null);
    }
}
