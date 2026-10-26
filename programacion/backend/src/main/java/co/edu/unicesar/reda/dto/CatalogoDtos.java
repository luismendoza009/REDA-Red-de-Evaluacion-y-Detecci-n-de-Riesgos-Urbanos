package co.edu.unicesar.reda.dto;

/** Respuestas de los catálogos (listas para los formularios). */
public final class CatalogoDtos {

    private CatalogoDtos() {
    }

    public record CategoriaResponse(Integer id, String nombre, String descripcion) {
    }

    public record ZonaResponse(Integer id, String nombre, String tipo) {
    }

    public record EntidadResponse(Integer id, String nombre, String tipo) {
    }
}
