package co.edu.unicesar.reda.controller;

import co.edu.unicesar.reda.dto.CatalogoDtos.CategoriaResponse;
import co.edu.unicesar.reda.dto.CatalogoDtos.EntidadResponse;
import co.edu.unicesar.reda.dto.CatalogoDtos.ZonaResponse;
import co.edu.unicesar.reda.service.CatalogoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @GetMapping("/entidades")
    public List<EntidadResponse> entidades() {
        return catalogos.entidades();
    }
}
