package co.edu.unicesar.reda.controller;

import co.edu.unicesar.reda.respuesta.LugarGeocodificadoResponse;
import co.edu.unicesar.reda.service.GeocodificacionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/geocodificacion")
@Tag(name = "Geocodificación")
@Validated
public class GeocodificacionController {

    private final GeocodificacionService geocodificacion;

    public GeocodificacionController(GeocodificacionService geocodificacion) {
        this.geocodificacion = geocodificacion;
    }

    @GetMapping("/direcciones")
    public List<LugarGeocodificadoResponse> buscar(
            @RequestParam @NotBlank @Size(min = 3, max = 200) String consulta) {
        return geocodificacion.buscar(consulta);
    }
}
