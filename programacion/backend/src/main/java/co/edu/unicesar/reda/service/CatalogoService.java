package co.edu.unicesar.reda.service;

import co.edu.unicesar.reda.dto.CatalogoDtos.CategoriaResponse;
import co.edu.unicesar.reda.dto.CatalogoDtos.EntidadResponse;
import co.edu.unicesar.reda.dto.CatalogoDtos.ZonaResponse;
import co.edu.unicesar.reda.repository.CategoriaRepository;
import co.edu.unicesar.reda.repository.EntidadPublicaRepository;
import co.edu.unicesar.reda.repository.ZonaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Listas de los catálogos para llenar los formularios. */
@Service
@Transactional(readOnly = true)
public class CatalogoService {

    private final CategoriaRepository categorias;
    private final ZonaRepository zonas;
    private final EntidadPublicaRepository entidades;

    public CatalogoService(CategoriaRepository categorias, ZonaRepository zonas, EntidadPublicaRepository entidades) {
        this.categorias = categorias;
        this.zonas = zonas;
        this.entidades = entidades;
    }

    public List<CategoriaResponse> categorias() {
        return categorias.findAllByOrderByNombreAsc().stream()
                .map(c -> new CategoriaResponse(c.getId(), c.getNombre(), c.getDescripcion())).toList();
    }

    public List<ZonaResponse> zonas() {
        return zonas.findAllByOrderByNombreAsc().stream()
                .map(z -> new ZonaResponse(z.getId(), z.getNombre(), z.getTipo())).toList();
    }

    public List<EntidadResponse> entidades() {
        return entidades.findAllByOrderByNombreAsc().stream()
                .map(e -> new EntidadResponse(e.getId(), e.getNombre(), e.getTipo())).toList();
    }
}
