package co.edu.unicesar.reda.peticion;

import jakarta.validation.constraints.NotNull;

public record ElegirRequest(@NotNull Integer idPostulacion) {
}
