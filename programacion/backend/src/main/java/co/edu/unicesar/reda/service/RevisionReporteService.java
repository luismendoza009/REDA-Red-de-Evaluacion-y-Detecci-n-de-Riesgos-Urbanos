package co.edu.unicesar.reda.service;

import co.edu.unicesar.reda.model.Estado;
import co.edu.unicesar.reda.model.HistorialEstado;
import co.edu.unicesar.reda.model.NombreEstado;
import co.edu.unicesar.reda.model.Reporte;
import co.edu.unicesar.reda.peticion.CambioEstadoRequest;
import co.edu.unicesar.reda.repository.EstadoRepository;
import co.edu.unicesar.reda.repository.ReporteRepository;
import co.edu.unicesar.reda.repository.RevisionRepository;
import co.edu.unicesar.reda.repository.UsuarioRepository;
import co.edu.unicesar.reda.respuesta.CambioEstadoResponse;
import co.edu.unicesar.reda.respuesta.ReporteEnRevision;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Caso de uso: un operador REVISA reportes (bandeja y cambio de estado, que queda en el historial). */
@Service
public class RevisionReporteService {

    private final ReporteRepository reportes;
    private final RevisionRepository revision;
    private final UsuarioRepository usuarios;
    private final EstadoRepository estados;
    private final NotificacionService notificaciones;

    public RevisionReporteService(ReporteRepository reportes, RevisionRepository revision, UsuarioRepository usuarios,
                                  EstadoRepository estados, NotificacionService notificaciones) {
        this.reportes = reportes;
        this.revision = revision;
        this.usuarios = usuarios;
        this.estados = estados;
        this.notificaciones = notificaciones;
    }

    /** Bandeja: pendientes y en revisión, primero los incidentes; con los estados a los que puede pasar cada uno. */
    @Transactional(readOnly = true)
    public List<ReporteEnRevision> bandeja() {
        return revision.bandejaDeRevision().stream().map(r -> new ReporteEnRevision(
                r.getId(), r.getTipo().name(), r.getCategoria().getNombre(), r.getZona().getNombre(),
                r.getEstado().getNombre(), r.getFechaReporte(), r.getDescripcion(), r.getNivelGravedad(),
                r.getEvidencias().size(),
                r.getEstado().tipo().siguientesPermitidos().stream().map(NombreEstado::name).sorted().toList()))
                .toList();
    }

    @Transactional
    public CambioEstadoResponse cambiarEstado(Integer idReporte, CambioEstadoRequest datos, Integer idResponsable) {
        Reporte reporte = Busqueda.porId(reportes, idReporte, "Reporte");
        Estado nuevo = estados.findByNombre(datos.nuevoEstado().etiqueta())
                .orElseThrow(() -> new IllegalStateException("Falta el estado " + datos.nuevoEstado() + " en la base"));
        HistorialEstado cambio = reporte.cambiarEstado(nuevo, Busqueda.porId(usuarios, idResponsable, "Usuario"),
                datos.observacion());
        // El reporte ya está gestionado por JPA: no se llama save() (eso haría un merge, que no
        // propaga la inserción del historial). flush() escribe el cambio y el historial ahora mismo,
        // para que un error de la base salga dentro de este método y no al confirmar.
        reportes.flush();
        notificaciones.registrarCambioReporte(reporte, cambio);
        return ReporteMapper.cambio(cambio);
    }
}
