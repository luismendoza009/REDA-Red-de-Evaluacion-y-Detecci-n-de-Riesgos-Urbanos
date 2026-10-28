package co.edu.unicesar.reda.peticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CerrarRequest(@NotBlank @Size(min = 10, max = 1000) String observacion) {
}
