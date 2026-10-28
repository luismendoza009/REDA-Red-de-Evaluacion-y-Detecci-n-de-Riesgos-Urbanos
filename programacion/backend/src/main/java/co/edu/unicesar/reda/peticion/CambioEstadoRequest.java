package co.edu.unicesar.reda.peticion;

import co.edu.unicesar.reda.model.NombreEstado;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CambioEstadoRequest(
        @NotNull NombreEstado nuevoEstado,
        @Size(max = 1000) String observacion) {
}
