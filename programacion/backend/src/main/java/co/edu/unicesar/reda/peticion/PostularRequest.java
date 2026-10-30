package co.edu.unicesar.reda.peticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostularRequest(@NotBlank @Size(min = 20, max = 1500) String mensaje) {
}
