package co.edu.unicesar.reda.service;

import co.edu.unicesar.reda.dto.CatalogoDtos.CategoriaResponse;
import co.edu.unicesar.reda.dto.CatalogoDtos.EntidadResponse;
import co.edu.unicesar.reda.dto.CatalogoDtos.ZonaResponse;
import co.edu.unicesar.reda.dto.CatalogoDtos.ZonaSugeridaResponse;
import co.edu.unicesar.reda.exception.ReglaNegocioException;
import co.edu.unicesar.reda.model.Zona;
import co.edu.unicesar.reda.repository.CategoriaRepository;
import co.edu.unicesar.reda.repository.EntidadPublicaRepository;
import co.edu.unicesar.reda.repository.ZonaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** Listas de los catálogos para llenar los formularios. */
@Service
@Transactional(readOnly = true)
public class CatalogoService {

    private final CategoriaRepository categorias;
    private final ZonaRepository zonas;
    private final EntidadPublicaRepository entidades;

    private final double radioSugerenciaMetros;

    public CatalogoService(CategoriaRepository categorias, ZonaRepository zonas, EntidadPublicaRepository entidades,
                           @Value("${reda.zonas.radio-sugerencia-metros}") double radioSugerenciaMetros) {
        this.categorias = categorias;
        this.zonas = zonas;
        this.radioSugerenciaMetros = radioSugerenciaMetros;
        this.entidades = entidades;
    }

    public List<CategoriaResponse> categorias() {
        return categorias.findAllByOrderByNombreAsc().stream()
                .map(c -> new CategoriaResponse(c.getId(), c.getNombre(), c.getDescripcion())).toList();
    }

    public List<ZonaResponse> zonas() {
        return zonas.findAllByOrderByNombreAsc().stream()
                .map(z -> new ZonaResponse(z.getId(), z.getNombre(), z.getTipo(),
                        z.getLatitudReferencia(), z.getLongitudReferencia())).toList();
    }

    /** Barrio cuyo punto de referencia está más cerca del punto marcado (vacío si ninguno está dentro del radio). */
    public Optional<ZonaSugeridaResponse> zonaSugerida(double latitud, double longitud) {
        if (latitud < -90 || latitud > 90 || longitud < -180 || longitud > 180) {
            throw new ReglaNegocioException("COORDENADA_INVALIDA", "La coordenada está fuera de rango");
        }
        return Zona.masCercana(zonas.findAll(), latitud, longitud, radioSugerenciaMetros)
                .map(z -> new ZonaSugeridaResponse(z.getId(), z.getNombre(),
                        (int) Math.round(z.distanciaA(latitud, longitud).getAsDouble())));
    }

    public List<EntidadResponse> entidades() {
        return entidades.findAllByOrderByNombreAsc().stream()
                .map(e -> new EntidadResponse(e.getId(), e.getNombre(), e.getTipo())).toList();
    }
}
