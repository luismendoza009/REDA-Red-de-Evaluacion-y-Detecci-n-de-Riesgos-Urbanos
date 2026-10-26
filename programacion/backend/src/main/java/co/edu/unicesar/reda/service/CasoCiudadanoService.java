package co.edu.unicesar.reda.service;

import co.edu.unicesar.reda.dto.CasoDtos.CasoCiudadanoResponse;
import co.edu.unicesar.reda.exception.ReglaNegocioException;
import co.edu.unicesar.reda.model.*;
import co.edu.unicesar.reda.repository.CasoRepository;
import co.edu.unicesar.reda.repository.EstadoCasoRepository;
import co.edu.unicesar.reda.repository.ReporteRepository;
import co.edu.unicesar.reda.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Casos de uso del ciudadano: pedir abogado, ver quién se postuló y elegir. */
@Service
public class CasoCiudadanoService {

    private final ReporteRepository reportes;
    private final UsuarioRepository usuarios;
    private final CasoRepository casos;
    private final EstadoCasoRepository estadosCaso;
    private final CasoMapper mapper;

    public CasoCiudadanoService(ReporteRepository reportes, UsuarioRepository usuarios, CasoRepository casos,
                                EstadoCasoRepository estadosCaso, CasoMapper mapper) {
        this.reportes = reportes;
        this.usuarios = usuarios;
        this.casos = casos;
        this.estadosCaso = estadosCaso;
        this.mapper = mapper;
    }

    /** "Buscar abogado": el caso se publica (resumen anónimo) para que los abogados se postulen. */
    @Transactional
    public CasoCiudadanoResponse solicitarAbogado(Integer idReporte, boolean autorizaCompartir, Integer idCiudadano) {
        Reporte reporte = Busqueda.porId(reportes, idReporte, "Reporte");
        Incidente incidente = reporte.getIncidente();
        if (incidente == null) {
            throw new ReglaNegocioException("NO_ES_INCIDENTE", "Solo se busca abogado para incidentes (hubo un daño)");
        }
        Usuario operadorQueValido = reporte.getHistorial().stream()
                .filter(h -> h.getEstadoNuevo().tipo() == NombreEstado.VALIDADO)
                .reduce((primero, segundo) -> segundo)
                .map(HistorialEstado::getResponsable)
                .orElse(null);
        Caso caso = incidente.solicitarAbogado(Busqueda.porId(usuarios, idCiudadano, "Usuario"), operadorQueValido,
                estadosCaso.obtener(EstadoCaso.Nombre.REMITIDO), autorizaCompartir);
        return mapper.paraCiudadano(casos.save(caso));
    }

    @Transactional(readOnly = true)
    public List<CasoCiudadanoResponse> misCasos(Integer idCiudadano) {
        return casos.deCiudadano(idCiudadano).stream().map(mapper::paraCiudadano).toList();
    }

    /**
     * El ciudadano elige abogado. TRANSACCIÓN todo-o-nada: postulaciones resueltas, remisión facturable
     * creada y caso aceptado. Sin la transacción, una falla a mitad de camino podría dejar al abogado
     * "elegido" sin remisión (no se cobra) o con dos abogados elegidos para el mismo caso.
     */
    @Transactional
    public CasoCiudadanoResponse elegir(Integer idCaso, Integer idPostulacion, Integer idCiudadano) {
        Caso caso = Busqueda.porId(casos, idCaso, "Caso");
        caso.elegir(idPostulacion, Busqueda.porId(usuarios, idCiudadano, "Usuario"),
                estadosCaso.obtener(EstadoCaso.Nombre.ACEPTADO));
        casos.flush();
        return mapper.paraCiudadano(caso);
    }
}
