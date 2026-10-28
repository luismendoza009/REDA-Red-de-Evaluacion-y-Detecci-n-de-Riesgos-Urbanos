package co.edu.unicesar.reda.service;

import co.edu.unicesar.reda.exception.AccesoDenegadoException;
import co.edu.unicesar.reda.exception.RecursoNoEncontradoException;
import co.edu.unicesar.reda.exception.ReglaNegocioException;
import co.edu.unicesar.reda.model.Caso;
import co.edu.unicesar.reda.model.NombreEstado;
import co.edu.unicesar.reda.model.Reporte;
import co.edu.unicesar.reda.repository.ReporteRepository;
import co.edu.unicesar.reda.repository.UsuarioRepository;
import co.edu.unicesar.reda.respuesta.AntecedenteResponse;
import co.edu.unicesar.reda.respuesta.MiReporteResponse;
import co.edu.unicesar.reda.respuesta.ReporteDetalleResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Caso de uso: CONSULTAR reportes (los míos, el detalle, los antecedentes). Solo lectura. */
@Service
@Transactional(readOnly = true)
public class ConsultaReporteService {

    private final ReporteRepository reportes;
    private final UsuarioRepository usuarios;
    private final BuscadorAntecedentes buscadorAntecedentes;

    public ConsultaReporteService(ReporteRepository reportes, UsuarioRepository usuarios,
                                  BuscadorAntecedentes buscadorAntecedentes) {
        this.reportes = reportes;
        this.usuarios = usuarios;
        this.buscadorAntecedentes = buscadorAntecedentes;
    }

    public List<MiReporteResponse> misReportes(Integer idAutor) {
        return reportes.findByAutorIdOrderByFechaReporteDesc(idAutor).stream().map(r -> {
            Caso caso = r.getIncidente() == null ? null : r.getIncidente().getCaso();
            NombreEstado estado = r.getEstado().tipo();
            boolean puedeBuscarAbogado = r.getIncidente() != null && caso == null
                    && (estado == NombreEstado.VALIDADO || estado == NombreEstado.EN_PROCESO);
            return new MiReporteResponse(r.getId(), r.getTipo().name(), r.getCategoria().getNombre(),
                    r.getZona().getNombre(), r.getEstado().getNombre(), r.getFechaReporte(), r.getDescripcion(),
                    r.getEvidencias().size(), puedeBuscarAbogado, caso == null ? null : caso.getId(),
                    caso == null ? null : caso.getEstado().getNombre());
        }).toList();
    }

    public ReporteDetalleResponse detalle(Integer idReporte, Integer idSolicitante) {
        Reporte reporte = reportes.findConHistorialById(idReporte)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reporte", idReporte));
        verificarAcceso(reporte, idSolicitante);
        return ReporteMapper.detalle(reporte);
    }

    /** Antecedentes de un incidente, calculados en el momento (incluye reportes posteriores a su registro). */
    public List<AntecedenteResponse> antecedentes(Integer idReporte, Integer idSolicitante) {
        Reporte reporte = Busqueda.porId(reportes, idReporte, "Reporte");
        verificarAcceso(reporte, idSolicitante);
        if (reporte.getIncidente() == null) {
            throw new ReglaNegocioException("NO_ES_INCIDENTE", "Solo los incidentes tienen antecedentes");
        }
        return buscadorAntecedentes.buscar(reporte.getCategoria().getId(), reporte.getLatitud(), reporte.getLongitud(),
                        reporte.getIncidente().getFechaHoraHecho())
                .stream().map(ReporteMapper::antecedente).toList();
    }

    private void verificarAcceso(Reporte reporte, Integer idSolicitante) {
        if (!reporte.puedeSerVistoPor(Busqueda.porId(usuarios, idSolicitante, "Usuario"))) {
            throw new AccesoDenegadoException("Solo el autor o el personal de revisión pueden ver este reporte");
        }
    }
}
