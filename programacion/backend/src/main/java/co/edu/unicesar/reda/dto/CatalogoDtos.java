package co.edu.unicesar.reda.dto;

import java.math.BigDecimal;

/** Respuestas de los catálogos (listas para los formularios). */
public final class CatalogoDtos {

    private CatalogoDtos() {
    }

    public record CategoriaResponse(Integer id, String nombre, String descripcion) {
    }

    public record ZonaResponse(Integer id, String nombre, String tipo, BigDecimal latitud, BigDecimal longitud) {
    }

    /** Barrio sugerido para el punto que el ciudadano marcó en el mapa. */
    public record ZonaSugeridaResponse(Integer id, String nombre, int distanciaMetros) {
    }

    public record EntidadResponse(Integer id, String nombre, String tipo) {
    }
}
