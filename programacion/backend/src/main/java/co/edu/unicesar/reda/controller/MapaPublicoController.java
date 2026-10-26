package co.edu.unicesar.reda.controller;

import co.edu.unicesar.reda.dto.ReporteDtos.PuntoMapaResponse;
import co.edu.unicesar.reda.service.MapaPublicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/publico")
@Tag(name = "Público")
public class MapaPublicoController {

    private final MapaPublicoService mapa;

    public MapaPublicoController(MapaPublicoService mapa) {
        this.mapa = mapa;
    }

    @GetMapping("/mapa")
    @Operation(summary = "Puntos del mapa público (sin datos personales)")
    public List<PuntoMapaResponse> mapa() {
        return mapa.puntos();
    }
}
