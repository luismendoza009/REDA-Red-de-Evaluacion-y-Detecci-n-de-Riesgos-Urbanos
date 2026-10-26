package co.edu.unicesar.reda.service;

import co.edu.unicesar.reda.dto.ReporteDtos.PuntoMapaResponse;
import co.edu.unicesar.reda.repository.MapaPublicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Mapa público. La privacidad se garantiza en la base (vista v_mapa_publico), no en Java. */
@Service
public class MapaPublicoService {

    private final MapaPublicoRepository mapa;

    public MapaPublicoService(MapaPublicoRepository mapa) {
        this.mapa = mapa;
    }

    @Transactional(readOnly = true)
    public List<PuntoMapaResponse> puntos() {
        return mapa.puntos().stream().map(ReporteMapper::punto).toList();
    }
}
