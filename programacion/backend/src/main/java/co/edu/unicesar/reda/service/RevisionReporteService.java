package co.edu.unicesar.reda.service;

import co.edu.unicesar.reda.dto.ReporteDtos.CambioEstadoRequest;
import co.edu.unicesar.reda.dto.ReporteDtos.CambioEstadoResponse;
import co.edu.unicesar.reda.model.Estado;
import co.edu.unicesar.reda.model.HistorialEstado;
import co.edu.unicesar.reda.model.Reporte;
import co.edu.unicesar.reda.repository.EstadoRepository;
import co.edu.unicesar.reda.repository.ReporteRepository;
import co.edu.unicesar.reda.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Caso de uso: un operador REVISA un reporte (cambia su estado, que queda en el historial). */
@Service
public class RevisionReporteService {

    private final ReporteRepository reportes;
    private final UsuarioRepository usuarios;
    private final EstadoRepository estados;

    public RevisionReporteService(ReporteRepository reportes, UsuarioRepository usuarios, EstadoRepository estados) {
        this.reportes = reportes;
        this.usuarios = usuarios;
        this.estados = estados;
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
        return ReporteMapper.cambio(cambio);
    }
}
