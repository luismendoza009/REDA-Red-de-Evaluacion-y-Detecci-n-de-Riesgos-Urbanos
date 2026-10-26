package co.edu.unicesar.reda.service;

import co.edu.unicesar.reda.dto.ReporteDtos.PuntoMapaResponse;
import co.edu.unicesar.reda.repository.ReporteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Mapa público. Lee la vista v_mapa_publico, que ya excluye autor, datos del daño y reportes
 * rechazados, y redondea las coordenadas. La privacidad se garantiza en la base, no en Java.
 */
@Service
public class MapaPublicoService {

    private final ReporteRepository reportes;

    public MapaPublicoService(ReporteRepository reportes) {
        this.reportes = reportes;
    }

    @Transactional(readOnly = true)
    public List<PuntoMapaResponse> puntos() {
        return reportes.mapaPublico().stream().map(ReporteMapper::punto).toList();
    }
}
