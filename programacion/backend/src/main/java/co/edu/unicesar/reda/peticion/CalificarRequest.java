package co.edu.unicesar.reda.peticion;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CalificarRequest(
        @NotNull @Min(1) @Max(5) Short puntaje,
        @Size(min = 3, max = 1000) String comentario) {
}
