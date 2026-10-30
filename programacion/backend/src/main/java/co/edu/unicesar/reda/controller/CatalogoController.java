package co.edu.unicesar.reda.controller;

import co.edu.unicesar.reda.respuesta.CategoriaResponse;
import co.edu.unicesar.reda.respuesta.EntidadResponse;
import co.edu.unicesar.reda.respuesta.ZonaResponse;
import co.edu.unicesar.reda.respuesta.ZonaSugeridaResponse;
import co.edu.unicesar.reda.service.CatalogoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalogos")
@Tag(name = "Catálogos")
public class CatalogoController {

    private final CatalogoService catalogos;

    public CatalogoController(CatalogoService catalogos) {
        this.catalogos = catalogos;
    }

    @GetMapping("/categorias")
    public List<CategoriaResponse> categorias() {
        return catalogos.categorias();
    }

    @GetMapping("/zonas")
    public List<ZonaResponse> zonas() {
        return catalogos.zonas();
    }

    /** 200 con el barrio sugerido, o 204 si ningún barrio conocido queda cerca del punto. */
    @GetMapping("/zonas/sugerida")
    public ResponseEntity<ZonaSugeridaResponse> zonaSugerida(@RequestParam double latitud, @RequestParam double longitud) {
        return catalogos.zonaSugerida(latitud, longitud)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/entidades")
    public List<EntidadResponse> entidades() {
        return catalogos.entidades();
    }
}
