package co.edu.unicesar.reda.service;

import co.edu.unicesar.reda.config.PropiedadesReda;
import co.edu.unicesar.reda.exception.RecursoNoEncontradoException;
import co.edu.unicesar.reda.model.AgrupadorPuntos;
import co.edu.unicesar.reda.model.Frecuencia;
import co.edu.unicesar.reda.model.SituacionCaso;
import co.edu.unicesar.reda.repository.MapaPublicoRepository.PuntoMapa;
import co.edu.unicesar.reda.repository.MapaPublicoRepository;
import co.edu.unicesar.reda.respuesta.ConteoSituaciones;
import co.edu.unicesar.reda.respuesta.DetallePublicoResponse;
import co.edu.unicesar.reda.respuesta.Hito;
import co.edu.unicesar.reda.respuesta.PuntoMapaResponse;
import co.edu.unicesar.reda.respuesta.PuntoResponse;
import co.edu.unicesar.reda.respuesta.ReporteEnPunto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Mapa público. La privacidad se garantiza en la base (vista v_mapa_publico): aquí solo se agrupa
 * y se ordena. Ningún dato sale con autor, datos del daño ni fotos.
 */
@Service
@Transactional(readOnly = true)
public class MapaPublicoService {

    private final MapaPublicoRepository mapa;
    private final AgrupadorPuntos<PuntoMapa> agrupador;

    public MapaPublicoService(MapaPublicoRepository mapa, PropiedadesReda propiedades) {
        this.mapa = mapa;
        this.agrupador = new AgrupadorPuntos<>(propiedades.mapa().radioAgrupacionMetros(),
                p -> p.latitud().doubleValue(), p -> p.longitud().doubleValue());
    }

    /** Lista plana de reportes (se conserva para integraciones que la usan). */
    public List<PuntoMapaResponse> puntosSueltos() {
        return mapa.puntos().stream().map(ReporteMapper::punto).toList();
    }

    /** Reportes agrupados por cercanía, de los puntos más frecuentes a los menos. */
    public List<PuntoResponse> puntosAgrupados() {
        return agrupador.agrupar(mapa.puntos(), Comparator.comparing(PuntoMapa::fecha).thenComparing(PuntoMapa::idReporte))
                .stream()
                .map(MapaPublicoService::aPunto)
                .sorted(Comparator.comparingInt(PuntoResponse::totalReportes).reversed()
                        .thenComparing(PuntoResponse::ultimoReporte, Comparator.reverseOrder()))
                .toList();
    }

    public DetallePublicoResponse detalle(Integer idReporte) {
        PuntoMapa p = mapa.porId(idReporte).orElseThrow(() -> new RecursoNoEncontradoException("Reporte", idReporte));
        List<Hito> hitos = mapa.lineaDeTiempo(idReporte).stream().map(h -> new Hito(h.estado(), h.fecha())).toList();
        return new DetallePublicoResponse(p.idReporte(), p.tipoReporte(), p.categoria(), p.zona(), p.estado(),
                p.situacion().name(), p.fecha(), p.descripcion(), p.nivelGravedad(), p.numEvidencias(), hitos);
    }

    private static PuntoResponse aPunto(AgrupadorPuntos.Grupo<PuntoMapa> g) {
        List<PuntoMapa> reportes = g.elementos();
        Map<SituacionCaso, Long> porSituacion = reportes.stream()
                .collect(Collectors.groupingBy(PuntoMapa::situacion, Collectors.counting()));
        List<ReporteEnPunto> lista = reportes.stream()
                .sorted(Comparator.comparing(PuntoMapa::fecha).reversed())
                .map(r -> new ReporteEnPunto(r.idReporte(), r.tipoReporte(), r.categoria(), r.estado(),
                        r.situacion().name(), r.fecha(), r.descripcion(), r.nivelGravedad(), r.numEvidencias()))
                .toList();
        return new PuntoResponse(
                reportes.stream().map(PuntoMapa::idReporte).min(Integer::compare).orElseThrow(),
                redondear(g.latitudCentro()), redondear(g.longitudCentro()),
                masFrecuente(reportes, PuntoMapa::zona),
                Frecuencia.de(g.tamano()).name(),
                g.tamano(),
                (int) reportes.stream().filter(r -> "INCIDENTE".equals(r.tipoReporte())).count(),
                reportes.stream().map(PuntoMapa::categoria).distinct().sorted().toList(),
                reportes.stream().map(PuntoMapa::fecha).min(Comparator.naturalOrder()).orElseThrow(),
                reportes.stream().map(PuntoMapa::fecha).max(Comparator.naturalOrder()).orElseThrow(),
                new ConteoSituaciones(porSituacion.getOrDefault(SituacionCaso.CON_ABOGADO, 0L),
                        porSituacion.getOrDefault(SituacionCaso.ABIERTO, 0L),
                        porSituacion.getOrDefault(SituacionCaso.CERRADO, 0L)),
                lista);
    }

    private static String masFrecuente(List<PuntoMapa> reportes, Function<PuntoMapa, String> campo) {
        return reportes.stream().collect(Collectors.groupingBy(campo, Collectors.counting()))
                .entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("");
    }

    private static double redondear(double coordenada) {
        return Math.round(coordenada * 10_000) / 10_000.0;
    }
}
