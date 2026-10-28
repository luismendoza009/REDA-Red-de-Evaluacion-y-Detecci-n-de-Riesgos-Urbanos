package co.edu.unicesar.reda.peticion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TestigoRequest(
        @NotBlank @Size(max = 120) String nombre,
        @NotBlank @Size(max = 150) String contacto) {
}
