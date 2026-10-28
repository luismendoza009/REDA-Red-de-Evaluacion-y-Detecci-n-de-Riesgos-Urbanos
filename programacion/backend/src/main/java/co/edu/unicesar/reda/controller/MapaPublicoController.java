package co.edu.unicesar.reda.controller;

import co.edu.unicesar.reda.dto.MapaDtos.DetallePublicoResponse;
import co.edu.unicesar.reda.dto.MapaDtos.PuntoResponse;
import co.edu.unicesar.reda.dto.ReporteDtos.PuntoMapaResponse;
import co.edu.unicesar.reda.service.MapaPublicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Mapa (exige sesión). Nada de lo que devuelve contiene datos personales, del daño ni fotos. */
@RestController
@RequestMapping("/api/mapa")
@Tag(name = "Mapa")
public class MapaPublicoController {

    private final MapaPublicoService mapa;

    public MapaPublicoController(MapaPublicoService mapa) {
        this.mapa = mapa;
    }

    @GetMapping("/mapa")
    @Operation(summary = "Reportes sueltos del mapa público")
    public List<PuntoMapaResponse> mapa() {
        return mapa.puntosSueltos();
    }

    @GetMapping("/puntos")
    @Operation(summary = "Puntos del mapa: reportes agrupados por cercanía, con su frecuencia y situación")
    public List<PuntoResponse> puntos() {
        return mapa.puntosAgrupados();
    }

    @GetMapping("/reportes/{id}")
    @Operation(summary = "Detalle público de un reporte: línea de tiempo y número de fotos (privadas)")
    public DetallePublicoResponse detalle(@PathVariable Integer id) {
        return mapa.detalle(id);
    }
}
